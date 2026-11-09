package com.ape.apecommon.constant;

import io.jsonwebtoken.Claims;

/**

 * @description: 公共字符串
 */
public class Constants {

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 用户ID
     */
    public static final String JWT_USERID = "userid";

    /**
     * 用户名称
     */
    public static final String JWT_USERNAME = Claims.SUBJECT;

    /**
     * 用户头像
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * 创建时间
     */
    public static final String JWT_CREATED = "created";

    /**
     * 用户权限
     */
    public static final String JWT_AUTHORITIES = "authorities";

    public static final String PREFIX_USER_TOKEN = "user_token:";

    public static final String X_ACCESS_TOKEN = "x_access_token";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 远程方法调用
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 远程方法调用
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 远程方法调用
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加）
     */
    public static final String[] JOB_WHITELIST_STR = { "com.ape" };

    /**
     * 定时任务违规的字符
     */
    public static final String[] JOB_ERROR_STR = { "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.ape.common.utils.file", "com.ape.common.config" };

    /** 角色 Key 常量 */
    public static final String ROLE_KEY_ADMIN = "admin";
    public static final String ROLE_KEY_TEACHER = "teacher";
    public static final String ROLE_KEY_STUDENT = "student";

    /** 用户类型常量 */
    public static final int USER_TYPE_ADMIN = 0;
    public static final int USER_TYPE_TEACHER = 1;
    public static final int USER_TYPE_STUDENT = 2;

    /** 默认头像路径（后续迁移到 ape_param 参数表） */
    public static final String DEFAULT_ADMIN_AVATAR = "/img/26001.jpeg";
    public static final String DEFAULT_TEACHER_MALE_AVATAR = "/img/26001.jpeg";
    public static final String DEFAULT_TEACHER_FEMALE_AVATAR = "/img/26003.jpeg";
    public static final String DEFAULT_STUDENT_MALE_AVATAR = "/img/xiaoxi.jpeg";
    public static final String DEFAULT_STUDENT_FEMALE_AVATAR = "/img/xiaoan.jpeg";

    /** 题型常量（与 ape_dict_data 题型字典对应） */
    public static final int QUESTION_TYPE_SINGLE = 0;
    public static final int QUESTION_TYPE_MULTI = 1;
    public static final int QUESTION_TYPE_FILL = 2;
    public static final int QUESTION_TYPE_JUDGE = 3;
    public static final int QUESTION_TYPE_QA = 4;
    public static final int QUESTION_TYPE_CALC = 5;

    /** 考试重置后的单人补考授权标记 */
    public static final String TEST_RETAKE_GRANT_REMARK = "RETAKE_GRANT";
    public static final String TEST_RETAKE_GRANT_ITEM_ID = "__RETAKE_GRANT__";

    public static String getQuestionTypeName(int type) {
        switch (type) {
            case QUESTION_TYPE_SINGLE: return "单选题";
            case QUESTION_TYPE_MULTI: return "多选题";
            case QUESTION_TYPE_FILL: return "填空题";
            case QUESTION_TYPE_JUDGE: return "判断题";
            case QUESTION_TYPE_QA: return "问答题";
            case QUESTION_TYPE_CALC: return "计算题";
            default: return "未知题型";
        }
    }

}
