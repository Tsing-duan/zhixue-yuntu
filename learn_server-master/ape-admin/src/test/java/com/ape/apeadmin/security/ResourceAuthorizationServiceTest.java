package com.ape.apeadmin.security;

import com.ape.apeadmin.security.ResourceAuthorizationService;
import com.ape.apecommon.constant.Constants;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeTaskService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceAuthorizationServiceTest {

    @AfterEach
    void tearDown() {
        ThreadContext.remove();
    }

    @Test
    void teacherCanWriteOnlyOwnTask() {
        ResourceAuthorizationService service = serviceWithTask(
                new ApeTask().setId("task-1").setTeacherId("teacher-owner"));
        bindUser("teacher-owner", Constants.USER_TYPE_TEACHER);

        assertThatCode(() -> service.requireTaskOwnerOrAdmin("task-1")).doesNotThrowAnyException();

        bindUser("other-teacher", Constants.USER_TYPE_TEACHER);
        assertThatThrownBy(() -> service.requireTaskOwnerOrAdmin("task-1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void missingResourceAndNonTeacherFailClosed() {
        ResourceAuthorizationService missing = serviceWithTask(null);
        bindUser("teacher-owner", Constants.USER_TYPE_TEACHER);
        assertThatThrownBy(() -> missing.requireTaskOwnerOrAdmin("missing"))
                .isInstanceOf(UnauthorizedException.class);

        ResourceAuthorizationService existing = serviceWithTask(
                new ApeTask().setId("task-1").setTeacherId("teacher-owner"));
        bindUser("student", Constants.USER_TYPE_STUDENT);
        assertThatThrownBy(() -> existing.requireTaskOwnerOrAdmin("task-1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void adminCanWriteTaskWithoutInheritingTeacherRole() {
        ResourceAuthorizationService service = serviceWithTask(
                new ApeTask().setId("task-1").setTeacherId("teacher-owner"));
        bindUser("admin", Constants.USER_TYPE_ADMIN);

        assertThatCode(() -> service.requireTaskOwnerOrAdmin("task-1")).doesNotThrowAnyException();
    }

    private ResourceAuthorizationService serviceWithTask(ApeTask task) {
        ResourceAuthorizationService service = new ResourceAuthorizationService();
        ApeTaskService taskService = mock(ApeTaskService.class);
        when(taskService.getById("task-1")).thenReturn(task);
        when(taskService.getById("missing")).thenReturn(null);
        ReflectionTestUtils.setField(service, "apeTaskService", taskService);
        return service;
    }

    private void bindUser(String id, int userType) {
        ThreadContext.remove();
        ApeUser current = new ApeUser().setId(id).setUserType(userType).setLoginAccount(id);
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        SecurityUtils.setSecurityManager(securityManager);
        Subject subject = new DelegatingSubject(
                new SimplePrincipalCollection(current, "test"),
                true,
                null,
                null,
                securityManager);
        ThreadContext.bind(subject);
    }
}
