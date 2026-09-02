package com.ape.apeadmin.controller.task;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeadmin.security.ResourceAuthorizationService;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.*;
import com.ape.apesystem.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.Logical;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 课程controller
 */
@Controller
@ResponseBody
@RequestMapping("task")
public class ApeTaskController {

    @Autowired
    private ApeTaskService apeTaskService;
    @Autowired
    private ApeUserService apeUserService;
    @Autowired
    private ApeChapterService apeChapterService;
    @Autowired
    private ApeTaskCommentService apeTaskCommentService;
    @Autowired
    private ApeTaskStudentService apeTaskStudentService;
    @Autowired
    private ApeTestService apeTestService;
    @Autowired
    private ApeTestStudentService apeTestStudentService;
    @Autowired
    private ApeArticleService apeArticleService;
    @Autowired
    private ApeArticleFavorService apeArticleFavorService;
    @Autowired
    private ApeArticleCommentService apeArticleCommentService;
    @Autowired
    private ApeChapterVideoService apeChapterVideoService;
    @Autowired
    private ApeHomeworkService apeHomeworkService;
    @Autowired
    private ApeHomeworkStudentService apeHomeworkStudentService;
    @Autowired
    private ApeTaskFavorService apeTaskFavorService;
    @Autowired
    private ResourceAuthorizationService resourceAuthorizationService;

