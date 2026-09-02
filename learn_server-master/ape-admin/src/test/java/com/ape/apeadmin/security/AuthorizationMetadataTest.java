package com.ape.apeadmin.security;

import com.ape.apeadmin.controller.account.ApeAccountController;
import com.ape.apeadmin.controller.ai.AiController;
import com.ape.apeadmin.controller.dept.ApeDeptController;
import com.ape.apeadmin.controller.gen.ApeGenTableController;
import com.ape.apeadmin.controller.log.ApeLoginLogController;
import com.ape.apeadmin.controller.log.ApeOperLogController;
import com.ape.apeadmin.controller.param.ApeParamController;
import com.ape.apeadmin.controller.post.ApePostController;
import com.ape.apeadmin.controller.role.ApeRoleController;
import com.ape.apeadmin.controller.user.ApeUserController;
import com.ape.apeadmin.controller.chapter.ApeChapterController;
import com.ape.apeadmin.controller.homework.ApeHomeworkController;
import com.ape.apeadmin.controller.task.ApeTaskController;
import com.ape.apeadmin.controller.test.ApeTestController;
import com.ape.apeadmin.controller.test.ApeTestItemController;
import com.ape.apeadmin.controller.task.ApeTaskStudentController;
import com.ape.apeadmin.controller.homework.ApeHomeworkStudentController;
import com.ape.apeadmin.controller.test.ApeTestStudentController;
import com.alibaba.fastjson2.JSONObject;
import com.ape.apesystem.domain.ApeChapter;
import com.ape.apesystem.domain.ApeHomework;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.domain.ApeTest;
import com.ape.apesystem.domain.ApeTestItem;
import com.ape.apesystem.domain.ApeTaskStudent;
import com.ape.apesystem.domain.ApeHomeworkStudent;
import com.ape.apesystem.domain.ApeTestStudent;
import com.ape.apesystem.domain.ApeUser;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.Logical;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationMetadataTest {

    @Test
    void systemAdministrationControllersAreAdminOnly() {
        assertAdminOnly(ApeAccountController.class);
        assertAdminOnly(ApeDeptController.class);
        assertAdminOnly(ApeGenTableController.class);
        assertAdminOnly(ApeLoginLogController.class);
        assertAdminOnly(ApeOperLogController.class);
        assertAdminOnly(ApeParamController.class);
        assertAdminOnly(ApePostController.class);
        assertAdminOnly(ApeRoleController.class);
    }

    @Test
    void userAdministrationMethodsAreAdminOnly() throws Exception {
        assertAdminOnly(ApeUserController.class.getMethod("getUserPage", ApeUser.class));
        assertAdminOnly(ApeUserController.class.getMethod("getUserById", String.class));
        assertAdminOnly(ApeUserController.class.getMethod("saveUser", ApeUser.class));
        assertAdminOnly(ApeUserController.class.getMethod("editUser", ApeUser.class));
        assertAdminOnly(ApeUserController.class.getMethod("removeUser", String.class));
        assertAdminOnly(ApeUserController.class.getMethod("resetPassword", JSONObject.class));
    }

    @Test
    void roleSpecificAiMethodsDoNotGiveAdminImplicitStudentOrTeacherAccess() throws Exception {
        assertExactRoles(AiController.class.getMethod("studyPlan", JSONObject.class), "student");
        assertExactRoles(AiController.class.getMethod("wrongList"), "student");
        assertExactRoles(AiController.class.getMethod("wrongAgent", JSONObject.class), "student");
        assertAnyRole(AiController.class.getMethod("lessonPrepare", JSONObject.class), "admin", "teacher");
        assertAnyRole(AiController.class.getMethod("courseSummary", JSONObject.class), "admin", "teacher");
        assertAnyRole(AiController.class.getMethod("dataAgent", JSONObject.class), "admin", "teacher");
        assertAnyRole(AiController.class.getMethod("courseStats", String.class), "admin", "teacher");
        assertAnyRole(AiController.class.getMethod("examStats", String.class), "admin", "teacher");
        assertExactRoles(AiController.class.getMethod("getAiUsage"), "admin");
    }

    @Test
    void teachingResourceWritesRequireAdminOrTeacherRole() throws Exception {
        assertAnyRole(ApeTaskController.class.getMethod("saveApeTask", ApeTask.class), "admin", "teacher");
        assertAnyRole(ApeTaskController.class.getMethod("editApeTask", ApeTask.class), "admin", "teacher");
        assertAnyRole(ApeTaskController.class.getMethod("removeApeTask", String.class), "admin", "teacher");
        assertAnyRole(ApeChapterController.class.getMethod("saveApeChapter", ApeChapter.class), "admin", "teacher");
        assertAnyRole(ApeChapterController.class.getMethod("editApeChapter", ApeChapter.class), "admin", "teacher");
        assertAnyRole(ApeChapterController.class.getMethod("removeApeChapter", String.class), "admin", "teacher");
        assertAnyRole(ApeHomeworkController.class.getMethod("saveApeHomework", ApeHomework.class), "admin", "teacher");
        assertAnyRole(ApeHomeworkController.class.getMethod("editApeHomework", ApeHomework.class), "admin", "teacher");
        assertAnyRole(ApeHomeworkController.class.getMethod("removeApeHomework", String.class), "admin", "teacher");
        assertAnyRole(ApeTestController.class.getMethod("saveApeTest", ApeTest.class), "admin", "teacher");
        assertAnyRole(ApeTestController.class.getMethod("editApeTest", ApeTest.class), "admin", "teacher");
        assertAnyRole(ApeTestController.class.getMethod("removeApeTest", String.class), "admin", "teacher");
        assertAnyRole(ApeTestItemController.class.getMethod("saveApeTestItem", ApeTestItem.class), "admin", "teacher");
        assertAnyRole(ApeTestItemController.class.getMethod("editApeTestItem", ApeTestItem.class), "admin", "teacher");
        assertAnyRole(ApeTestItemController.class.getMethod("removeApeTestItem", String.class), "admin", "teacher");
    }

    @Test
    void enrollmentAndSubmissionEndpointsHaveExplicitRoleBoundaries() throws Exception {
        assertAnyRole(ApeTaskStudentController.class.getMethod("editApeTaskStudent", ApeTaskStudent.class), "admin", "teacher");
        assertAnyRole(ApeTaskStudentController.class.getMethod("removeApeTaskStudent", String.class), "admin", "teacher");
        assertExactRoles(ApeTaskStudentController.class.getMethod("saveApeTaskStudent", ApeTaskStudent.class), "student");

        assertExactRoles(ApeHomeworkStudentController.class.getMethod("saveApeHomeworkStudent", JSONObject.class), "student");
        assertExactRoles(ApeHomeworkStudentController.class.getMethod("editApeHomeworkStudent", ApeHomeworkStudent.class), "student");
        assertExactRoles(ApeHomeworkStudentController.class.getMethod("removeApeHomeworkStudent", String.class), "student");

        assertExactRoles(ApeTestStudentController.class.getMethod("saveApeTestStudent", JSONObject.class), "student");
        assertAnyRole(ApeTestStudentController.class.getMethod("editApeTestStudent", JSONObject.class), "admin", "teacher");
        assertAnyRole(ApeTestStudentController.class.getMethod("resetTestStudent", JSONObject.class), "admin", "teacher");
    }

    private void assertAdminOnly(Class<?> type) {
        RequiresRoles annotation = type.getAnnotation(RequiresRoles.class);
        assertThat(annotation).as(type.getName()).isNotNull();
        assertThat(annotation.value()).containsExactly("admin");
    }

    private void assertAdminOnly(Method method) {
        assertExactRoles(method, "admin");
    }

    private void assertExactRoles(Method method, String... expected) {
        RequiresRoles annotation = method.getAnnotation(RequiresRoles.class);
        assertThat(annotation).as(method.toGenericString()).isNotNull();
        assertThat(annotation.logical()).isEqualTo(Logical.AND);
        assertThat(Arrays.asList(annotation.value())).containsExactly(expected);
    }

    private void assertAnyRole(Method method, String... expected) {
        RequiresRoles annotation = method.getAnnotation(RequiresRoles.class);
        assertThat(annotation).as(method.toGenericString()).isNotNull();
        assertThat(annotation.logical()).isEqualTo(Logical.OR);
        assertThat(Arrays.asList(annotation.value())).containsExactly(expected);
    }
}
