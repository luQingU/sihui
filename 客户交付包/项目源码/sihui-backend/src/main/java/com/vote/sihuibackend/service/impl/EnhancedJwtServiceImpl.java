package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.dto.AuthResponse;
import com.vote.sihuibackend.security.UserPrincipal;
import com.vote.sihuibackend.service.EnhancedJwtService;
import com.vote.sihuibackend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 增强JWT服务实现类
 */
@Slf4j
@Service
public class EnhancedJwtServiceImpl implements EnhancedJwtService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private final JwtUtil jwtUtil;

    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long accessTokenExpiration; // 24小时

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshTokenExpiration; // 7天

    @Value("${session.max-concurrent:5}")
    private int maxConcurrentSessions; // 最大并发会话数

    @Value("${session.timeout:3600000}")
    private Long sessionTimeout; // 会话超时时间，1小时

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final String BLACKLIST_PREFIX = "jwt_blacklist:";

    public EnhancedJwtServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String generateEnhancedAccessToken(UserPrincipal userPrincipal, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        claims.put("sessionId", sessionId);
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("type", "access");

        return createToken(claims, userPrincipal.getUsername(), accessTokenExpiration);
    }

    @Override
    public String generateEnhancedRefreshToken(UserPrincipal userPrincipal, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getId());
        claims.put("sessionId", sessionId);
        claims.put("type", "refresh");

        String refreshToken = createToken(claims, userPrincipal.getUsername(), refreshTokenExpiration);

        // 存储会话信息
        storeSessionInfo(sessionId, userPrincipal.getId(), refreshToken);

        return refreshToken;
    }

    @Override
    public TokenInfo validateAndGetTokenInfo(String token) {
        try {
            // 检查Token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                return new TokenInfo(null, null, null, null, false, false);
            }

            Claims claims = getAllClaimsFromToken(token);

            String username = claims.getSubject();
            Long userId = Long.valueOf(claims.get("userId").toString());
            String sessionId = (String) claims.get("sessionId");

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");
            if (roles == null) {
                roles = new ArrayList<>();
            }

            boolean expired = claims.getExpiration().before(new Date());

            // 检查会话是否有效
            boolean sessionValid = isSessionValid(sessionId, userId);
            boolean valid = !expired && sessionValid;

            if (valid) {
                // 更新会话活跃时间
                updateSessionActivity(sessionId, userId);
            }

            return new TokenInfo(username, userId, sessionId, roles, valid, expired);

        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return new TokenInfo(null, null, null, null, false, false);
        }
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        try {
            TokenInfo tokenInfo = validateAndGetTokenInfo(refreshToken);

            if (!tokenInfo.isValid()) {
                throw new RuntimeException("刷新令牌无效或已过期");
            }

            // 验证是否为刷新令牌
            Claims claims = getAllClaimsFromToken(refreshToken);
            if (!"refresh".equals(claims.get("type"))) {
                throw new RuntimeException("无效的刷新令牌类型");
            }

            // 创建新的访问令牌
            UserPrincipal userPrincipal = createUserPrincipal(tokenInfo);
            String newAccessToken = generateEnhancedAccessToken(userPrincipal, tokenInfo.getSessionId());

            // 构建响应
            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    tokenInfo.getUserId(),
                    tokenInfo.getUsername(),
                    null, // email - 需要从数据库查询
                    null, // nickname - 需要从数据库查询
                    new HashSet<>(tokenInfo.getRoles()),
                    "ACTIVE");

            return new AuthResponse(newAccessToken, refreshToken, accessTokenExpiration / 1000, userInfo);

        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("令牌刷新失败: " + e.getMessage());
        }
    }

    @Override
    public void logout(String sessionId, Long userId) {
        try {
            if (redisTemplate != null) {
                // 获取会话中的所有令牌
                String sessionKey = SESSION_PREFIX + sessionId;
                Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);

                // 将令牌加入黑名单
                for (Object value : sessionData.values()) {
                    if (value instanceof String) {
                        addTokenToBlacklist((String) value);
                    }
                }

                // 删除会话信息
                redisTemplate.delete(sessionKey);

                // 从用户会话列表中移除
                String userSessionsKey = USER_SESSIONS_PREFIX + userId;
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);

                log.info("用户 {} 的会话 {} 已注销", userId, sessionId);
            } else {
                log.info("Redis不可用，跳过会话注销");
            }

        } catch (Exception e) {
            log.error("注销会话失败: {}", e.getMessage());
        }
    }

    @Override
    public void forceLogoutAllSessions(Long userId) {
        try {
            if (redisTemplate != null) {
                String userSessionsKey = USER_SESSIONS_PREFIX + userId;
                Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

                if (sessionIds != null) {
                    for (Object sessionId : sessionIds) {
                        logout((String) sessionId, userId);
                    }
                }

                log.info("强制注销用户 {} 的所有会话", userId);
            } else {
                log.info("Redis不可用，跳过强制注销所有会话");
            }

        } catch (Exception e) {
            log.error("强制注销所有会话失败: {}", e.getMessage());
        }
    }

    @Override
    public List<SessionInfo> getUserActiveSessions(Long userId) {
        List<SessionInfo> sessions = new ArrayList<>();

        try {
            if (redisTemplate != null) {
                String userSessionsKey = USER_SESSIONS_PREFIX + userId;
                Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

                if (sessionIds != null) {
                    for (Object sessionIdObj : sessionIds) {
                        String sessionId = (String) sessionIdObj;
                        String sessionKey = SESSION_PREFIX + sessionId;

                        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);
                        if (!sessionData.isEmpty()) {
                            SessionInfo sessionInfo = buildSessionInfo(sessionId, sessionData);
                            sessions.add(sessionInfo);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取用户活跃会话失败: {}", e.getMessage());
        }

        return sessions;
    }

    @Override
    public boolean checkConcurrentSessionLimit(Long userId, int maxSessions) {
        try {
            if (redisTemplate != null) {
                String userSessionsKey = USER_SESSIONS_PREFIX + userId;
                Long sessionCount = redisTemplate.opsForSet().size(userSessionsKey);
                return sessionCount < maxSessions;
            } else {
                return true; // Redis不可用时默认允许
            }
        } catch (Exception e) {
            log.error("检查并发会话限制失败: {}", e.getMessage());
            return true; // 默认允许
        }
    }

    @Override
    public void updateSessionActivity(String sessionId, Long userId) {
        try {
            if (redisTemplate != null) {
                String sessionKey = SESSION_PREFIX + sessionId;
                redisTemplate.opsForHash().put(sessionKey, "lastActivity", System.currentTimeMillis());
                redisTemplate.expire(sessionKey, sessionTimeout, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.error("更新会话活跃状态失败: {}", e.getMessage());
        }
    }

    @Override
    public void cleanupExpiredSessions() {
        // 这个方法可以通过定时任务调用，清理过期的会话
        try {
            if (redisTemplate != null) {
                log.info("开始清理过期会话...");
                // 实现清理逻辑
                // 可以扫描所有会话，检查过期时间并删除
            }
        } catch (Exception e) {
            log.error("清理过期会话失败: {}", e.getMessage());
        }
    }

    // 私有辅助方法

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void storeSessionInfo(String sessionId, Long userId, String refreshToken) {
        try {
            if (redisTemplate != null) {
                String sessionKey = SESSION_PREFIX + sessionId;

                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("userId", userId);
                sessionData.put("refreshToken", refreshToken);
                sessionData.put("loginTime", System.currentTimeMillis());
                sessionData.put("lastActivity", System.currentTimeMillis());
                sessionData.put("deviceInfo", "Unknown"); // 可以从请求头获取
                sessionData.put("ipAddress", "Unknown"); // 可以从请求获取

                redisTemplate.opsForHash().putAll(sessionKey, sessionData);
                redisTemplate.expire(sessionKey, refreshTokenExpiration, TimeUnit.MILLISECONDS);

                // 添加到用户会话集合
                String userSessionsKey = USER_SESSIONS_PREFIX + userId;
                redisTemplate.opsForSet().add(userSessionsKey, sessionId);
                redisTemplate.expire(userSessionsKey, refreshTokenExpiration, TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            log.error("存储会话信息失败: {}", e.getMessage());
        }
    }

    private boolean isSessionValid(String sessionId, Long userId) {
        try {
            if (redisTemplate != null) {
                String sessionKey = SESSION_PREFIX + sessionId;
                return redisTemplate.hasKey(sessionKey);
            } else {
                return true; // Redis不可用时默认认为会话有效
            }
        } catch (Exception e) {
            log.error("检查会话有效性失败: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenBlacklisted(String token) {
        try {
            if (redisTemplate != null) {
                String blacklistKey = BLACKLIST_PREFIX + token.hashCode();
                return redisTemplate.hasKey(blacklistKey);
            } else {
                return false; // Redis不可用时默认认为token不在黑名单中
            }
        } catch (Exception e) {
            log.error("检查令牌黑名单失败: {}", e.getMessage());
            return false;
        }
    }

    private void addTokenToBlacklist(String token) {
        try {
            if (redisTemplate != null) {
                String blacklistKey = BLACKLIST_PREFIX + token.hashCode();
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted",
                        refreshTokenExpiration, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.error("添加令牌到黑名单失败: {}", e.getMessage());
        }
    }

    private UserPrincipal createUserPrincipal(TokenInfo tokenInfo) {
        // 这里需要根据实际的UserPrincipal构造方法调整
        // 简化实现，实际需要从数据库查询用户详细信息
        return new UserPrincipal(
                tokenInfo.getUserId(),
                tokenInfo.getUsername(),
                null, // password
                null, // email
                null, // nickname
                null, // status
                new ArrayList<>() // authorities
        );
    }

    private SessionInfo buildSessionInfo(String sessionId, Map<Object, Object> sessionData) {
        String deviceInfo = (String) sessionData.getOrDefault("deviceInfo", "Unknown");
        String ipAddress = (String) sessionData.getOrDefault("ipAddress", "Unknown");
        String location = "Unknown"; // 可以根据IP地址解析位置

        Long loginTime = (Long) sessionData.getOrDefault("loginTime", 0L);
        Long lastActivity = (Long) sessionData.getOrDefault("lastActivity", 0L);

        return new SessionInfo(sessionId, deviceInfo, ipAddress, location,
                loginTime, lastActivity, false);
    }
}