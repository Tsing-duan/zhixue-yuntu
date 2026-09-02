package com.ape.apecommon.utils;

import cn.hutool.crypto.digest.BCrypt;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 密码工具类 — 支持 MD5（旧格式兼容）和 BCrypt（新格式）
 */
public class PasswordUtils {

    /**
     * 生成 BCrypt 加密密码（新格式，推荐）
     */
    public static String encrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    /**
     * 验证 BCrypt 加密密码
     */
    public static boolean decrypt(String password, String hash) {
        if (!StringUtils.hasLength(password) || !StringUtils.hasLength(hash)) {
            return false;
        }
        if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
            return BCrypt.checkpw(password, hash);
        }
        return false;
    }

    /**
     * 生成 MD5 加盐密码（旧格式，仅用于迁移兼容）
     */
    public static String encryptMd5(String password) {
        String salt = UUID.randomUUID().toString().replace("-", "");
        String hash = DigestUtils.md5DigestAsHex((salt + password).getBytes());
        return salt + "$" + hash;
    }

    /**
     * 验证 MD5 加盐密码（旧格式，仅用于迁移兼容）
     */
    public static boolean decryptMd5(String password, String salt, String hash) {
        if (!StringUtils.hasLength(password) || !StringUtils.hasLength(salt)
                || !StringUtils.hasLength(hash) || hash.length() != 32) {
            return false;
        }
        String checkHash = DigestUtils.md5DigestAsHex((salt + password).getBytes());
        return hash.equals(checkHash);
    }

    /**
     * 判断 hash 是否为旧 MD5 格式
     */
    public static boolean isMd5Format(String hash) {
        return hash != null && hash.length() == 32 && !hash.startsWith("$");
    }
}