    /** 分页获取课程 */
    @Log(name = "分页获取课程", type = BusinessType.OTHER)
    @PostMapping("getApeTaskPage")
    public Result getApeTaskPage(@RequestBody ApeTask apeTask) {
        if (apeTask.getType() != null && apeTask.getType() == 1) {
            apeTask.setTeacherId(ShiroUtils.getUserInfo().getId());
        }
        Page<ApeTask> page = new Page<>(apeTask.getPageNumber(),apeTask.getPageSize());
        QueryWrapper<ApeTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeTask.getName()),ApeTask::getName,apeTask.getName())
                .like(StringUtils.isNotBlank(apeTask.getTaskDescribe()),ApeTask::getTaskDescribe,apeTask.getTaskDescribe())
                .like(StringUtils.isNotBlank(apeTask.getTeacherName()),ApeTask::getTeacherName,apeTask.getTeacherName())
                .eq(StringUtils.isNotBlank(apeTask.getTeacherId()),ApeTask::getTeacherId,apeTask.getTeacherId())
                .eq(apeTask.getState() != null,ApeTask::getState,apeTask.getState())
                .like(StringUtils.isNotBlank(apeTask.getMajor()),ApeTask::getMajor,apeTask.getMajor())
                .like(StringUtils.isNotBlank(apeTask.getClassification()),ApeTask::getClassification,apeTask.getClassification());
        Page<ApeTask> apeTaskPage = apeTaskService.page(page, queryWrapper);
        // 为每个课程填充收藏数
        for (ApeTask task : apeTaskPage.getRecords()) {
            int favorCount = (int) apeTaskFavorService.count(
                    new QueryWrapper<ApeTaskFavor>().lambda()
                            .eq(ApeTaskFavor::getTaskId, task.getId()));
            task.setFavorCount(favorCount);
        }
        return Result.success(apeTaskPage);
    }

    /** 根据id获取课程 */
    @Log(name = "根据id获取课程", type = BusinessType.OTHER)
    @GetMapping("getApeTaskById")
    public Result getApeTaskById(@RequestParam("id")String id) {
        ApeTask apeTask = apeTaskService.getById(id);
        // 查询收藏数
        int favorCount = (int) apeTaskFavorService.count(
                new QueryWrapper<ApeTaskFavor>().lambda().eq(ApeTaskFavor::getTaskId, id));
        apeTask.setFavorCount(favorCount);
        // 查询当前用户是否已收藏
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (userInfo != null) {
            int count = (int) apeTaskFavorService.count(
                    new QueryWrapper<ApeTaskFavor>().lambda()
                            .eq(ApeTaskFavor::getTaskId, id)
                            .eq(ApeTaskFavor::getUserId, userInfo.getId()));
            apeTask.setIsFavorite(count > 0 ? 1 : 0);
        } else {
            apeTask.setIsFavorite(0);
        }
        return Result.success(apeTask);
    }

    @GetMapping("getApeTaskByTeacher")
    public Result getApeTaskByTeacher(@RequestParam("id")String id) {
        QueryWrapper<ApeTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeTask::getTeacherId,id);
        List<ApeTask> taskList = apeTaskService.list(queryWrapper);
        return Result.success(taskList);
    }

    @GetMapping("getApeTaskByTeacherId")
    @RequiresRoles("teacher")
    public Result getApeTaskByTeacherId() {
        QueryWrapper<ApeTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeTask::getTeacherId,ShiroUtils.getUserInfo().getId());
        List<ApeTask> taskList = apeTaskService.list(queryWrapper);
        return Result.success(taskList);
    }

    /** 获取课程列表 */
    @Log(name = "获取课程列表", type = BusinessType.OTHER)
    @GetMapping("getApeTaskList")
    public Result getApeTaskList() {
        List<ApeTask> taskList = apeTaskService.list();
        return Result.success(taskList);
    }

    /** 保存课程 */
    @Log(name = "保存课程", type = BusinessType.INSERT)
    @PostMapping("saveApeTask")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result saveApeTask(@RequestBody ApeTask apeTask) {
        ApeUser currentUser = ShiroUtils.getUserInfo();
        if (currentUser.getUserType() != null && currentUser.getUserType() == 1) {
            apeTask.setTeacherId(ShiroUtils.getUserInfo().getId());
        }
        resourceAuthorizationService.requireTeacherAssignmentOrAdmin(apeTask.getTeacherId());
        ApeUser apeUser = apeUserService.getById(apeTask.getTeacherId());
        if (apeUser != null) {
            apeTask.setTeacherName(apeUser.getUserName());
        }
        boolean save = apeTaskService.save(apeTask);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑课程 */
    @Log(name = "编辑课程", type = BusinessType.UPDATE)
    @PostMapping("editApeTask")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result editApeTask(@RequestBody ApeTask apeTask) {
        resourceAuthorizationService.requireTaskOwnerOrAdmin(apeTask.getId());
        ApeUser currentUser = ShiroUtils.getUserInfo();
        if (currentUser.getUserType() != null && currentUser.getUserType() == 1) {
            apeTask.setTeacherId(currentUser.getId());
        }
        if (StringUtils.isNotBlank(apeTask.getTeacherId())) {
            ApeUser apeUser = apeUserService.getById(apeTask.getTeacherId());
            if (apeUser != null) {
                apeTask.setTeacherName(apeUser.getUserName());
            }
        }
        boolean save = apeTaskService.updateById(apeTask);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除课程 */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("removeApeTask")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    @Log(name = "删除课程", type = BusinessType.DELETE)
    public Result removeApeTask(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                resourceAuthorizationService.requireTaskOwnerOrAdmin(id);
                apeTaskService.removeById(id);
                QueryWrapper<ApeTaskComment> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ApeTaskComment::getTaskId,id);
                apeTaskCommentService.remove(queryWrapper);
                QueryWrapper<ApeTaskStudent> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(ApeTaskStudent::getTaskId,id);
                apeTaskStudentService.remove(queryWrapper1);
                QueryWrapper<ApeChapter> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.lambda().eq(ApeChapter::getTaskId,id);
                List<ApeChapter> chapterList = apeChapterService.list(queryWrapper2);
                for (ApeChapter chapter : chapterList) {
                    apeChapterService.removeById(chapter.getId());
                    QueryWrapper<ApeChapterVideo> queryWrapper3 = new QueryWrapper<>();
                    queryWrapper3.lambda().eq(ApeChapterVideo::getChapterId,chapter.getId());
                    apeChapterVideoService.remove(queryWrapper3);
                    QueryWrapper<ApeHomework> queryWrapper4 = new QueryWrapper<>();
                    queryWrapper4.lambda().eq(ApeHomework::getChapterId,chapter.getId());
                    apeHomeworkService.remove(queryWrapper4);
                    QueryWrapper<ApeHomeworkStudent> queryWrapper5 = new QueryWrapper<>();
                    queryWrapper5.lambda().eq(ApeHomeworkStudent::getChapterId,chapter.getId());
                    apeHomeworkStudentService.remove(queryWrapper5);
                }
                QueryWrapper<ApeArticle> queryWrapper3 = new QueryWrapper<>();
                queryWrapper3.lambda().eq(ApeArticle::getTaskId,id);
                List<ApeArticle> articleList = apeArticleService.list(queryWrapper3);
                for (ApeArticle apeArticle : articleList) {
                    apeArticleService.removeById(apeArticle.getId());
                    QueryWrapper<ApeArticleFavor> queryWrapper4 = new QueryWrapper<>();
                    queryWrapper4.lambda().eq(ApeArticleFavor::getArticleId,apeArticle.getId());
                    apeArticleFavorService.remove(queryWrapper4);
                    QueryWrapper<ApeArticleComment> queryWrapper5 = new QueryWrapper<>();
                    queryWrapper5.lambda().eq(ApeArticleComment::getTaskId,apeArticle.getId());
                    apeArticleCommentService.remove(queryWrapper5);
                }
                QueryWrapper<ApeTest> queryWrapper5 = new QueryWrapper<>();
                queryWrapper5.lambda().eq(ApeTest::getTaskId,id);
                List<ApeTest> testList = apeTestService.list(queryWrapper5);
                for (ApeTest apeTest : testList) {
                    apeTestService.removeById(apeTest.getId());
                    QueryWrapper<ApeTestStudent> queryWrapper6 = new QueryWrapper<>();
                    queryWrapper6.lambda().eq(ApeTestStudent::getTestId,apeTest.getId());
                    apeTestStudentService.remove(queryWrapper6);
                }
            }
            return Result.success();
        } else {
            return Result.fail("课程id不能为空！");
        }
    }

}
