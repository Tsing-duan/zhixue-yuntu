package com.ape.apeadmin.security;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apeadmin.controller.login.LoginController;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.utils.PasswordUtils;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeUserService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationSecurityTest {

    @Test
    void publicRegistrationMustNotCreateAdministrator() {
        ApeUserService userService = mock(ApeUserService.class);
        LoginController controller = new LoginController();
        ReflectionTestUtils.setField(controller, "apeUserService", userService);

        ApeUser requestedAdmin = new ApeUser()
                .setLoginAccount("newadmin")
                .setPassword("Admin123")
                .setUserType(Constants.USER_TYPE_ADMIN);

        Result result = controller.register(requestedAdmin);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("管理员");
        verify(userService, never()).save(requestedAdmin);
    }

    @Test
    void managementPortalMustRejectStudentAccount() {
        ApeUserService userService = mock(ApeUserService.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        LoginController controller = new LoginController();
        ReflectionTestUtils.setField(controller, "apeUserService", userService);
        ReflectionTestUtils.setField(controller, "eventPublisher", eventPublisher);

        ApeUser student = new ApeUser()
                .setId("student-1")
                .setLoginAccount("student-demo")
                .setPassword(PasswordUtils.encrypt("TestPassword123"))
                .setStatus(0)
                .setUserType(Constants.USER_TYPE_STUDENT);
        when(userService.getOne(any())).thenReturn(student);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        JSONObject credentials = new JSONObject();
        credentials.put("username", "student-demo");
        credentials.put("password", "TestPassword123");

        Result result = controller.login(request, credentials);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("学生账号").contains("学生端");
    }
}
