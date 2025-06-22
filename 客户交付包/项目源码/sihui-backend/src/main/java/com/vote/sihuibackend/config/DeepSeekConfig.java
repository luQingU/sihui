package com.vote.sihuibackend.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API配置类
 * 配置HTTP客户端和API相关参数
 */
@Configuration
public class DeepSeekConfig {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    @Value("${deepseek.api.temperature}")
    private Double temperature;

    @Value("${deepseek.api.max-tokens}")
    private Integer maxTokens;

    /**
     * 创建HTTP客户端Bean
     * 
     * @return 配置好的HTTP客户端
     */
    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000) // 连接超时10秒
                .setSocketTimeout(60000) // 读取超时60秒
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

    // Getters for configuration properties
    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getModel() {
        return model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }
}