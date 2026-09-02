package com.ape.apeadmin.validator;

import com.ape.apecommon.constant.Constants;
import com.ape.apesystem.domain.ApeUser;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 普通学生和教师账号密码规则；管理员演示账号不参与限制。
 */
public class AccountPasswordValidator {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Za-z0-9_]{6,10}$");
    private static final Pattern LETTER_PATTERN = Pattern.compile("[A-Za-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");

    private AccountPasswordValidator() {
    }

    public static String validateUserCredentials(ApeUser user) {
        if (user == null || isAdmin(user.getUserType())) {
            return null;
        }
        String accountError = validateAccount(user.getLoginAccount());
        if (accountError != null) {
            return accountError;
        }
        return validatePassword(user.getPassword());
    }

    public static String validatePasswordForUser(ApeUser user, String password) {
        if (user == null || isAdmin(user.getUserType())) {
            return null;
        }
        return validatePassword(password);
    }

    public static String validateAccountForUser(ApeUser user) {
        if (user == null || isAdmin(user.getUserType())) {
            return null;
        }
        return validateAccount(user.getLoginAccount());
    }

    private static boolean isAdmin(Integer userType) {
        return Integer.valueOf(Constants.USER_TYPE_ADMIN).equals(userType);
    }

    private static String validateAccount(String account) {
        if (StringUtils.isBlank(account)) {
            return "账号不能为空";
        }
        if (!ACCOUNT_PATTERN.matcher(account).matches()) {
            return "账号长度为6-10位，且只能包含字母、数字、下划线";
        }
        return null;
    }

    private static String validatePassword(String password) {
        if (StringUtils.isBlank(password)) {
            return "密码不能为空";
        }
        if (password.length() < 6 || password.length() > 10) {
            return "密码长度为6-10位";
        }
        if (!LETTER_PATTERN.matcher(password).find() || !DIGIT_PATTERN.matcher(password).find()) {
            return "密码至少包含字母和数字";
        }
        return null;
    }
}
