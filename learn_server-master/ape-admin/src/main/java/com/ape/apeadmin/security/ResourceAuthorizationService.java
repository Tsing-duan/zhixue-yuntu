package com.ape.apeadmin.security;

import com.ape.apecommon.constant.Constants;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeChapter;
import com.ape.apesystem.domain.ApeHomework;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.domain.ApeTest;
import com.ape.apesystem.domain.ApeTestItem;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeChapterService;
import com.ape.apesystem.service.ApeHomeworkService;
import com.ape.apesystem.service.ApeTaskService;
import com.ape.apesystem.service.ApeTestItemService;
import com.ape.apesystem.service.ApeTestService;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Central fail-closed ownership checks for teaching resources.
 */
@Component
public class ResourceAuthorizationService {

    @Autowired
    private ApeTaskService apeTaskService;
    @Autowired
    private ApeChapterService apeChapterService;
    @Autowired
    private ApeHomeworkService apeHomeworkService;
    @Autowired
    private ApeTestService apeTestService;
    @Autowired
    private ApeTestItemService apeTestItemService;

    public void requireTeacherAssignmentOrAdmin(String teacherId) {
        ApeUser current = requireCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        if (!isTeacher(current) || teacherId == null || !teacherId.equals(current.getId())) {
            deny();
        }
    }

    public void requireTaskOwnerOrAdmin(String taskId) {
        ApeUser current = requireCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        ApeTask task = taskId == null ? null : apeTaskService.getById(taskId);
        if (!isTeacher(current) || task == null || !current.getId().equals(task.getTeacherId())) {
            deny();
        }
    }

    public void requireChapterParentOwnerOrAdmin(String taskId) {
        requireTaskOwnerOrAdmin(taskId);
    }

    public void requireChapterOwnerOrAdmin(String chapterId) {
        ApeChapter chapter = chapterId == null ? null : apeChapterService.getById(chapterId);
        if (chapter == null) {
            deny();
        }
        requireTaskOwnerOrAdmin(chapter.getTaskId());
    }

    public void requireHomeworkParentOwnerOrAdmin(String chapterId) {
        requireChapterOwnerOrAdmin(chapterId);
    }

    public void requireHomeworkOwnerOrAdmin(String homeworkId) {
        ApeHomework homework = homeworkId == null ? null : apeHomeworkService.getById(homeworkId);
        if (homework == null) {
            deny();
        }
        requireChapterOwnerOrAdmin(homework.getChapterId());
    }

    public void requireTestParentOwnerOrAdmin(String taskId) {
        requireTaskOwnerOrAdmin(taskId);
    }

    public void requireTestOwnerOrAdmin(String testId) {
        ApeTest test = testId == null ? null : apeTestService.getById(testId);
        if (test == null) {
            deny();
        }
        requireTaskOwnerOrAdmin(test.getTaskId());
    }

    public void requireTestItemParentOwnerOrAdmin(String testId) {
        requireTestOwnerOrAdmin(testId);
    }

    public void requireTestItemOwnerOrAdmin(String itemId) {
        ApeTestItem item = itemId == null ? null : apeTestItemService.getById(itemId);
        if (item == null) {
            deny();
        }
        requireTestOwnerOrAdmin(item.getTestId());
    }

    private ApeUser requireCurrentUser() {
        ApeUser current = ShiroUtils.getUserInfo();
        if (current == null || current.getId() == null || current.getUserType() == null) {
            deny();
        }
        return current;
    }

    private boolean isAdmin(ApeUser user) {
        return user.getUserType() == Constants.USER_TYPE_ADMIN;
    }

    private boolean isTeacher(ApeUser user) {
        return user.getUserType() == Constants.USER_TYPE_TEACHER;
    }

    private void deny() {
        throw new UnauthorizedException("无权操作该教学资源");
    }
}
