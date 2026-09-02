package com.ape.apeadmin.controller.homework;

import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apeadmin.security.ResourceAuthorizationService;
import com.ape.apesystem.domain.ApeChapter;
import com.ape.apesystem.domain.ApeHomework;
import com.ape.apesystem.service.ApeChapterService;
import com.ape.apesystem.service.ApeHomeworkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 作业controlle
 */
@Controller
@ResponseBody
@RequestMapping("homework")
public class ApeHomeworkController {

    @Autowired
    private ApeHomeworkService apeHomeworkService;
    @Autowired
    private ApeChapterService apeChapterService;
    @Autowired
    private ResourceAuthorizationService resourceAuthorizationService;

    /** 分页获取作业 */
    @Log(name = "分页获取作业", type = BusinessType.OTHER)
    @PostMapping("getApeHomeworkPage")
    public Result getApeHomeworkPage(@RequestBody ApeHomework apeHomework) {
        Page<ApeHomework> page = new Page<>(apeHomework.getPageNumber(),apeHomework.getPageSize());
        QueryWrapper<ApeHomework> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(apeHomework.getTitle()),ApeHomework::getTitle,apeHomework.getTitle())
                .eq(apeHomework.getType() != null,ApeHomework::getType,apeHomework.getType())
                .orderByAsc(ApeHomework::getSort);
        Page<ApeHomework> apeHomeworkPage = apeHomeworkService.page(page, queryWrapper);
        return Result.success(apeHomeworkPage);
    }

    /** 根据id获取作业 */
    @Log(name = "根据id获取作业", type = BusinessType.OTHER)
    @GetMapping("getApeHomeworkById")
    public Result getApeHomeworkById(@RequestParam("id")String id) {
        ApeHomework apeHomework = apeHomeworkService.getById(id);
        return Result.success(apeHomework);
    }

    @GetMapping("getApeHomeworkByChapterId")
    public Result getApeHomeworkByChapterId(@RequestParam("id")String id) {
        QueryWrapper<ApeHomework> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeHomework::getChapterId,id);
        int count = apeHomeworkService.count(queryWrapper);
        if (count > 0) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    /** 保存作业 */
    @Log(name = "保存作业", type = BusinessType.INSERT)
    @PostMapping("saveApeHomework")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result saveApeHomework(@RequestBody ApeHomework apeHomework) {
        resourceAuthorizationService.requireHomeworkParentOwnerOrAdmin(apeHomework.getChapterId());
        if (StringUtils.isNotBlank(apeHomework.getChapterId())) {
            ApeChapter chapter = apeChapterService.getById(apeHomework.getChapterId());
            if (chapter != null) {
                apeHomework.setChapterName(chapter.getName());
            }
        }
        boolean save = apeHomeworkService.save(apeHomework);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑作业 */
    @Log(name = "编辑作业", type = BusinessType.UPDATE)
    @PostMapping("editApeHomework")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result editApeHomework(@RequestBody ApeHomework apeHomework) {
        resourceAuthorizationService.requireHomeworkOwnerOrAdmin(apeHomework.getId());
        if (StringUtils.isNotBlank(apeHomework.getChapterId())) {
            resourceAuthorizationService.requireHomeworkParentOwnerOrAdmin(apeHomework.getChapterId());
        }
        if (StringUtils.isNotBlank(apeHomework.getChapterId())) {
            ApeChapter chapter = apeChapterService.getById(apeHomework.getChapterId());
            if (chapter != null) {
                apeHomework.setChapterName(chapter.getName());
            }
        }
        boolean save = apeHomeworkService.updateById(apeHomework);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除作业 */
    @GetMapping("removeApeHomework")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    @Log(name = "删除作业", type = BusinessType.DELETE)
    public Result removeApeHomework(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                resourceAuthorizationService.requireHomeworkOwnerOrAdmin(id);
                apeHomeworkService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("作业id不能为空！");
        }
    }

}
