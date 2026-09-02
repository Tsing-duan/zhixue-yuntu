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
import com.ape.apesystem.service.ApeTestItemService;
import com.ape.apesystem.service.ApeTestService;
import com.ape.apesystem.service.ApeTestStudentService;
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
import java.util.Date;
import java.util.List;

/**
 * @description: 考试题目controller
 */
@Controller
@ResponseBody
@RequestMapping("item")
public class ApeTestItemController {

    @Autowired
    private ApeTestItemService apeTestItemService;
    @Autowired
    private ApeTestService apeTestService;
    @Autowired
    private ApeTestStudentService apeTestStudentService;
    @Autowired
    private ResourceAuthorizationService resourceAuthorizationService;

    /** 分页获取考试题目 */
    @Log(name = "分页获取考试题目", type = BusinessType.OTHER)
    @PostMapping("getApeTestItemPage")
    public Result getApeTestItemPage(@RequestBody ApeTestItem apeTestItem) {
        Page<ApeTestItem> page = new Page<>(apeTestItem.getPageNumber(),apeTestItem.getPageSize());
        QueryWrapper<ApeTestItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeTestItem.getTitle()),ApeTestItem::getTitle,apeTestItem.getTitle())
                .eq(StringUtils.isNotBlank(apeTestItem.getTestId()),ApeTestItem::getTestId,apeTestItem.getTestId());
        Page<ApeTestItem> apeTestItemPage = apeTestItemService.page(page, queryWrapper);
        return Result.success(apeTestItemPage);
    }

    /** 根据id获取考试题目 */
    @Log(name = "根据id获取考试题目", type = BusinessType.OTHER)
    @GetMapping("getApeTestItemById")
    public Result getApeTestItemById(@RequestParam("id")String id) {
        ApeTestItem apeTestItem = apeTestItemService.getById(id);
        return Result.success(apeTestItem);
    }

    @GetMapping("getApeTestItemByTestId")
    @RequiresRoles("student")
    public Result getApeTestItemByTestId(@RequestParam("id")String id) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        if (userInfo == null) {
            return Result.fail("请先登录后再参加考试");
        }
        ApeTest test = apeTestService.getById(id);
        if (test == null) {
            return Result.fail("考试不存在");
        }
        boolean submitted = apeTestStudentService.count(answerWrapper(id, userInfo.getId())) > 0;
        boolean retakeGranted = apeTestStudentService.count(retakeGrantWrapper(id, userInfo.getId())) > 0;
        if (!submitted && !retakeGranted && test.getEndTime() != null && test.getEndTime().before(new Date())) {
            return Result.fail("考试截止时间已过，不能参加考试");
        }
        QueryWrapper<ApeTestItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeTestItem::getTestId,id).orderByAsc(ApeTestItem::getSort);
        List<ApeTestItem> apeTestItem = apeTestItemService.list(queryWrapper);
        List<ApeTestStudent> apeTestStudents = new ArrayList<>();
        for (ApeTestItem item : apeTestItem) {
            QueryWrapper<ApeTestStudent> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeTestStudent::getUserId,userInfo.getId())
                    .eq(ApeTestStudent::getItemId,item.getId());
            ApeTestStudent apeTestStudent = apeTestStudentService.getOne(wrapper);
            if (apeTestStudent == null) {
                apeTestStudent = new ApeTestStudent();
                apeTestStudent.setItemId(item.getId());
                apeTestStudent.setTestId(item.getTestId());
                apeTestStudent.setTitle(item.getTitle());
                apeTestStudent.setContent(item.getContent());
                apeTestStudent.setSort(item.getSort());
                apeTestStudent.setType(item.getType());
                apeTestStudent.setScore(item.getScore());
                apeTestStudent.setKeyword(item.getKeyword());
                apeTestStudent.setAnswer(item.getAnswer());
                apeTestStudent.setUserId(userInfo.getId());
            }
            apeTestStudents.add(apeTestStudent);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("testItem",apeTestStudents);
        jsonObject.put("test",test);
        return Result.success(jsonObject);
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

    /** 保存考试题目 */
    @Log(name = "保存考试题目", type = BusinessType.INSERT)
    @PostMapping("saveApeTestItem")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result saveApeTestItem(@RequestBody ApeTestItem apeTestItem) {
        resourceAuthorizationService.requireTestItemParentOwnerOrAdmin(apeTestItem.getTestId());
        boolean save = apeTestItemService.save(apeTestItem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑考试题目 */
    @Log(name = "编辑考试题目", type = BusinessType.UPDATE)
    @PostMapping("editApeTestItem")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result editApeTestItem(@RequestBody ApeTestItem apeTestItem) {
        resourceAuthorizationService.requireTestItemOwnerOrAdmin(apeTestItem.getId());
        if (StringUtils.isNotBlank(apeTestItem.getTestId())) {
            resourceAuthorizationService.requireTestItemParentOwnerOrAdmin(apeTestItem.getTestId());
        }
        boolean save = apeTestItemService.updateById(apeTestItem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除考试题目 */
    @GetMapping("removeApeTestItem")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    @Log(name = "删除考试题目", type = BusinessType.DELETE)
    public Result removeApeTestItem(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                resourceAuthorizationService.requireTestItemOwnerOrAdmin(id);
                apeTestItemService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("考试题目id不能为空！");
        }
    }

}
