package com.ape.apeframework.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
/**

 * @description: 跨域
 */
@Configuration
public class CorsConfig {

    @Value("${APE_CORS_ALLOWED_ORIGINS:http://localhost:3001,http://127.0.0.1:3001,http://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOrigins;

    /**
    * @description: 配置跨域
    */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许cookies跨域
        corsConfiguration.setAllowCredentials(true);
        // #允许向该服务器提交请求的URI
        for (String origin : allowedOrigins.split(",")) {
            String trimmedOrigin = origin.trim();
            if (!trimmedOrigin.isEmpty()) {
                corsConfiguration.addAllowedOriginPattern(trimmedOrigin);
            }
        }
        // #允许访问的头信息,*表示全部，可以添加多个
        corsConfiguration.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        corsConfiguration.setMaxAge(1800L);
        // 允许提交请求的方法
        corsConfiguration.addAllowedMethod("OPTIONS");
        corsConfiguration.addAllowedMethod("GET");
        corsConfiguration.addAllowedMethod("POST");
        corsConfiguration.addAllowedMethod("PUT");
        corsConfiguration.addAllowedMethod("DELETE");
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }
}

