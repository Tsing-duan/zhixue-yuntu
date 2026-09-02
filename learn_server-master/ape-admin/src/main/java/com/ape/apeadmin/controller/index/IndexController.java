package com.ape.apeadmin.controller.index;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.domain.ApeTaskStudent;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeAccountService;
import com.ape.apesystem.service.ApeTaskService;
import com.ape.apesystem.service.ApeTaskStudentService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Controller
@ResponseBody
@RequestMapping("/index")
public class IndexController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private ApeUserService apeUserService;
    @Autowired
    private ApeTaskService apeTaskService;
    @Autowired
    private ApeTaskStudentService apeTaskStudentService;
    @Autowired
    private ApeAccountService apeAccountService;

    /** 获取首页统计数据 */
    @Log(name = "获取首页统计数据", type = BusinessType.OTHER)
    @GetMapping("getIndexAchievement")
    public Result getIndexAchievement() {
        int count = apeTaskService.count();
        QueryWrapper<ApeUser> query = new QueryWrapper<>();
        query.lambda().eq(ApeUser::getUserType,1);
        int count1 = apeUserService.count(query);
        QueryWrapper<ApeUser> query2 = new QueryWrapper<>();
        query2.lambda().eq(ApeUser::getUserType,2);
        int count2 = apeUserService.count(query2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("count",count);
        jsonObject.put("count1",count1);
        jsonObject.put("count2",count2);
        jsonObject.put("total",count1 + count2);
        log.info("首页统计数据 -> taskCount={}, teacherCount={}, studentCount={}, total={}", count, count1, count2, count1 + count2);
        return Result.success(jsonObject);
    }

    //后台首页

    @GetMapping("getIndexData")
    @Log(name = "获取首页概览数据", type = BusinessType.OTHER)
    public Result getIndexData(@RequestParam("type") Integer type) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (type != 0 && userInfo == null) {
            return Result.fail("登录状态失效，请重新登录");
        }
        JSONObject jsonObject = new JSONObject();
        //用户数量
        QueryWrapper<ApeUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUser::getUserType,1).or()
                .eq(ApeUser::getUserType,2);
        int count = apeUserService.count(queryWrapper);
        jsonObject.put("userNum",count);
        //教师数量
        QueryWrapper<ApeUser> query = new QueryWrapper<>();
        query.lambda().eq(ApeUser::getUserType,1);
        int count1 = apeUserService.count(query);
        jsonObject.put("teacherNum",count1);
        if (type == 0) {
            //学生数量
            QueryWrapper<ApeUser> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeUser::getUserType,2);
            int num = apeUserService.count(wrapper);
            jsonObject.put("studentNum",num);
        } else {
            // 学生数量：按 teacherId + state=0 去重 user_id
            int studentNum = countDistinctStudentNumByTeacher(userInfo.getId());
            jsonObject.put("studentNum", studentNum);
        }
        log.info("首页概览数据 -> type={}, userId={}, data={}", type, userInfo == null ? null : userInfo.getId(), jsonObject);
        return Result.success(jsonObject);
    }

    @GetMapping("getIndexSexData")
    @Log(name = "获取首页性别统计", type = BusinessType.OTHER)
    public Result getIndexSexData(@RequestParam("type") Integer type) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (type != 0 && userInfo == null) {
            return Result.fail("登录状态失效，请重新登录");
        }
        List<JSONObject> list = new ArrayList<>();
        int nan = 0;
        int nv = 0;
        if (type == 0) {
            QueryWrapper<ApeUser> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeUser::getUserType,2)
                    .eq(ApeUser::getSex,0);
            nan = apeUserService.count(wrapper);
            QueryWrapper<ApeUser> wrapper1 = new QueryWrapper<>();
            wrapper1.lambda().eq(ApeUser::getUserType,2)
                    .eq(ApeUser::getSex,1);
            nv = apeUserService.count(wrapper1);
        } else {
            List<String> userIds = listDistinctStudentIdsByTeacher(userInfo.getId());
            for (String userId : userIds) {
                ApeUser user = apeUserService.getById(userId);
                if (user == null) {
                    continue;
                }
                if (user.getSex() == 0) {
                    nan += 1;
                } else {
                    nv += 1;
                }
            }

        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value",nan);
        jsonObject.put("name","男");
        list.add(jsonObject);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("value",nv);
        jsonObject1.put("name","女");
        list.add(jsonObject1);
        log.info("首页性别统计 -> type={}, userId={}, male={}, female={}", type, userInfo == null ? null : userInfo.getId(), nan, nv);
        return Result.success(list);
    }

    @GetMapping("getTaskChart")
    @Log(name = "获取任务图表数据", type = BusinessType.OTHER)
    public Result getTaskChart(@RequestParam("type")Integer type) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (type != 0 && userInfo == null) {
            return Result.fail("登录状态失效，请重新登录");
        }
        List<String> tasks = new ArrayList<>();
        List<Integer> nums =  new ArrayList<>();
        if (type == 0) {
            QueryWrapper<ApeTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().orderByAsc(ApeTask::getCreateTime).last("limit 10");
            List<ApeTask> taskList = apeTaskService.list(queryWrapper);
            for (ApeTask task : taskList) {
                tasks.add(task.getName());
                nums.add(countDistinctStudentNumByTask(task.getId()));
            }
        } else {
            QueryWrapper<ApeTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().orderByAsc(ApeTask::getCreateTime).eq(ApeTask::getTeacherId,userInfo.getId()).last("limit 10");
            List<ApeTask> taskList = apeTaskService.list(queryWrapper);
            for (ApeTask task : taskList) {
                tasks.add(task.getName());
                nums.add(countDistinctStudentNumByTask(task.getId()));
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tasks",tasks);
        jsonObject.put("nums",nums);
        log.info("任务图表数据 -> type={}, userId={}, taskSize={}, numSize={}", type, userInfo == null ? null : userInfo.getId(), tasks.size(), nums.size());
        return Result.success(jsonObject);
    }

    @GetMapping("getTaskIndexList")
    @Log(name = "获取首页任务列表", type = BusinessType.OTHER)
    public Result getTaskIndexList(@RequestParam("type")Integer type) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (type != 0 && userInfo == null) {
            return Result.fail("登录状态失效，请重新登录");
        }
        List<ApeTask> taskList = new ArrayList<>();
        if (type == 0) {
            QueryWrapper<ApeTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().orderByDesc(ApeTask::getCreateTime).last("limit 5");
            taskList = apeTaskService.list(queryWrapper);
        } else {
            QueryWrapper<ApeTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().orderByDesc(ApeTask::getCreateTime).eq(ApeTask::getTeacherId,userInfo.getId()).last("limit 5");
            taskList = apeTaskService.list(queryWrapper);
        }
        log.info("首页任务列表 -> type={}, userId={}, taskCount={}", type, userInfo == null ? null : userInfo.getId(), taskList.size());
        return Result.success(taskList);
    }

    /**
     * 统计某教师名下已通过报名的去重学生数
     */
    private int countDistinctStudentNumByTeacher(String teacherId) {
        return listDistinctStudentIdsByTeacher(teacherId).size();
    }

    /**
     * 查询某教师名下已通过报名的去重学生ID列表
     */
    private List<String> listDistinctStudentIdsByTeacher(String teacherId) {
        QueryWrapper<ApeTaskStudent> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT user_id")
                .lambda()
                .eq(ApeTaskStudent::getTeacherId, teacherId)
                .eq(ApeTaskStudent::getState, 0);
        return apeTaskStudentService.list(wrapper)
                .stream()
                .map(ApeTaskStudent::getUserId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 统计某课程已通过报名的去重学生数
     */
    private int countDistinctStudentNumByTask(String taskId) {
        QueryWrapper<ApeTaskStudent> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT user_id")
                .lambda()
                .eq(ApeTaskStudent::getTaskId, taskId)
                .eq(ApeTaskStudent::getState, 0);
        return (int) apeTaskStudentService.list(wrapper)
                .stream()
                .map(ApeTaskStudent::getUserId)
                .filter(id -> id != null && !id.isEmpty())
                .count();
    }

}
