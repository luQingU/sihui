package com.vote.sihuibackend.aspect;

import com.vote.sihuibackend.service.PerformanceMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 性能监控AOP切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PerformanceMonitorAspect {

    private final PerformanceMonitorService performanceMonitorService;

    /**
     * 监控Service层方法执行时间
     */
    @Around("execution(* com.vote.sihuibackend.service.impl.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录执行时间
            performanceMonitorService.recordExecutionTime(methodName, executionTime);

            // 如果执行时间超过阈值，记录详细日志
            if (executionTime > 500) {
                log.warn("Slow service method execution: {} took {}ms", methodName, executionTime);
            } else if (executionTime > 100) {
                log.info("Service method execution: {} took {}ms", methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Service method {} failed after {}ms", methodName, executionTime, e);
            throw e;
        }
    }

    /**
     * 监控Controller层方法执行时间
     */
    @Around("execution(* com.vote.sihuibackend.controller.*.*(..))")
    public Object monitorControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录执行时间
            performanceMonitorService.recordExecutionTime("Controller." + methodName, executionTime);

            // 记录API响应时间
            if (executionTime > 1000) {
                log.warn("Slow API response: {} took {}ms", methodName, executionTime);
            } else if (executionTime > 200) {
                log.info("API response: {} took {}ms", methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Controller method {} failed after {}ms", methodName, executionTime, e);
            throw e;
        }
    }

    /**
     * 监控Repository层方法执行时间
     */
    @Around("execution(* com.vote.sihuibackend.repository.*.*(..))")
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录执行时间
            performanceMonitorService.recordExecutionTime("Repository." + methodName, executionTime);

            // 记录数据库查询时间
            if (executionTime > 300) {
                log.warn("Slow database query: {} took {}ms", methodName, executionTime);
            } else if (executionTime > 50) {
                log.debug("Database query: {} took {}ms", methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Repository method {} failed after {}ms", methodName, executionTime, e);
            throw e;
        }
    }
}