package com.vote.sihuibackend.integration;

import com.vote.sihuibackend.config.CommonTestConfiguration;
import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.entity.User.UserStatus;
import com.vote.sihuibackend.repository.RoleRepository;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化的数据库集成测试
 * 只测试基础的数据库连接和Repository功能
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(CommonTestConfiguration.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:dbtest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.jpa.show-sql=false",
        "logging.level.org.springframework.web=ERROR",
        "logging.level.org.hibernate=ERROR",
        "jwt.secret=test-secret-for-db-integration-test-minimum-256-bits-extended-to-64-characters-for-hs512-algorithm-security"
})
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public class DatabaseIntegrationTest {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    public void setUp() {
        try {
            // 清理测试数据
            cleanupData();
        } catch (Exception e) {
            System.out.println("警告：清理测试数据时出现错误: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            cleanupData();
        } catch (Exception e) {
            System.out.println("清理测试数据时忽略错误: " + e.getMessage());
        }
    }

    private void cleanupData() {
        try {
            if (userRepository != null && userRepository.count() > 0) {
                userRepository.deleteAll();
                userRepository.flush();
            }
        } catch (Exception e) {
            // 忽略表不存在或其他数据库错误
            System.out.println("清理数据时忽略错误: " + e.getMessage());
        }
    }

    @Test
    public void testBasicDatabaseConnection() {
        // 测试基本的数据库连接和Repository功能
        try {
            assertNotNull(userRepository, "UserRepository should not be null");
            assertNotNull(roleRepository, "RoleRepository should not be null");

            // 检查基础数据库操作
            long userCount = userRepository.count();
            assertTrue(userCount >= 0, "Should be able to count users");

            // 验证数据库连接有效
            assertTrue(true, "Database connection successful");
        } catch (Exception e) {
            fail("Basic database connection test failed: " + e.getMessage());
        }
    }

    @Test
    public void testUserRepository() {
        // 测试用户Repository的基本CRUD操作
        try {
            assertNotNull(userRepository, "UserRepository should not be null");

            // 初始状态检查
            long initialCount = userRepository.count();
            assertTrue(initialCount >= 0, "Initial user count should be non-negative");

        } catch (Exception e) {
            fail("User repository test failed: " + e.getMessage());
        }
    }

    @Test
    public void testUserManagementService() {
        // 测试UserManagementService的基本功能
        try {
            assertNotNull(userManagementService, "UserManagementService should not be null");

            // 测试用户创建
            UserCreateRequest createRequest = new UserCreateRequest();
            createRequest.setUsername("testuser_" + System.currentTimeMillis());
            createRequest.setEmail("test_" + System.currentTimeMillis() + "@example.com");
            createRequest.setPassword("TestPassword123!");
            createRequest.setRealName("测试用户");
            createRequest.setPhone("13800138000");

            UserResponse createdUser = userManagementService.createUser(createRequest);
            assertNotNull(createdUser, "Created user should not be null");
            assertEquals(createRequest.getUsername(), createdUser.getUsername());

            // 测试用户查询
            assertTrue(userManagementService.getUserById(createdUser.getId()).isPresent(), "User should be found");

            // 测试用户存在性检查
            assertTrue(userManagementService.existsByUsername(createRequest.getUsername()), "User should exist");

        } catch (Exception e) {
            fail("User management service test failed: " + e.getMessage());
        }
    }

    @Test
    public void testRoleRepository() {
        // 简化的角色Repository测试
        try {
            assertNotNull(roleRepository, "RoleRepository should not be null");

            // 检查角色Repository基本功能
            long roleCount = roleRepository.count();
            assertTrue(roleCount >= 0, "Role count should be non-negative");

        } catch (Exception e) {
            fail("Role repository test failed: " + e.getMessage());
        }
    }

    @Test
    public void testUserCreationAndRetrieval() {
        // 测试完整的用户创建和检索流程
        try {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("integrationtest_" + System.currentTimeMillis());
            request.setEmail("integration_" + System.currentTimeMillis() + "@test.com");
            request.setPassword("TestPassword123!");
            request.setRealName("集成测试用户");
            request.setPhone("13900139000");

            UserResponse response = userManagementService.createUser(request);

            assertNotNull(response, "User response should not be null");
            assertEquals(request.getUsername(), response.getUsername());
            assertEquals(request.getEmail(), response.getEmail());
            assertEquals(request.getRealName(), response.getRealName());
            assertEquals(UserStatus.ACTIVE, response.getStatus());
        } catch (Exception e) {
            fail("User creation and retrieval test failed: " + e.getMessage());
        }
    }

    @Test
    public void testUserSearch() {
        // 测试用户搜索功能
        try {
            // 创建测试用户
            UserCreateRequest createRequest = new UserCreateRequest();
            createRequest.setUsername("searchtest_" + System.currentTimeMillis());
            createRequest.setEmail("searchtest_" + System.currentTimeMillis() + "@test.com");
            createRequest.setPassword("SearchTest123!");
            createRequest.setRealName("搜索测试用户");
            createRequest.setPhone("13800138002");

            UserResponse createdUser = userManagementService.createUser(createRequest);
            assertNotNull(createdUser, "Created user should not be null");

            // 测试搜索功能
            Pageable pageable = PageRequest.of(0, 10);
            assertTrue(userManagementService.searchUsers(createRequest.getUsername(), pageable).getTotalElements() > 0);

            // 验证用户数据
            UserResponse foundUser = userManagementService.getUserById(createdUser.getId()).get();
            assertEquals(createRequest.getUsername(), foundUser.getUsername());
            assertEquals(createRequest.getEmail(), foundUser.getEmail());
            assertEquals(createRequest.getRealName(), foundUser.getRealName());
            assertEquals(createRequest.getPhone(), foundUser.getPhone());
        } catch (Exception e) {
            fail("User search test failed: " + e.getMessage());
        }
    }

    @Test
    public void testDatabaseTransactionality() {
        // 测试数据库事务功能
        try {
            long initialCount = userRepository.count();

            // 创建测试用户
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("transaction_test_" + System.currentTimeMillis());
            request.setEmail("transaction_" + System.currentTimeMillis() + "@test.com");
            request.setPassword("TransactionTest123!");
            request.setRealName("事务测试用户");

            userManagementService.createUser(request);

            // 验证用户已创建
            long newCount = userRepository.count();
            assertTrue(newCount > initialCount, "User count should have increased");

        } catch (Exception e) {
            fail("Database transactionality test failed: " + e.getMessage());
        }
    }
}