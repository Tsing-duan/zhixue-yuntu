package com.ape.apeadmin.controller;

import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.ResultCode;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * @description: 全局异常处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Result> handleUnauthenticated(UnauthenticatedException e) {
        logSecurityException("Unauthenticated", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.alert(ResultCode.COMMON_NO_LOGIN));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result> handleUnauthorized(UnauthorizedException e) {
        logSecurityException("Unauthorized", e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.alert(ResultCode.COMMON_NO_PERMIT));
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        logException("RuntimeException", e);
        return Result.fail(isAiRequest() ? "AI 请求未能处理，请检查输入或稍后再试。" : e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        logException("Exception", e);
        return Result.fail(isAiRequest() ? "AI 请求未能处理，请检查输入或稍后再试。" : e.getMessage());
    }

    private boolean isAiRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = attributes == null ? "" : attributes.getRequest().getRequestURI();
        return "/ai".equals(uri) || uri.startsWith("/ai/");
    }

    private void logException(String type, Exception e) {
        if (isAiRequest()) {
            log.error("AI 请求异常 -> type={}", e.getClass().getSimpleName());
            return;
        }
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes == null ? null : attributes.getRequest();
            if (request != null) {
                log.error("全局异常捕获 -> type={}, uri={}, method={}, msg={}", type, request.getRequestURI(), request.getMethod(), e.getMessage(), e);
            } else {
                log.error("全局异常捕获 -> type={}, msg={}", type, e.getMessage(), e);
            }
        } catch (Exception ex) {
            log.error("全局异常日志记录失败 -> type={}, msg={}", type, e.getMessage(), e);
        }
    }

    private void logSecurityException(String type, Exception e) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        if (request == null) {
            log.warn("安全校验拒绝 -> type={}", type);
            return;
        }
        log.warn("安全校验拒绝 -> type={}, uri={}, method={}", type, request.getRequestURI(), request.getMethod());
    }
}
