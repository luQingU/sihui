package com.vote.sihuibackend.service;

import com.vote.sihuibackend.config.JwtConfig;
import com.vote.sihuibackend.dto.UserCreateRequest;
import com.vote.sihuibackend.dto.UserResponse;
import com.vote.sihuibackend.enums.Permission;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.service.impl.SecurityTestServiceImpl;
import com.vote.sihuibackend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 安全测试服务单元测试
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SecurityTestServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private AuthService authService;

    @Mock
    private PermissionService permissionService;

    private SecurityTestService securityTestService;

    @BeforeEach
    void setUp() {
        securityTestService = new SecurityTestServiceImpl(
                jwtUtil, jwtConfig, passwordEncoder, userRepository,
                userManagementService, authService, permissionService);
    }

    @Test
    void testJwtSecurity_Success() {
        // Given
        when(jwtConfig.getSecret()).thenReturn("mySecretKeyThatIsLongEnoughForHS512AlgorithmToWorkProperly123456789");
        when(jwtConfig.getExpiration()).thenReturn(86400000L); // 24小时
        when(jwtUtil.generateToken(any())).thenReturn("valid.jwt.token");
        when(jwtUtil.getUsernameFromToken("valid.jwt.token")).thenReturn("security_test_user");
        when(jwtUtil.getUsernameFromToken("invalid.jwt.token"))
                .thenThrow(new io.jsonwebtoken.MalformedJwtException("Invalid token"));

        // When
        Map<String, Object> result = securityTestService.testJwtSecurity();

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertTrue((Integer) result.get("passedCount") > 0);
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void testPasswordEncryption_Success() {
        // Given
        String plainPassword = "TestPassword123!";
        String encodedPassword = "$2a$10$encodedPasswordHash";
        String encodedPassword2 = "$2a$10$differentEncodedPasswordHash";

        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword, encodedPassword2);
        when(passwordEncoder.matches(plainPassword, encodedPassword)).thenReturn(true);

        // When
        Map<String, Object> result = securityTestService.testPasswordEncryption();

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertTrue((Integer) result.get("passedCount") > 0);
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void testSqlInjectionProtection_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userManagementService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new RuntimeException("Validation error"));

        // When
        Map<String, Object> result = securityTestService.testSqlInjectionProtection();

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("testedPayloads"));
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void testAccessControl_Success() {
        // Given
        UserResponse mockUser = new UserResponse();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        when(userManagementService.createUser(any(UserCreateRequest.class)))
                .thenReturn(mockUser);
        when(permissionService.hasPermission(1L, Permission.USER_VIEW)).thenReturn(false);
        when(permissionService.hasPermission(1L, Permission.USER_DELETE)).thenReturn(false);
        when(permissionService.hasPermission(1L, Permission.SYSTEM_ADMIN)).thenReturn(false);
        when(permissionService.hasPermission(99999L, Permission.USER_VIEW)).thenReturn(false);
        when(permissionService.hasPermission(isNull(), eq(Permission.USER_VIEW)))
                .thenThrow(new IllegalArgumentException("User ID cannot be null"));

        // When
        Map<String, Object> result = securityTestService.testAccessControl();

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertTrue((Integer) result.get("passedCount") > 0);
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void performVulnerabilityAssessment_Success() {
        // Given
        when(jwtConfig.getSecret()).thenReturn("mySecretKeyThatIsLongEnoughForHS512AlgorithmToWorkProperly123456789");
        when(jwtConfig.getExpiration()).thenReturn(86400000L);
        when(jwtConfig.getRefreshExpiration()).thenReturn(604800000L);
        when(jwtUtil.generateToken(any())).thenReturn("valid.jwt.token");
        when(jwtUtil.getUsernameFromToken("valid.jwt.token")).thenReturn("security_test_user");
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // When
        Map<String, Object> result = securityTestService.performVulnerabilityAssessment();

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("totalIssues"));
        assertNotNull(result.get("totalPassed"));
        assertNotNull(result.get("securityRating"));
        assertNotNull(result.get("testResults"));
        assertNotNull(result.get("timestamp"));
    }
}