package com.ape.apeadmin.security;

import com.ape.apeadmin.controller.portal.PortalReadController;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.domain.ApeAccount;
import com.ape.apesystem.service.ApeUserService;
import com.ape.apesystem.service.ApeAccountService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PortalReadSecurityTest {
    @BeforeAll
    static void initializeMappings() {
        MapperBuilderAssistant builder = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        builder.setCurrentNamespace("portal-test");
        TableInfoHelper.initTableInfo(builder, ApeUser.class);
        TableInfoHelper.initTableInfo(builder, ApeAccount.class);
    }
    private final ApeUserService users = mock(ApeUserService.class);
    private final ApeAccountService notices = mock(ApeAccountService.class);
    private final PortalReadController controller = new PortalReadController(users, notices);
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void teacherProjectionCannotSerializePrivateAccountData() throws Exception {
        ApeUser teacher = new ApeUser().setId("teacher").setUserName("Teacher")
                .setAvatar("avatar.png").setSchool("School").setAgree("Lecturer").setRemark("Intro")
                .setLoginAccount("private-login").setTel("private-phone").setEmail("private-email")
                .setAddress("private-address").setPassword("secret").setSalt("salt")
                .setLoginIp("private-ip").setRoleIds(Collections.singletonList("private-role"));
        when(users.getOne(any())).thenReturn(teacher);
        assertThat(json.readTree(json.writeValueAsString(controller.teacher("teacher").getData())).size()).isEqualTo(6);
        String detail = json.writeValueAsString(controller.teacher("teacher").getData());
        assertThat(detail).contains("Teacher", "Intro").doesNotContain("private", "secret", "salt", "password", "roleIds");
        Page<ApeUser> page = new Page<>();page.setRecords(Collections.singletonList(teacher));
        when(users.page(any(Page.class), any())).thenReturn(page);
        String list = json.writeValueAsString(controller.teachers(new PortalReadController.TeacherSearch()).getData());
        assertThat(list).contains("Teacher").doesNotContain("private", "secret", "salt", "password", "roleIds");
        verify(users, never()).updateById(any());
    }

    @Test
    void noticeProjectionOmitsAuditMetadataAndMissingTeachersFailClosed() throws Exception {
        ApeAccount notice = new ApeAccount().setId("notice").setTitle("Title").setContent("Content");
        notice.setCreateBy("private-author");notice.setUpdateBy("private-editor");
        Page<ApeAccount> page = new Page<>();page.setRecords(Collections.singletonList(notice));
        when(notices.page(any(Page.class), any())).thenReturn(page);
        String response = json.writeValueAsString(controller.notices(new PortalReadController.PageSearch()).getData());
        assertThat(response).contains("Title", "Content").doesNotContain("private", "createBy", "updateBy");
        assertThat(controller.teacher("missing").isSuccess()).isFalse();
    }

    @Test
    void paginationDefaultsAndBoundsPreventUnboundedReads() {
        PortalReadController.PageSearch search = new PortalReadController.PageSearch();
        assertThat(search.page()).isEqualTo(1);assertThat(search.size()).isEqualTo(12);
        search.setPageNumber(-5);search.setPageSize(Integer.MAX_VALUE);
        assertThat(search.page()).isEqualTo(1);assertThat(search.size()).isEqualTo(60);
        search.setPageSize(-1);assertThat(search.size()).isEqualTo(1);
    }
}
