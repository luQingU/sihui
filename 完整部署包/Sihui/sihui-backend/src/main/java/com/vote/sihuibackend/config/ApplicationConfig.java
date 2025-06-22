package com.vote.sihuibackend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 应用程序配置类
 * 
 * @author Sihui System
 * @since 1.0.0
 */
@Configuration
@EnableCaching
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class ApplicationConfig {

    // 其他配置Bean可以在这里添加

}