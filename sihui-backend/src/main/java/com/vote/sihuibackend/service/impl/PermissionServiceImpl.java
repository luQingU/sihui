package com.vote.sihuibackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.entity.Role;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.RoleRepository;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "userPermissions", key = "#userId + '_' + #permission.code")
    public boolean hasPermission(Long userId, Permission permission) {
        return hasPermission(userId, permission.getCode());
    }

    @Override
    @Cacheable(value = "userPermissions", key = "#userId + '_' + #permissionCode")
    public boolean hasPermission(Long userId, String permissionCode) {
        // 管理员拥有所有权限
        if (isAdmin(userId)) {
            return true;
        }

        Set<String> userPermissions = getUserPermissionCodes(userId);
        return userPermissions.contains(permissionCode);
    }

    @Override
    public boolean hasAnyPermission(Long userId, Permission... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }

        // 管理员拥有所有权限
        if (isAdmin(userId)) {
            return true;
        }

        Set<String> userPermissions = getUserPermissionCodes(userId);
        for (Permission permission : permissions) {
            if (userPermissions.contains(permission.getCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(Long userId, Permission... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }

        // 管理员拥有所有权限
        if (isAdmin(userId)) {
            return true;
        }

        Set<String> userPermissions = getUserPermissionCodes(userId);
        for (Permission permission : permissions) {
            if (!userPermissions.contains(permission.getCode())) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Cacheable(value = "userPermissions", key = "#userId + '_all'")
    public Set<Permission> getUserPermissions(Long userId) {
        Set<String> permissionCodes = getUserPermissionCodes(userId);
        return permissionCodes.stream()
                .filter(Permission::isValidCode)
                .map(Permission::fromCode)
                .collect(Collectors.toSet());
    }

    @Override
    @Cacheable(value = "userPermissionCodes", key = "#userId")
    public Set<String> getUserPermissionCodes(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return Collections.emptySet();
        }

        User user = userOpt.get();
        Set<String> permissionCodes = new HashSet<>();

        // 获取用户所有角色的权限
        for (Role role : user.getRoles()) {
            Set<String> rolePermissions = parsePermissionsFromJson(role.getPermissions());
            permissionCodes.addAll(rolePermissions);
        }

        log.debug("User {} has permissions: {}", userId, permissionCodes);
        return permissionCodes;
    }

    @Override
    @Cacheable(value = "rolePermissions", key = "#roleId")
    public Set<Permission> getRolePermissions(Long roleId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            return Collections.emptySet();
        }

        Role role = roleOpt.get();
        Set<String> permissionCodes = parsePermissionsFromJson(role.getPermissions());

        return permissionCodes.stream()
                .filter(Permission::isValidCode)
                .map(Permission::fromCode)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void setRolePermissions(Long roleId, Set<Permission> permissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));

        Set<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        String permissionsJson = convertPermissionsToJson(permissionCodes);
        role.setPermissions(permissionsJson);
        roleRepository.save(role);

        log.info("Updated permissions for role {}: {}", roleId, permissionCodes);
    }

    @Override
    @Transactional
    public void addPermissionToRole(Long roleId, Permission permission) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));

        Set<String> existingPermissions = parsePermissionsFromJson(role.getPermissions());
        existingPermissions.add(permission.getCode());

        String permissionsJson = convertPermissionsToJson(existingPermissions);
        role.setPermissions(permissionsJson);
        roleRepository.save(role);

        log.info("Added permission {} to role {}", permission.getCode(), roleId);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Permission permission) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));

        Set<String> existingPermissions = parsePermissionsFromJson(role.getPermissions());
        existingPermissions.remove(permission.getCode());

        String permissionsJson = convertPermissionsToJson(existingPermissions);
        role.setPermissions(permissionsJson);
        roleRepository.save(role);

        log.info("Removed permission {} from role {}", permission.getCode(), roleId);
    }

    @Override
    @Cacheable(value = "userAdmin", key = "#userId")
    public boolean isAdmin(Long userId) {
        return hasPermission(userId, Permission.ADMIN_ALL.getCode());
    }

    @Override
    public boolean canAccessSelfResource(Long currentUserId, Long resourceUserId) {
        // 用户可以访问自己的资源
        if (Objects.equals(currentUserId, resourceUserId)) {
            return true;
        }

        // 管理员可以访问所有资源
        return isAdmin(currentUserId);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Arrays.asList(Permission.values());
    }

    @Override
    public Set<Permission> getPermissionsByCategory(String category) {
        return Arrays.stream(Permission.values())
                .filter(permission -> permission.getCode().startsWith(category + ":"))
                .collect(Collectors.toSet());
    }

    /**
     * 从JSON字符串解析权限集合
     */
    private Set<String> parsePermissionsFromJson(String permissionsJson) {
        if (!StringUtils.hasText(permissionsJson)) {
            return new HashSet<>();
        }

        try {
            Map<String, Object> permissionMap = objectMapper.readValue(permissionsJson, Map.class);
            Object permissions = permissionMap.get("permissions");

            if (permissions instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> permissionList = (List<String>) permissions;
                return new HashSet<>(permissionList);
            }

            return new HashSet<>();
        } catch (JsonProcessingException e) {
            log.error("Failed to parse permissions JSON: {}", permissionsJson, e);
            return new HashSet<>();
        }
    }

    /**
     * 将权限集合转换为JSON字符串
     */
    private String convertPermissionsToJson(Set<String> permissions) {
        try {
            Map<String, Object> permissionMap = new HashMap<>();
            permissionMap.put("permissions", new ArrayList<>(permissions));
            return objectMapper.writeValueAsString(permissionMap);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert permissions to JSON: {}", permissions, e);
            return "{}";
        }
    }
}