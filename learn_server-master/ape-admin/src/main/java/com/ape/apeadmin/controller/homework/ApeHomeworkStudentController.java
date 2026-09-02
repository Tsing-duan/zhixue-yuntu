package com.ape.apeadmin.controller.homework;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeadmin.security.ResourceAuthorizationService;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeChapter;
import com.ape.apesystem.domain.ApeHomework;
import com.ape.apesystem.domain.ApeHomeworkStudent;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeChapterService;
import com.ape.apesystem.service.ApeHomeworkService;
import com.ape.apesystem.service.ApeHomeworkStudentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 学生作业controller
 */
@Controller
@ResponseBody
@RequestMapping("student")
public class ApeHomeworkStudentController {

    @Autowired
    private ApeHomeworkStudentService apeHomeworkStudentService;
    @Autowired
    private ApeHomeworkService apeHomeworkService;
    @Autowired
    private ApeChapterService apeChapterService;
    @Autowired
    private com.ape.apesystem.service.ApeTestStudentService apeTestStudentService;
    @Autowired
    private ResourceAuthorizationService resourceAuthorizationService;

    /** 分页获取学生作业 */
    @Log(name = "分页获取学生作业", type = BusinessType.OTHER)
    @PostMapping("getApeHomeworkStudentPage")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result getApeHomeworkStudentPage(@RequestBody ApeHomeworkStudent apeHomeworkStudent) {
        resourceAuthorizationService.requireHomeworkParentOwnerOrAdmin(apeHomeworkStudent.getChapterId());
        Page<ApeHomeworkStudent> page = new Page<>(apeHomeworkStudent.getPageNumber(),apeHomeworkStudent.getPageSize());
        QueryWrapper<ApeHomeworkStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeHomeworkStudent.getChapterId()),ApeHomeworkStudent::getChapterId,apeHomeworkStudent.getChapterId())
                .eq(StringUtils.isNotBlank(apeHomeworkStudent.getWorkId()),ApeHomeworkStudent::getWorkId,apeHomeworkStudent.getWorkId())
                .eq(StringUtils.isNotBlank(apeHomeworkStudent.getTitle()),ApeHomeworkStudent::getTitle,apeHomeworkStudent.getTitle())
                .eq(StringUtils.isNotBlank(apeHomeworkStudent.getUserId()),ApeHomeworkStudent::getUserId,apeHomeworkStudent.getUserId());
        Page<ApeHomeworkStudent> apeHomeworkStudentPage = apeHomeworkStudentService.page(page, queryWrapper);
        return Result.success(apeHomeworkStudentPage);
    }

    @GetMapping("getApeHomeworkStudentFlag")
    @RequiresRoles("student")
    public Result getApeHomeworkStudentFlag(@RequestParam("id") String id) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        QueryWrapper<ApeHomeworkStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeHomeworkStudent::getChapterId,id)
                .eq(ApeHomeworkStudent::getUserId,userInfo.getId());
        int count = apeHomeworkStudentService.count(queryWrapper);
        if (count > 0) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    @GetMapping("getHomeworkStudentFlag")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result getHomeworkStudentFlag(@RequestParam("id") String id,@RequestParam("userId")String userId) {
        resourceAuthorizationService.requireHomeworkParentOwnerOrAdmin(id);
        ApeUser userInfo = ShiroUtils.getUserInfo();
        QueryWrapper<ApeHomeworkStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeHomeworkStudent::getChapterId,id)
                .eq(ApeHomeworkStudent::getUserId,userId);
        int count = apeHomeworkStudentService.count(queryWrapper);
        if (count > 0) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    @GetMapping("getMyApeHomework")
    @RequiresRoles("student")
    public Result getMyApeHomework() {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        QueryWrapper<ApeHomeworkStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeHomeworkStudent::getUserId, userInfo.getId())
                .orderByAsc(ApeHomeworkStudent::getChapterId);
        List<ApeHomeworkStudent> allList = apeHomeworkStudentService.list(queryWrapper);
        // 按 chapterId 去重，保留每个章节最新记录为代表
        Map<String, ApeHomeworkStudent> chapterMap = new LinkedHashMap<>();
        for (ApeHomeworkStudent s : allList) {
            if (!chapterMap.containsKey(s.getChapterId())) {
                chapterMap.put(s.getChapterId(), s);
            }
        }
        List<ApeHomeworkStudent> result = new ArrayList<>(chapterMap.values());
        for (ApeHomeworkStudent student : result) {
            if (StringUtils.isNotBlank(student.getChapterId())) {
                ApeChapter chapter = apeChapterService.getById(student.getChapterId());
                if (chapter != null) {
                    student.setTaskName(chapter.getTaskName());
                }
            }
        }
        return Result.success(result);
    }

    @PostMapping("getApeHomeworkStudentList")
    @RequiresRoles("student")
    public Result getApeHomeworkStudentList(@RequestBody ApeHomeworkStudent apeHomeworkStudent) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        QueryWrapper<ApeHomework> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeHomework::getChapterId,apeHomeworkStudent.getChapterId()).orderByAsc(ApeHomework::getSort);
        List<ApeHomework> homeworkList = apeHomeworkService.list(queryWrapper);
        List<ApeHomeworkStudent> homeworkStudentList = new ArrayList<>();
        for (ApeHomework homework : homeworkList) {
            QueryWrapper<ApeHomeworkStudent> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeHomeworkStudent::getUserId,userInfo.getId())
                            .eq(ApeHomeworkStudent::getWorkId,homework.getId());
            ApeHomeworkStudent homeworkStudent = apeHomeworkStudentService.getOne(wrapper);
            if (homeworkStudent == null) {
                homeworkStudent = new ApeHomeworkStudent();
                homeworkStudent.setChapterId(homework.getChapterId());
                homeworkStudent.setChapterName(homework.getChapterName());
                homeworkStudent.setWorkId(homework.getId());
                homeworkStudent.setTitle(homework.getTitle());
                homeworkStudent.setSort(homework.getSort());
                homeworkStudent.setAnswer(homework.getAnswer());
                homeworkStudent.setType(homework.getType());
                homeworkStudent.setScore(homework.getScore());
                homeworkStudent.setContent(homework.getContent());
                homeworkStudent.setUserId(userInfo.getId());
                homeworkStudent.setSolution("");
                homeworkStudent.setPoint(0);
            }
            homeworkStudentList.add(homeworkStudent);
        }
        return Result.success(homeworkStudentList);
    }



    /** 根据id获取学生作业 */
    @Log(name = "根据id获取学生作业", type = BusinessType.OTHER)
    @GetMapping("getApeHomeworkStudentById")
    @RequiresRoles("student")
    public Result getApeHomeworkStudentById(@RequestParam("id")String id) {
        ApeHomeworkStudent apeHomeworkStudent = apeHomeworkStudentService.getById(id);
        requireCurrentStudentOwns(apeHomeworkStudent);
        return Result.success(apeHomeworkStudent);
    }

    /** 保存学生作业 */
    @Log(name = "保存学生作业", type = BusinessType.INSERT)
    @PostMapping("saveApeHomeworkStudent")
    @RequiresRoles("student")
    public Result saveApeHomeworkStudent(@RequestBody JSONObject jsonObject) {
        ApeUser currentUser = ShiroUtils.getUserInfo();
        JSONArray homework = jsonObject.getJSONArray("homework");
        List<ApeHomeworkStudent> list = new ArrayList<>();
        for (int i = 0; i < homework.size(); i++) {
            ApeHomeworkStudent homeworkStudent = homework.getJSONObject(i).toJavaObject(ApeHomeworkStudent.class);
            ApeHomework source = apeHomeworkService.getById(homeworkStudent.getWorkId());
            if (source == null) {
                return Result.fail("作业题不存在");
            }
            homeworkStudent.setId(null);
            homeworkStudent.setUserId(currentUser.getId());
            homeworkStudent.setChapterId(source.getChapterId());
            homeworkStudent.setChapterName(source.getChapterName());
            homeworkStudent.setTitle(source.getTitle());
            homeworkStudent.setSort(source.getSort());
            homeworkStudent.setAnswer(source.getAnswer());
            homeworkStudent.setType(source.getType());
            homeworkStudent.setScore(source.getScore());
            homeworkStudent.setContent(source.getContent());
            setHomeworkPoint(homeworkStudent);
            list.add(homeworkStudent);
        }
        boolean save = apeHomeworkStudentService.saveBatch(list);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑学生作业 */
    @Log(name = "编辑学生作业", type = BusinessType.UPDATE)
    @PostMapping("editApeHomeworkStudent")
    @RequiresRoles("student")
    public Result editApeHomeworkStudent(@RequestBody ApeHomeworkStudent apeHomeworkStudent) {
        if (StringUtils.isBlank(apeHomeworkStudent.getId())) {
            return Result.fail("学生作业id不能为空！");
        }
        ApeHomeworkStudent oldHomeworkStudent = apeHomeworkStudentService.getById(apeHomeworkStudent.getId());
        if (oldHomeworkStudent == null) {
            return Result.fail("学生作业不存在！");
        }
        requireCurrentStudentOwns(oldHomeworkStudent);
        apeHomeworkStudent.setUserId(oldHomeworkStudent.getUserId());
        oldHomeworkStudent.setSolution(apeHomeworkStudent.getSolution());
        setHomeworkPoint(oldHomeworkStudent);
        boolean save = apeHomeworkStudentService.updateById(oldHomeworkStudent);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除学生作业 */
    @GetMapping("removeApeHomeworkStudent")
    @RequiresRoles("student")
    @Log(name = "删除学生作业", type = BusinessType.DELETE)
    public Result removeApeHomeworkStudent(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                requireCurrentStudentOwns(apeHomeworkStudentService.getById(id));
                apeHomeworkStudentService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("学生作业id不能为空！");
        }
    }

    @GetMapping("getWrongWork")
    @RequiresRoles("student")
    public Result getWrongWork(@RequestParam(value = "id", required = false) String id) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        List<ApeHomeworkStudent> resultList = new ArrayList<>();

        // 1. 查询作业错题
        QueryWrapper<ApeHomeworkStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(id), ApeHomeworkStudent::getChapterId, id)
                .eq(ApeHomeworkStudent::getUserId,userInfo.getId())
                .orderByAsc(ApeHomeworkStudent::getChapterId)
                .orderByAsc(ApeHomeworkStudent::getSort);
        List<ApeHomeworkStudent> homeworkList = apeHomeworkStudentService.list(queryWrapper);
        for (ApeHomeworkStudent student : homeworkList) {
            if (StringUtils.isNotBlank(student.getChapterId())) {
                ApeChapter chapter = apeChapterService.getById(student.getChapterId());
                if (chapter != null) {
                    student.setTaskName(chapter.getTaskName());
                    student.setChapterName(chapter.getName());
                }
            }
        }
        homeworkList = homeworkList.stream()
                .filter(item -> !isHomeworkAnswerCorrect(item))
                .collect(Collectors.toList());
        resultList.addAll(homeworkList);

        // 2. 查询考试错题（ape_test_student 表）
        QueryWrapper<com.ape.apesystem.domain.ApeTestStudent> testWrapper = new QueryWrapper<>();
        testWrapper.lambda()
                .eq(com.ape.apesystem.domain.ApeTestStudent::getUserId, userInfo.getId())
                .orderByAsc(com.ape.apesystem.domain.ApeTestStudent::getTestId)
                .orderByAsc(com.ape.apesystem.domain.ApeTestStudent::getSort);
        List<com.ape.apesystem.domain.ApeTestStudent> testList = apeTestStudentService.list(testWrapper);
        for (com.ape.apesystem.domain.ApeTestStudent testItem : testList) {
            // 过滤正确答题（point >= score 表示正确）
            if (testItem.getPoint() != null && testItem.getScore() != null
                    && testItem.getPoint() >= testItem.getScore()) {
                continue;
            }
            if (testItem.getScore() != null && testItem.getScore() > 0
                    && testItem.getPoint() == null) {
                continue;
            }
            ApeHomeworkStudent mapped = new ApeHomeworkStudent();
            mapped.setId(testItem.getId());
            mapped.setWorkId(testItem.getItemId());
            mapped.setTitle(testItem.getTitle());
            mapped.setSort(testItem.getSort());
            mapped.setAnswer(testItem.getAnswer());
            mapped.setType(testItem.getType());
            mapped.setScore(testItem.getScore());
            mapped.setContent(testItem.getContent());
            mapped.setPoint(testItem.getPoint());
            mapped.setSolution(testItem.getSolution());
            mapped.setUserId(testItem.getUserId());
            mapped.setTaskName(testItem.getTestName() != null ? testItem.getTestName() : "考试");
            mapped.setChapterName("考试错题");
            mapped.setChapterId(testItem.getTestId());
            resultList.add(mapped);
        }

        return Result.success(resultList);
    }

    private void requireCurrentStudentOwns(ApeHomeworkStudent submission) {
        ApeUser currentUser = ShiroUtils.getUserInfo();
        if (currentUser == null || submission == null || !currentUser.getId().equals(submission.getUserId())) {
            throw new UnauthorizedException("无权操作该学生作业");
        }
    }

    private void setHomeworkPoint(ApeHomeworkStudent homeworkStudent) {
        homeworkStudent.setPoint(0);
        if (homeworkStudent.getScore() != null && isHomeworkAnswerCorrect(homeworkStudent)) {
            homeworkStudent.setPoint(homeworkStudent.getScore());
        }
    }

    private boolean isHomeworkAnswerCorrect(ApeHomeworkStudent homeworkStudent) {
        if (homeworkStudent == null || StringUtils.isBlank(homeworkStudent.getAnswer())
                || StringUtils.isBlank(homeworkStudent.getSolution())) {
            return false;
        }
        if (Integer.valueOf(1).equals(homeworkStudent.getType())) {
            return normalizeMultipleAnswer(homeworkStudent.getAnswer())
                    .equals(normalizeMultipleAnswer(homeworkStudent.getSolution()));
        }
        return homeworkStudent.getAnswer().trim().equals(homeworkStudent.getSolution().trim());
    }

    private String normalizeMultipleAnswer(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .sorted()
                .collect(Collectors.joining(","));
    }

}
