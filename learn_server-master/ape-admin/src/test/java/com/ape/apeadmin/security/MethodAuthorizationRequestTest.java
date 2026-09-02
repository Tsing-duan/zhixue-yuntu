package com.ape.apeadmin.security;

import com.ape.apeadmin.controller.GlobalExceptionHandler;
import com.ape.apeadmin.controller.role.ApeRoleController;
import com.ape.apeadmin.controller.task.ApeTaskController;
import com.ape.apeadmin.controller.task.ApeTaskStudentController;
import com.ape.apecommon.constant.Constants;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeRoleService;
import com.ape.apesystem.service.ApeTaskService;
import com.ape.apesystem.service.ApeTaskStudentService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MethodAuthorizationRequestTest {

    private MockMvc mockMvc;
    private DefaultSecurityManager securityManager;

    @BeforeEach
    void setUp() {
        securityManager = new DefaultSecurityManager(new UserTypeRealm());
        SecurityUtils.setSecurityManager(securityManager);

        ApeRoleController roleController = new ApeRoleController();
        ApeRoleService roleService = mock(ApeRoleService.class);
        when(roleService.list(any())).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(roleController, "apeRoleService", roleService);

        ApeTaskController taskController = new ApeTaskController();
        ApeTaskService taskService = mock(ApeTaskService.class);
        when(taskService.list(any())).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(taskController, "apeTaskService", taskService);

        ApeTaskStudentController taskStudentController = new ApeTaskStudentController();
        ApeTaskStudentService taskStudentService = mock(ApeTaskStudentService.class);
        when(taskStudentService.list(any())).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(taskStudentController, "apeTaskStudentService", taskStudentService);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        secureProxy(roleController),
                        secureProxy(taskController),
                        secureProxy(taskStudentController))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        Subject subject = ThreadContext.unbindSubject();
        if (subject != null) {
            subject.logout();
        }
        ThreadContext.unbindSecurityManager();
    }

    @Test
    void anonymousRequestToAdminApiReturns401() throws Exception {
        bindAnonymous();

        mockMvc.perform(post("/role/getRoleList")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1002));
    }

    @Test
    void studentRequestToAdminApiReturns403() throws Exception {
        login(Constants.USER_TYPE_STUDENT);

        mockMvc.perform(post("/role/getRoleList")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void teacherRequestToAdminApiReturns403() throws Exception {
        login(Constants.USER_TYPE_TEACHER);

        mockMvc.perform(post("/role/getRoleList")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void studentRequestToTeacherApiReturns403() throws Exception {
        login(Constants.USER_TYPE_STUDENT);

        mockMvc.perform(get("/task/getApeTaskByTeacherId"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void adminDoesNotImplicitlyInheritTeacherRole() throws Exception {
        login(Constants.USER_TYPE_ADMIN);

        mockMvc.perform(get("/task/getApeTaskByTeacherId"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void adminDoesNotImplicitlyInheritStudentRole() throws Exception {
        login(Constants.USER_TYPE_ADMIN);

        mockMvc.perform(post("/student/getApeMyTaskList")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void adminRequestToAdminApiSucceeds() throws Exception {
        login(Constants.USER_TYPE_ADMIN);

        mockMvc.perform(post("/role/getRoleList")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void teacherRequestToTeacherApiSucceeds() throws Exception {
        login(Constants.USER_TYPE_TEACHER);

        mockMvc.perform(get("/task/getApeTaskByTeacherId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void studentRequestToStudentApiSucceeds() throws Exception {
        login(Constants.USER_TYPE_STUDENT);

        mockMvc.perform(post("/student/getApeMyTaskList")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    private Object secureProxy(Object target) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvisor(advisor);
        return proxyFactory.getProxy();
    }

    private void bindAnonymous() {
        ThreadContext.bind(new Subject.Builder(securityManager).buildSubject());
    }

    private void login(int userType) {
        ApeUser user = new ApeUser()
                .setId("test-user-" + userType)
                .setLoginAccount("test-user-" + userType)
                .setUserType(userType)
                .setStatus(0);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        ThreadContext.bind(subject);
        subject.login(new ApeUserAuthenticationToken(user));
    }

    private static final class ApeUserAuthenticationToken implements AuthenticationToken {
        private final ApeUser user;

        private ApeUserAuthenticationToken(ApeUser user) {
            this.user = user;
        }

        @Override
        public Object getPrincipal() {
            return user;
        }

        @Override
        public Object getCredentials() {
            return "test-credential";
        }
    }

    private static final class UserTypeRealm extends AuthorizingRealm {
        @Override
        public boolean supports(AuthenticationToken token) {
            return token instanceof ApeUserAuthenticationToken;
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            ApeUser user = (ApeUser) principals.getPrimaryPrincipal();
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            if (user != null && user.getUserType() != null) {
                if (user.getUserType() == Constants.USER_TYPE_ADMIN) {
                    info.addRole(Constants.ROLE_KEY_ADMIN);
                } else if (user.getUserType() == Constants.USER_TYPE_TEACHER) {
                    info.addRole(Constants.ROLE_KEY_TEACHER);
                } else if (user.getUserType() == Constants.USER_TYPE_STUDENT) {
                    info.addRole(Constants.ROLE_KEY_STUDENT);
                }
            }
            return info;
        }

        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
            ApeUser user = (ApeUser) token.getPrincipal();
            return new SimpleAuthenticationInfo(user, token.getCredentials(), getName());
        }
    }
}
