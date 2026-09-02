package com.ape.apeframework.config;

import com.ape.apecommon.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates and installs the externally supplied JWT signing secret while the
 * Spring context is starting. There is deliberately no fallback value.
 */
@Configuration
public class JwtSecretConfig {

    public JwtSecretConfig(@Value("${jwt.secret:}") String configuredSecret) {
        JwtUtil.configureJwtSecret(configuredSecret);
    }
}
