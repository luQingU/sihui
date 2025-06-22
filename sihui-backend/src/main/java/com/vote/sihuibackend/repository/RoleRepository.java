package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问层
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(String name);

    /**
     * 根据角色名称查找角色(忽略大小写)
     */
    Optional<Role> findByNameIgnoreCase(String name);

    /**
     * 查找所有激活状态的角色
     */
    List<Role> findByStatus(Role.RoleStatus status);

    /**
     * 根据角色名称判断是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据角色名称判断是否存在(忽略大小写)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * 查找角色名称包含指定关键字的角色
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE %?1% OR r.displayName LIKE %?1%")
    List<Role> findByNameContainingOrDisplayNameContaining(String keyword);

    /**
     * 根据权限查找角色
     */
    @Query("SELECT r FROM Role r WHERE JSON_EXTRACT(r.permissions, '$.permissions') LIKE %?1%")
    List<Role> findByPermissionsContaining(String permission);
}