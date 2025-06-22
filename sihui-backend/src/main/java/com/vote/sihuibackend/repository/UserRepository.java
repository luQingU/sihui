package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.entity.Role;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    @Cacheable(value = "users", key = "'username:' + #username")
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    @Cacheable(value = "users", key = "'email:' + #email")
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    @Cacheable(value = "users", key = "'phone:' + #phone")
    Optional<User> findByPhone(String phone);

    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :loginId OR u.email = :loginId")
    @Cacheable(value = "users", key = "'login:' + #loginId")
    Optional<User> findByUsernameOrEmail(@Param("loginId") String loginId);

    /**
     * 根据用户名、邮箱或手机号查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :loginId OR u.email = :loginId OR u.phone = :loginId")
    @Cacheable(value = "users", key = "'loginAll:' + #loginId")
    Optional<User> findByUsernameOrEmailOrPhone(@Param("loginId") String loginId);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据状态查找用户
     */
    @Cacheable(value = "usersByStatus", key = "#status.name()")
    List<User> findByStatus(User.UserStatus status);

    /**
     * 根据状态分页查找用户
     */
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);

    /**
     * 根据邮箱验证状态查找用户
     */
    List<User> findByEmailVerified(Boolean emailVerified);

    /**
     * 根据手机验证状态查找用户
     */
    List<User> findByPhoneVerified(Boolean phoneVerified);

    /**
     * 查找拥有指定角色的用户
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    @Cacheable(value = "usersByRole", key = "#role.id")
    List<User> findByRole(@Param("role") Role role);

    /**
     * 查找拥有指定角色名称的用户
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    @Cacheable(value = "usersByRoleName", key = "#roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * 搜索用户(支持用户名、邮箱、真实姓名模糊查询)
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.realName LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 搜索用户(支持用户名、邮箱、真实姓名模糊查询，非分页)
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.realName LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * 更新用户最后登录信息
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt, u.lastLoginIp = :lastLoginIp WHERE u.id = :userId")
    @CacheEvict(value = "users", key = "'id:' + #userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt,
            @Param("lastLoginIp") String lastLoginIp);

    /**
     * 更新用户邮箱验证状态
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :emailVerified WHERE u.id = :userId")
    @CacheEvict(value = "users", key = "'id:' + #userId")
    void updateEmailVerified(@Param("userId") Long userId, @Param("emailVerified") Boolean emailVerified);

    /**
     * 更新用户手机验证状态
     */
    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = :phoneVerified WHERE u.id = :userId")
    @CacheEvict(value = "users", key = "'id:' + #userId")
    void updatePhoneVerified(@Param("userId") Long userId, @Param("phoneVerified") Boolean phoneVerified);

    /**
     * 统计各状态用户数量
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    @Cacheable(value = "userStats", key = "'statusCount'")
    List<Object[]> countByStatus();

    /**
     * 清除用户相关缓存
     */
    @CacheEvict(value = { "users", "usersByStatus", "usersByRole", "usersByRoleName", "userStats" }, allEntries = true)
    default void clearUserCache() {
        // 用于手动清除用户相关缓存
    }
}