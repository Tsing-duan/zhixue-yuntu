package com.ape.apeadmin.controller.test;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeadmin.security.ResourceAuthorizationService;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.*;
import com.ape.apesystem.service.ApeTaskService;
import com.ape.apesystem.service.ApeTaskStudentService;
import com.ape.apesystem.service.ApeTestService;
import com.ape.apesystem.service.ApeTestStudentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 考试controller
 */
@Controller
@ResponseBody
@RequestMapping("test")
public class ApeTestController {

    @Autowired
    private ApeTestService apeTestService;
    @Autowired
    private ApeTaskService apeTaskService;
    @Autowired
    private ApeTaskStudentService apeTaskStudentService;
    @Autowired
    private ApeTestStudentService apeTestStudentService;
    @Autowired
    private ResourceAuthorizationService resourceAuthorizationService;

    /** 分页获取考试 */
    @Log(name = "分页获取考试", type = BusinessType.OTHER)
    @PostMapping("getApeTestPage")
    public Result getApeTestPage(@RequestBody ApeTest apeTest) {
        Page<ApeTest> page = new Page<>(apeTest.getPageNumber(),apeTest.getPageSize());
        QueryWrapper<ApeTest> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeTest.getName()),ApeTest::getName,apeTest.getName())
                .like(StringUtils.isNotBlank(apeTest.getTaskName()),ApeTest::getTaskName,apeTest.getTaskName())
                .eq(apeTest.getState() != null,ApeTest::getState,apeTest.getState())
                .like(StringUtils.isNotBlank(apeTest.getCreateBy()),ApeTest::getCreateBy,apeTest.getCreateBy());
        if (apeTest.getType() != null && apeTest.getType() == 1) {
            QueryWrapper<ApeTask> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeTask::getTeacherId,ShiroUtils.getUserInfo().getId());
            List<ApeTask> taskList = apeTaskService.list(wrapper);
            List<String> list = new ArrayList<String>();
            for (ApeTask apeTask : taskList) {
                list.add(apeTask.getId());
            }
            if (list.size()>0) {
                queryWrapper.lambda().in(ApeTest::getTaskId,list);
            } else {
                list.add(" ");
                queryWrapper.lambda().in(ApeTest::getTaskId,list);
            }
        }
        Page<ApeTest> apeTestPage = apeTestService.page(page, queryWrapper);
        return Result.success(apeTestPage);
    }

    /** 根据id获取考试 */
    @Log(name = "根据id获取考试", type = BusinessType.OTHER)
    @GetMapping("getApeTestById")
    public Result getApeTestById(@RequestParam("id")String id) {
        ApeTest apeTest = apeTestService.getById(id);
        return Result.success(apeTest);
    }

    /** 保存考试 */
    @Log(name = "保存考试", type = BusinessType.INSERT)
    @PostMapping("saveApeTest")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result saveApeTest(@RequestBody ApeTest apeTest) {
        resourceAuthorizationService.requireTestParentOwnerOrAdmin(apeTest.getTaskId());
        if (StringUtils.isNotBlank(apeTest.getTaskId())) {
            ApeTask task = apeTaskService.getById(apeTest.getTaskId());
            if (task != null) {
                apeTest.setTaskName(task.getName());
            }
        }
        boolean save = apeTestService.save(apeTest);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑考试 */
    @Log(name = "编辑考试", type = BusinessType.UPDATE)
    @PostMapping("editApeTest")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result editApeTest(@RequestBody ApeTest apeTest) {
        resourceAuthorizationService.requireTestOwnerOrAdmin(apeTest.getId());
        if (StringUtils.isNotBlank(apeTest.getTaskId())) {
            resourceAuthorizationService.requireTestParentOwnerOrAdmin(apeTest.getTaskId());
        }
        if (StringUtils.isNotBlank(apeTest.getTaskId())) {
            ApeTask task = apeTaskService.getById(apeTest.getTaskId());
            if (task != null) {
                apeTest.setTaskName(task.getName());
            }
        }
        boolean save = apeTestService.updateById(apeTest);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除考试 */
    @GetMapping("removeApeTest")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    @Log(name = "删除考试", type = BusinessType.DELETE)
    public Result removeApeTest(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                resourceAuthorizationService.requireTestOwnerOrAdmin(id);
                apeTestService.removeById(id);
                QueryWrapper<ApeTestStudent> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ApeTestStudent::getTestId,id);
                apeTestStudentService.remove(queryWrapper);
            }
            return Result.success();
        } else {
            return Result.fail("考试id不能为空！");
        }
    }

    /** 获取用户考试 */
    @GetMapping("getTestListByUser")
    @RequiresRoles("student")
    public Result getTestListByUser() {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        QueryWrapper<ApeTaskStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeTaskStudent::getUserId,userInfo.getId())
                .eq(ApeTaskStudent::getState,0);
        List<ApeTaskStudent> studentList = apeTaskStudentService.list(queryWrapper);
        List<ApeTest> apeTestList = new ArrayList<>();
        for (ApeTaskStudent apeTaskStudent : studentList) {
            QueryWrapper<ApeTest> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeTest::getTaskId,apeTaskStudent.getTaskId()).eq(ApeTest::getState,0);
            List<ApeTest> testList = apeTestService.list(wrapper);
            if (testList.size() > 0) {
                apeTestList.addAll(testList);
            }
        }
        for (ApeTest test : apeTestList) {
            boolean retakeGranted = apeTestStudentService.count(retakeGrantWrapper(test.getId(), userInfo.getId())) > 0;
            if (retakeGranted) {
                test.setSchedule("可补考");
                test.setScoreTotal(0);
                continue;
            }
            List<ApeTestStudent> list = apeTestStudentService.list(answerWrapper(test.getId(), userInfo.getId()));
            if (list.size() <= 0) {
                if (test.getEndTime() != null && test.getEndTime().before(new Date())) {
                    test.setSchedule("已截止");
                } else {
                    test.setSchedule("未开始");
                }
                test.setScoreTotal(0);
            } else {
                test.setSchedule("已完成");
                int total = 0;
                for (ApeTestStudent apeTestStudent : list) {
                    if (apeTestStudent.getPoint() != null) {
                        total += apeTestStudent.getPoint();
                    }
                }
                test.setScoreTotal(total);
            }
        }
        return Result.success(apeTestList);
    }

    @PostMapping("getTestStudent")
    public Result getTestStudent(@RequestBody JSONObject jsonObject) {
        String testId = jsonObject.getString("testId");
        ApeTest test = apeTestService.getById(testId);
        String userName = jsonObject.getString("userName");
        Integer pageNumber = jsonObject.getInteger("pageNumber");
        Integer pageSize = jsonObject.getInteger("pageSize");
        // 查询该考试的所有提交记录
        QueryWrapper<ApeTestStudent> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeTestStudent::getTestId, testId)
                .like(StringUtils.isNotBlank(userName), ApeTestStudent::getCreateBy, userName)
                .orderByAsc(ApeTestStudent::getUpdateTime);
        List<ApeTestStudent> allList = apeTestStudentService.list(queryWrapper);
        // Java 中按 userId 去重，保留每个学生最新记录作为代表
        Map<String, ApeTestStudent> userMap = new LinkedHashMap<>();
        for (ApeTestStudent s : allList) {
            ApeTestStudent existing = userMap.get(s.getUserId());
            if (existing == null || (s.getUpdateTime() != null &&
                    (existing.getUpdateTime() == null || s.getUpdateTime().after(existing.getUpdateTime())))) {
                userMap.put(s.getUserId(), s);
            }
        }
        List<ApeTestStudent> distinctList = new ArrayList<>(userMap.values());
        // 手动分页
        int total = distinctList.size();
        int start = (pageNumber - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<ApeTestStudent> pageList = start < total ? distinctList.subList(start, end) : new ArrayList<>();
        // 计算每个学生的总分
        for (ApeTestStudent student : pageList) {
            List<ApeTestStudent> testStudents = apeTestStudentService.list(answerWrapper(testId, student.getUserId()));
            int score = 0;
            for (ApeTestStudent item : testStudents) {
                if (item.getPoint() != null) {
                    score += item.getPoint();
                }
            }
            student.setTestName(test.getName());
            student.setTotalScore(test.getTotalScore());
            student.setTotalGetScore(score);
        }
        Page<ApeTestStudent> studentPage = new Page<>(pageNumber, pageSize, total);
        studentPage.setRecords(pageList);
        return Result.success(studentPage);
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

}
