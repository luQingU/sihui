package com.vote.sihuibackend.aspect;

import com.vote.sihuibackend.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 输入验证AOP切面
 */
@Slf4j
@Aspect
@Component
public class InputValidationAspect {

    /**
     * 拦截Controller层的所有请求，进行输入验证
     */
    @Around("execution(* com.vote.sihuibackend.controller.*.*(..))")
    public Object validateInput(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." +
                joinPoint.getSignature().getName();

        try {
            // 检查所有字符串参数
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];

                if (arg instanceof String) {
                    String stringArg = (String) arg;

                    // 检查SQL注入
                    if (SecurityUtil.containsSqlInjection(stringArg)) {
                        SecurityUtil.logSecurityEvent("SQL_INJECTION_ATTEMPT",
                                "Method: " + methodName + ", Input: " + stringArg);

                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put("error", "输入包含非法字符");
                        return ResponseEntity.badRequest().body(errorResponse);
                    }

                    // 检查XSS攻击
                    if (SecurityUtil.containsXss(stringArg)) {
                        SecurityUtil.logSecurityEvent("XSS_ATTEMPT",
                                "Method: " + methodName + ", Input: " + stringArg);

                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put("error", "输入包含非法脚本");
                        return ResponseEntity.badRequest().body(errorResponse);
                    }

                    // 清理输入
                    args[i] = SecurityUtil.sanitizeInput(stringArg);
                }

                // 检查DTO对象中的字符串字段
                if (arg != null && arg.getClass().getPackage() != null &&
                        arg.getClass().getPackage().getName().contains("com.vote.sihuibackend.dto")) {
                    validateDtoFields(arg, methodName);
                }
            }

            // 继续执行原方法
            return joinPoint.proceed(args);

        } catch (SecurityException e) {
            log.error("Security validation failed for method: {}", methodName, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "安全验证失败");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 验证DTO对象中的字段
     */
    private void validateDtoFields(Object dto, String methodName) throws SecurityException {
        try {
            java.lang.reflect.Field[] fields = dto.getClass().getDeclaredFields();

            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(dto);

                if (value instanceof String) {
                    String stringValue = (String) value;

                    if (SecurityUtil.containsSqlInjection(stringValue)) {
                        SecurityUtil.logSecurityEvent("SQL_INJECTION_ATTEMPT_DTO",
                                "Method: " + methodName + ", Field: " + field.getName() +
                                        ", Value: " + stringValue);
                        throw new SecurityException("DTO field contains SQL injection: " + field.getName());
                    }

                    if (SecurityUtil.containsXss(stringValue)) {
                        SecurityUtil.logSecurityEvent("XSS_ATTEMPT_DTO",
                                "Method: " + methodName + ", Field: " + field.getName() +
                                        ", Value: " + stringValue);
                        throw new SecurityException("DTO field contains XSS: " + field.getName());
                    }

                    // 清理字段值
                    field.set(dto, SecurityUtil.sanitizeInput(stringValue));
                }
            }
        } catch (IllegalAccessException e) {
            log.warn("Failed to validate DTO fields for: {}", dto.getClass().getSimpleName(), e);
        }
    }
}