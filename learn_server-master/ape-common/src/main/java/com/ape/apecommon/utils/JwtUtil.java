package com.ape.apecommon.utils;

/**
 * @description: JWT工具类
 */

import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.utils.spring.SpringUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JwtUtil {

    /** JWT 签名密钥由 Spring 启动配置注入，不提供源码默认值。 */
    private static final int MIN_JWT_SECRET_LENGTH = 32;
    private static volatile String jwtSecret;

    /**
     * Token过期时间 3 天（Redis 缓存为 6 天，实现滑动过期）
     */
    public static final long EXPIRE_TIME = 24 * 3 * 60 * 60 * 1000L;
    public static final int days = 3;
    private static StringRedisTemplate stringRedisTemplate;

    /**
     * 由启动配置调用。缺失或过短时直接阻止应用启动。
     */
    public static void configureJwtSecret(String configuredSecret) {
        if (configuredSecret == null || configuredSecret.trim().length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET is required and must contain at least " + MIN_JWT_SECRET_LENGTH + " characters");
        }
        jwtSecret = configuredSecret;
    }

    private static String requireJwtSecret() {
        if (jwtSecret == null) {
            throw new IllegalStateException("JWT secret is not initialized; set JWT_SECRET before starting the application");
        }
        return jwtSecret;
    }

    private static StringRedisTemplate getStringRedisTemplate() {
        if (stringRedisTemplate == null) {
            stringRedisTemplate = SpringUtils.getBean(StringRedisTemplate.class);
        }
        return stringRedisTemplate;
    }

    /**
     * @description: 校验token是否正确
     */
    public static boolean verify(String token, String userId) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(requireJwtSecret());
            JWTVerifier verifier = JWT.require(algorithm).withClaim("userId", userId).build();
            verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * @description: 获得token中的信息无需secret解密也能获得
     */
    public static String getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @description: 生成签名，指定时间后过期
     */
    public static String sign(String userId) {
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(requireJwtSecret());
        String token = JWT.create().withClaim("userId", userId).withExpiresAt(expireDate).sign(algorithm);
        getStringRedisTemplate().opsForValue().set(Constants.PREFIX_USER_TOKEN + userId, token, days * 2L, TimeUnit.DAYS);
        return token;
    }

    /**
     * @description: 根据request中的token获取用户账号
     */
    public static String getUserIdByToken(HttpServletRequest request) {
        String accessToken = getTokenByRequest(request);
        return getUserId(accessToken);
    }

    /**
     * @description: 获取 request 里传递的 token
     */
    public static String getTokenByRequest(HttpServletRequest request) {
        return request.getHeader(Constants.X_ACCESS_TOKEN);
    }
}
