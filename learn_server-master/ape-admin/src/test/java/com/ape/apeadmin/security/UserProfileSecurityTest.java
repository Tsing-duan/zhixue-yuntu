package com.ape.apeadmin.security;

import com.ape.apeadmin.controller.user.ApeUserController;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileSecurityTest {

    private ApeUserController controller;
    private ApeUserService userService;

    @BeforeEach
    void setUp() {
        ApeUser current = new ApeUser().setId("current-user").setUserType(2).setLoginAccount("student-demo");
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        SecurityUtils.setSecurityManager(securityManager);
        Subject subject = new DelegatingSubject(
                new SimplePrincipalCollection(current, "test"),
                true,
                null,
                null,
                securityManager);
        ThreadContext.bind(subject);

        controller = new ApeUserController();
        userService = mock(ApeUserService.class);
        when(userService.updateById(any())).thenReturn(true);
        ReflectionTestUtils.setField(controller, "apeUserService", userService);
    }

    @AfterEach
    void tearDown() {
        ThreadContext.remove();
    }

    @Test
    void profileUpdateUsesCurrentIdentityAndAllowlistedFieldsOnly() {
        ApeUser request = new ApeUser()
                .setId("victim-user")
                .setUserName("Demo Student")
                .setEmail("demo@example.invalid")
                .setPassword("attacker-password")
                .setSalt("attacker-salt")
                .setLoginAccount("admin")
                .setUserType(0)
                .setStatus(1)
                .setDelFlag(1)
                .setRoleIds(Collections.singletonList("admin-role"));

        controller.setUserInfo(request);

        ArgumentCaptor<ApeUser> captor = ArgumentCaptor.forClass(ApeUser.class);
        verify(userService).updateById(captor.capture());
        ApeUser update = captor.getValue();
        assertThat(update.getId()).isEqualTo("current-user");
        assertThat(update.getUserName()).isEqualTo("Demo Student");
        assertThat(update.getEmail()).isEqualTo("demo@example.invalid");
        assertThat(update.getPassword()).isNull();
        assertThat(update.getSalt()).isNull();
        assertThat(update.getLoginAccount()).isNull();
        assertThat(update.getUserType()).isNull();
        assertThat(update.getStatus()).isNull();
        assertThat(update.getDelFlag()).isNull();
        assertThat(update.getRoleIds()).isNull();
    }
}
