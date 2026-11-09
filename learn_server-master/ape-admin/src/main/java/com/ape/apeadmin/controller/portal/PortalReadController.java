package com.ape.apeadmin.controller.portal;

import com.ape.apecommon.domain.Result;
import com.ape.apesystem.domain.ApeAccount;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeAccountService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.*;

/** Authenticated portal reads. Management endpoints retain their administrator checks. */
@RestController
@RequestMapping("/portal")
@RequiresAuthentication
public class PortalReadController {
    private final ApeUserService users;
    private final ApeAccountService accounts;

    public PortalReadController(ApeUserService users, ApeAccountService accounts) {
        this.users = users;
        this.accounts = accounts;
    }

    @PostMapping("/teachers")
    public Result teachers(@RequestBody TeacherSearch search) {
        LambdaQueryWrapper<ApeUser> query = visibleTeachers()
                .like(StringUtils.isNotBlank(search.getUserName()), ApeUser::getUserName, search.getUserName())
                .eq(StringUtils.isNotBlank(search.getSchool()), ApeUser::getSchool, search.getSchool())
                .orderByAsc(ApeUser::getUserName, ApeUser::getId);
        Page<ApeUser> page = users.page(new Page<>(search.page(), search.size()), query);
        return Result.success(page.convert(TeacherView::from));
    }

    @GetMapping("/teachers/{id}")
    public Result teacher(@PathVariable String id) {
        ApeUser teacher = users.getOne(visibleTeachers().eq(ApeUser::getId, id));
        return teacher == null ? Result.fail("教师不存在或暂不可查看") : Result.success(TeacherView.from(teacher));
    }

    @PostMapping("/notices")
    public Result notices(@RequestBody PageSearch search) {
        // Closed notices and internal audit metadata are never exposed to the portal.
        LambdaQueryWrapper<ApeAccount> query = new LambdaQueryWrapper<ApeAccount>()
                .select(ApeAccount::getId, ApeAccount::getTitle, ApeAccount::getContent)
                .eq(ApeAccount::getState, 0)
                .orderByDesc(ApeAccount::getCreateTime, ApeAccount::getId);
        Page<ApeAccount> page = accounts.page(new Page<>(search.page(), search.size()), query);
        return Result.success(page.convert(row -> new NoticeView(row.getId(), row.getTitle(), row.getContent())));
    }

    private LambdaQueryWrapper<ApeUser> visibleTeachers() {
        // TableLogic also excludes deleted users. Client input cannot override type/status.
        return new LambdaQueryWrapper<ApeUser>()
                .select(ApeUser::getId, ApeUser::getUserName, ApeUser::getAvatar,
                        ApeUser::getSchool, ApeUser::getAgree, ApeUser::getRemark)
                .eq(ApeUser::getUserType, 1).eq(ApeUser::getStatus, 0);
    }

    @Data
    public static class PageSearch {
        private Integer pageNumber;
        private Integer pageSize;
        public long page() { return pageNumber == null ? 1 : Math.max(1, pageNumber); }
        public long size() { return pageSize == null ? 12 : Math.max(1, Math.min(60, pageSize)); }
    }

    @Data
    @lombok.EqualsAndHashCode(callSuper = true)
    public static class TeacherSearch extends PageSearch {
        private String userName;
        private String school;
    }

    @Value
    public static class TeacherView {
        String id;
        String userName;
        String avatar;
        String school;
        String agree;
        String remark;
        static TeacherView from(ApeUser user) {
            return new TeacherView(user.getId(), user.getUserName(), user.getAvatar(),
                    user.getSchool(), user.getAgree(), user.getRemark());
        }
    }

    @Value
    public static class NoticeView {
        String id;
        String title;
        String content;
    }
}
