package com.ape.apeadmin.controller.test;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeadmin.security.ResourceAuthorizationService;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeTest;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.domain.ApeTestStudent;
import com.ape.apesystem.domain.ApeTestItem;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeTaskService;
import com.ape.apesystem.service.ApeTestService;
import com.ape.apesystem.service.ApeTestStudentService;
import com.ape.apesystem.service.ApeTestItemService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 用户考试题目controller
 */
@Controller
@ResponseBody
@RequestMapping("student")
public class ApeTestStudentController {

    @Autowired
    private ApeTestStudentService apeTestStudentService;
    @Autowired
    private ApeTestService apeTestService;
    @Autowired
    private ApeTestItemService apeTestItemService;
    @Autowired
    private ApeTaskService apeTaskService;
    @Autowired
    private ApeUserService apeUserService;
    @Autowired
    private ResourceAuthorizationService resourceAuthorizationService;

    /** 分页获取用户考试题目 */
    @Log(name = "分页获取用户考试题目", type = BusinessType.OTHER)
    @PostMapping("getApeTestStudentPage")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result getApeTestStudentPage(@RequestBody ApeTestStudent apeTestStudent) {
        resourceAuthorizationService.requireTestOwnerOrAdmin(apeTestStudent.getTestId());
        Page<ApeTestStudent> page = new Page<>(apeTestStudent.getPageNumber(),apeTestStudent.getPageSize());
        QueryWrapper<ApeTestStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(apeTestStudent.getItemId()),ApeTestStudent::getItemId,apeTestStudent.getItemId())
                .eq(StringUtils.isNotBlank(apeTestStudent.getTestId()),ApeTestStudent::getTestId,apeTestStudent.getTestId())
                .like(StringUtils.isNotBlank(apeTestStudent.getTitle()),ApeTestStudent::getTitle,apeTestStudent.getTitle())
                .eq(apeTestStudent.getSort() != null,ApeTestStudent::getSort,apeTestStudent.getSort())
                .eq(apeTestStudent.getType() != null,ApeTestStudent::getType,apeTestStudent.getType())
                .eq(StringUtils.isNotBlank(apeTestStudent.getUserId()),ApeTestStudent::getUserId,apeTestStudent.getUserId());
        Page<ApeTestStudent> apeTestStudentPage = apeTestStudentService.page(page, queryWrapper);
        return Result.success(apeTestStudentPage);
    }

    /** 根据id获取用户考试题目 */
    @Log(name = "根据id获取用户考试题目", type = BusinessType.OTHER)
    @GetMapping("getApeTestStudentById")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result getApeTestStudentById(@RequestParam("id")String id) {
        ApeTestStudent apeTestStudent = apeTestStudentService.getById(id);
        resourceAuthorizationService.requireTestOwnerOrAdmin(apeTestStudent == null ? null : apeTestStudent.getTestId());
        return Result.success(apeTestStudent);
    }

    @GetMapping("getTestUserState")
    @RequiresRoles("student")
    public Result getTestUserState(@RequestParam("id")String id) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (userInfo != null && hasSubmittedAnswer(id, userInfo.getId())) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    /** 保存用户考试题目 */
    @Log(name = "保存用户考试题目", type = BusinessType.INSERT)
    @PostMapping("saveApeTestStudent")
    @RequiresRoles("student")
    @Transactional
    public Result saveApeTestStudent(@RequestBody JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        if (jsonArray == null || jsonArray.isEmpty()) {
            return Result.fail("考试内容不能为空");
        }
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return Result.fail("请先登录后再提交考试");
        }
        String firstItemId = jsonArray.getJSONObject(0).getString("itemId");
        ApeTestItem firstItem = StringUtils.isBlank(firstItemId) ? null : apeTestItemService.getById(firstItemId);
        String testId = firstItem == null ? null : firstItem.getTestId();
        if (StringUtils.isBlank(testId)) {
            return Result.fail("考试ID不能为空");
        }
        if (hasSubmittedAnswer(testId, userInfo.getId())) {
            return Result.success("考试已提交，请勿重复交卷");
        }
        ApeTest test = apeTestService.getById(testId);
        if (test == null) {
            return Result.fail("考试不存在");
        }
        boolean retakeGranted = hasRetakeGrant(testId, userInfo.getId());
        if (isExpired(test) && !retakeGranted) {
            return Result.fail("考试截止时间已过无法提交");
        }
        List<ApeTestStudent> apeTestStudents = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            ApeTestStudent object = jsonArray.getJSONObject(i).toJavaObject(ApeTestStudent.class);
            ApeTestItem source = StringUtils.isBlank(object.getItemId()) ? null : apeTestItemService.getById(object.getItemId());
            if (source == null || !testId.equals(source.getTestId())) {
                return Result.fail("一次只能提交同一场考试");
            }
            object.setId(null);
            object.setTestId(source.getTestId());
            object.setTitle(source.getTitle());
            object.setSort(source.getSort());
            object.setType(source.getType());
            object.setScore(source.getScore());
            object.setKeyword(source.getKeyword());
            object.setAnswer(source.getAnswer());
            object.setContent(source.getContent());
            object.setUserId(userInfo.getId());
            object.setPoint(0);
            if (StringUtils.isNotBlank(object.getSolution())) {
                // 类型 0-3：单选、多选、填空、判断
                if (object.getType() != null && object.getType() >= 0 && object.getType() <= 3) {
                    String answer = object.getAnswer();
                    String solution = object.getSolution();
                    // 多选题(type=1)排序后比较，避免顺序差异误判
                    if (object.getType() == 1 && answer != null && solution != null) {
                        answer = normalizeMultiChoice(answer);
                        solution = normalizeMultiChoice(solution);
                    }
                    if (answer != null && answer.equals(solution)) {
                        object.setPoint(object.getScore());
                    }
                }
                // 类型 4：问答题关键词评分
                if (object.getType() == 4) {
                    JSONArray parseArray = JSONArray.parseArray(object.getKeyword());
                    int score = 0;
                    for (int j = 0; j < parseArray.size(); j++) {
                        JSONObject item = parseArray.getJSONObject(j);
                        if (object.getSolution().contains(item.getString("option"))) {
                            score += item.getInteger("value");
                        }
                    }
                    object.setPoint(score);
                }
                // 类型 5：计算题不自动评分，留给教师手动批阅
            }
            apeTestStudents.add(object);
        }
        boolean save = apeTestStudentService.saveBatch(apeTestStudents);
        if (save) {
            if (retakeGranted) {
                apeTestStudentService.remove(retakeGrantWrapper(testId, userInfo.getId()));
            }
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑用户考试题目 */
    @Log(name = "编辑用户考试题目", type = BusinessType.UPDATE)
    @PostMapping("editApeTestStudent")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result editApeTestStudent(@RequestBody JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        List<ApeTestStudent> apeTestStudents = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            ApeTestStudent requestItem = jsonArray.getJSONObject(i).toJavaObject(ApeTestStudent.class);
            ApeTestStudent storedItem = apeTestStudentService.getById(requestItem.getId());
            if (storedItem == null) {
                return Result.fail("学生考试记录不存在");
            }
            resourceAuthorizationService.requireTestOwnerOrAdmin(storedItem.getTestId());
            storedItem.setPoint(requestItem.getPoint());
            apeTestStudents.add(storedItem);
        }
        boolean save = apeTestStudentService.updateBatchById(apeTestStudents);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    @GetMapping("getTestStudentItem")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result getTestStudent(@RequestParam("testId")String testId,@RequestParam("userId")String userId) {
        resourceAuthorizationService.requireTestOwnerOrAdmin(testId);
        List<ApeTestStudent> studentList = apeTestStudentService.list(answerWrapper(testId, userId));
        return Result.success(studentList);
    }

    /** 删除用户考试题目 */
    @GetMapping("removeApeTestStudent")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    @Log(name = "删除用户考试题目", type = BusinessType.DELETE)
    public Result removeApeTestStudent(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                ApeTestStudent submission = apeTestStudentService.getById(id);
                resourceAuthorizationService.requireTestOwnerOrAdmin(submission == null ? null : submission.getTestId());
                apeTestStudentService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("用户考试题目id不能为空！");
        }
    }

    /** 重置学生考试 */
    @Log(name = "重置学生考试", type = BusinessType.DELETE)
    @PostMapping("resetTestStudent")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    @Transactional
    public Result resetTestStudent(@RequestBody JSONObject jsonObject) {
        ApeUser currentUser = ShiroUtils.getUserInfo();
        if (currentUser == null) {
            return Result.fail("请先登录后再操作");
        }
        // 仅管理员和教师可重置
        if (currentUser.getUserType() == null
                || (currentUser.getUserType() != Constants.USER_TYPE_ADMIN
                    && currentUser.getUserType() != Constants.USER_TYPE_TEACHER)) {
            return Result.fail("无权限执行此操作");
        }

        String testId = jsonObject.getString("testId");
        String userId = jsonObject.getString("userId");
        if (StringUtils.isBlank(testId) || StringUtils.isBlank(userId)) {
            return Result.fail("考试ID和学生ID不能为空");
        }
        ApeTest test = apeTestService.getById(testId);
        if (test == null) {
            return Result.fail("考试不存在");
        }
        ApeUser targetUser = apeUserService.getById(userId);
        if (targetUser == null) {
            return Result.fail("学生不存在");
        }
        if (targetUser.getUserType() == null || targetUser.getUserType() != Constants.USER_TYPE_STUDENT) {
            return Result.fail("只能重置学生考试");
        }

        // 教师只能重置自己课程的考试
        if (currentUser.getUserType() != Constants.USER_TYPE_ADMIN) {
            ApeTask task = apeTaskService.getById(test.getTaskId());
            if (task == null || !currentUser.getId().equals(task.getTeacherId())) {
                return Result.fail("无权重置该考试的记录，只能操作自己课程下的考试");
            }
        }

        QueryWrapper<ApeTestStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ApeTestStudent::getTestId, testId)
                .eq(ApeTestStudent::getUserId, userId);
        apeTestStudentService.remove(queryWrapper);

        ApeTestStudent grant = new ApeTestStudent();
        grant.setItemId(Constants.TEST_RETAKE_GRANT_ITEM_ID);
        grant.setTestId(testId);
        grant.setTitle("补考授权");
        grant.setSort(0);
        grant.setScore(0);
        grant.setPoint(0);
        grant.setUserId(userId);
        grant.setRemark(Constants.TEST_RETAKE_GRANT_REMARK);
        grant.setCreateBy(getUserDisplayName(targetUser));
        boolean saved = apeTestStudentService.save(grant);
        if (saved) {
            grant.setCreateBy(getUserDisplayName(targetUser));
            apeTestStudentService.updateById(grant);
            return Result.success("重置成功，已授权该学生补考");
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    private boolean hasSubmittedAnswer(String testId, String userId) {
        return apeTestStudentService.count(answerWrapper(testId, userId)) > 0;
    }

    private boolean hasRetakeGrant(String testId, String userId) {
        return apeTestStudentService.count(retakeGrantWrapper(testId, userId)) > 0;
    }

    private QueryWrapper<ApeTestStudent> answerWrapper(String testId, String userId) {
        QueryWrapper<ApeTestStudent> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ApeTestStudent::getTestId, testId)
                .eq(ApeTestStudent::getUserId, userId)
                .and(w -> w.isNull(ApeTestStudent::getRemark)
                        .or()
                        .ne(ApeTestStudent::getRemark, Constants.TEST_RETAKE_GRANT_REMARK));
        return wrapper;
    }

    private QueryWrapper<ApeTestStudent> retakeGrantWrapper(String testId, String userId) {
        QueryWrapper<ApeTestStudent> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ApeTestStudent::getTestId, testId)
                .eq(ApeTestStudent::getUserId, userId)
                .eq(ApeTestStudent::getRemark, Constants.TEST_RETAKE_GRANT_REMARK);
        return wrapper;
    }

    private boolean isExpired(ApeTest test) {
        return test != null && test.getEndTime() != null && test.getEndTime().before(new Date());
    }

    private String getUserDisplayName(ApeUser user) {
        if (user == null) {
            return "";
        }
        return StringUtils.defaultIfBlank(user.getUserName(), user.getLoginAccount());
    }

    /**
     * 多选题答案排序标准化，避免 "A,B" 和 "B,A" 误判
     */
    private String normalizeMultiChoice(String value) {
        if (value == null || value.isEmpty()) return "";
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining(","));
    }

}
