package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关联数据访问层
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 根据用户ID查找所有角色关联
     */
    List<UserRole> findByUser_Id(Long userId);

    /**
     * 根据角色ID查找所有用户关联
     */
    List<UserRole> findByRole_Id(Long roleId);

    /**
     * 根据用户ID和角色ID查找关联
     */
    Optional<UserRole> findByUser_IdAndRole_Id(Long userId, Long roleId);

    /**
     * 检查用户是否拥有指定角色
     */
    boolean existsByUser_IdAndRole_Id(Long userId, Long roleId);

    /**
     * 根据用户ID删除所有角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = ?1")
    void deleteByUserId(Long userId);

    /**
     * 根据角色ID删除所有用户关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.role.id = ?1")
    void deleteByRoleId(Long roleId);

    /**
     * 根据用户ID和角色ID删除关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = ?1 AND ur.role.id = ?2")
    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 统计某个角色的用户数量
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.id = ?1")
    long countByRoleId(Long roleId);

    /**
     * 统计某个用户的角色数量
     */
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.user.id = ?1")
    long countByUserId(Long userId);
}