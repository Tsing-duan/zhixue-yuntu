package com.ape.apeadmin.controller.user;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.enums.ResultCode;
import com.ape.apecommon.utils.PasswordUtils;
import com.ape.apeadmin.validator.AccountPasswordValidator;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeRole;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.domain.ApeUserRole;
import com.ape.apesystem.service.ApeRoleService;
import com.ape.apesystem.service.ApeUserRoleService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**

 * @description: 用户controller
 */
@Controller
@ResponseBody
@RequestMapping("/user")
public class ApeUserController {

    @Autowired
    private ApeUserService apeUserService;
    @Autowired
    private ApeRoleService apeRoleService;
    @Autowired
    private ApeUserRoleService apeUserRoleService;

    /** 分页查询用户 */
    @Log(name = "分页查询用户", type = BusinessType.OTHER)
    @PostMapping("getUserPage")
    @RequiresRoles("admin")
    public Result getUserPage(@RequestBody ApeUser apeUser) {
        Page<ApeUser> page = apeUserService.getUserPage(apeUser);
        return Result.success(page);
    }

    /** 根据类型查询用户 */
    @Log(name = "根据类型查询用户", type = BusinessType.OTHER)
    @GetMapping("getUserListByType")
    @RequiresRoles("admin")
    public Result getUserListByType(@RequestParam("type")Integer type) {
        QueryWrapper<ApeUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUser::getUserType,type);
        List<ApeUser> userList = apeUserService.list(queryWrapper);
        return Result.success(userList);
    }

    /** 根据id查询用户 */
    @Log(name = "根据id查询用户", type = BusinessType.OTHER)
    @GetMapping("getUserById")
    @RequiresRoles("admin")
    public Result getUserById(@RequestParam("id") String id) {
        ApeUser apeUser = apeUserService.getById(id);
        QueryWrapper<ApeUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUserRole::getUserId,apeUser.getId());
        List<ApeUserRole> list = apeUserRoleService.list(queryWrapper);
        List<String> roles = new ArrayList<>();
        for (ApeUserRole apeUserRole : list) {
            roles.add(apeUserRole.getRoleId());
        }
        apeUser.setRoleIds(roles);
        return Result.success(apeUser);
    }

    /** 新增用户 */
    @Log(name = "新增用户", type = BusinessType.INSERT)
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("saveUser")
    @RequiresRoles("admin")
    public Result saveUser(@RequestBody ApeUser apeUser) {
        String validateMessage = AccountPasswordValidator.validateUserCredentials(apeUser);
        if (validateMessage != null) {
            return Result.fail(validateMessage);
        }
        //先校验登陆账号是否重复
        boolean account = checkAccount(apeUser);
        if (!account) {
            return Result.fail("登陆账号已存在不可重复！");
        }
        String uuid = IdWorker.get32UUID();
        // BCrypt加密
        apeUser.setId(uuid);
        apeUser.setPassword(PasswordUtils.encrypt(apeUser.getPassword()));
        apeUser.setSalt("");
        apeUser.setAvatar(resolveDefaultAvatar(apeUser.getUserType(), apeUser.getSex()));
        apeUser.setPwdUpdateDate(new Date());
        //保存用户
        boolean save = apeUserService.save(apeUser);
        //再保存用户角色关系
        List<String> roleIds = apeUser.getRoleIds();
        List<ApeUserRole> apeUserRoles = new ArrayList<>();
        if (roleIds != null && roleIds.size() > 0) {
            for (String roleId : roleIds) {
                ApeUserRole apeUserRole = new ApeUserRole();
                apeUserRole.setUserId(uuid);
                apeUserRole.setRoleId(roleId);
                apeUserRoles.add(apeUserRole);
            }
        }
        if (apeUser.getUserType() == Constants.USER_TYPE_TEACHER) {
            ApeUserRole apeUserRole = new ApeUserRole();
            apeUserRole.setUserId(uuid);
            QueryWrapper<ApeRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ApeRole::getRoleKey,Constants.ROLE_KEY_TEACHER);
            ApeRole role = apeRoleService.getOne(queryWrapper);
            apeUserRole.setRoleId(role.getId());
            apeUserRoles.add(apeUserRole);
        }
        apeUserRoleService.saveBatch(apeUserRoles);
        return Result.success();
    }

    /** 编辑用户 */
    @Log(name = "编辑用户", type = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("editUser")
    @RequiresRoles("admin")
    public Result editUser(@RequestBody ApeUser apeUser) {
        ApeUser user = apeUserService.getById(apeUser.getId());
        if (!user.getLoginAccount().equals(apeUser.getLoginAccount())) {
            String validateMessage = AccountPasswordValidator.validateAccountForUser(apeUser);
            if (validateMessage != null) {
                return Result.fail(validateMessage);
            }
            //先校验登陆账号是否重复
            boolean account = checkAccount(apeUser);
            if (!account) {
                return Result.fail("登陆账号已存在不可重复！");
            }
        }
        //更新用户
        boolean edit = apeUserService.updateById(apeUser);
        //先删除用户角色关系
        QueryWrapper<ApeUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUserRole::getUserId,apeUser.getId());
        apeUserRoleService.remove(queryWrapper);
        //再次保存最新的关系
        List<String> roleIds = apeUser.getRoleIds();
        List<ApeUserRole> apeUserRoles = new ArrayList<>();
        if (roleIds != null && roleIds.size() > 0) {
            for (String roleId : roleIds) {
                ApeUserRole apeUserRole = new ApeUserRole();
                apeUserRole.setUserId(apeUser.getId());
                apeUserRole.setRoleId(roleId);
                apeUserRoles.add(apeUserRole);
            }
        }
        if (apeUser.getUserType() == Constants.USER_TYPE_TEACHER) {
            ApeUserRole apeUserRole = new ApeUserRole();
            apeUserRole.setUserId(apeUser.getId());
            QueryWrapper<ApeRole> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().eq(ApeRole::getRoleKey,Constants.ROLE_KEY_TEACHER);
            ApeRole role = apeRoleService.getOne(queryWrapper1);
            apeUserRole.setRoleId(role.getId());
            apeUserRoles.add(apeUserRole);
        }
        apeUserRoleService.saveBatch(apeUserRoles);
        return Result.success();
    }

    /** 校验用户 */
    public boolean checkAccount(ApeUser apeUser) {
        QueryWrapper<ApeUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUser::getLoginAccount,apeUser.getLoginAccount());
        int count = apeUserService.count(queryWrapper);
        return count <= 0;
    }

    /** 删除用户 */
    @Log(name = "删除用户", type = BusinessType.DELETE)
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("removeUser")
    @RequiresRoles("admin")
    public Result removeUser(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                boolean remove = apeUserService.removeById(id);
                QueryWrapper<ApeUserRole> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(ApeUserRole::getUserId,id);
                apeUserRoleService.remove(queryWrapper);
            }
            return Result.success();
        } else {
            return Result.fail("角色id不能为空！");
        }
    }

    @Log(name = "修改密码", type = BusinessType.UPDATE)
    @PostMapping("changePassword")
    @RequiresAuthentication
    public Result changePassword(@RequestBody JSONObject json) {
        ApeUser currentUser = ShiroUtils.getUserInfo();
        String password = json.getString("password");
        ApeUser apeUser = apeUserService.getById(currentUser.getId());
        boolean decrypt = PasswordUtils.decrypt(password, apeUser.getPassword());
        if (!decrypt && PasswordUtils.isMd5Format(apeUser.getSalt())) {
            decrypt = PasswordUtils.decryptMd5(password, apeUser.getPassword(), apeUser.getSalt());
        }
        if (decrypt) {
            String newPassword = json.getString("newPassword");
            String validateMessage = AccountPasswordValidator.validatePasswordForUser(apeUser, newPassword);
            if (validateMessage != null) {
                return Result.fail(validateMessage);
            }
            String bcryptHash = PasswordUtils.encrypt(newPassword);
            apeUser.setPassword(bcryptHash);
            apeUser.setSalt("");
            apeUser.setPwdUpdateDate(new Date());
            boolean update = apeUserService.updateById(apeUser);
            if (update) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } else {
            return Result.fail("旧密码不正确");
        }
    }

    /** 重置密码 */
    @Log(name = "重置密码", type = BusinessType.UPDATE)
    @PostMapping("resetPassword")
    @RequiresRoles("admin")
    public Result resetPassword(@RequestBody JSONObject json) {
        ApeUser currentUser = ShiroUtils.getUserInfo();
        if (currentUser.getUserType() == null || currentUser.getUserType() != Constants.USER_TYPE_ADMIN) {
            return Result.fail("无权限执行此操作");
        }
        String id = json.getString("id");
        String newPassword = json.getString("newPassword");
        ApeUser apeUser = apeUserService.getById(id);
        String validateMessage = AccountPasswordValidator.validatePasswordForUser(apeUser, newPassword);
        if (validateMessage != null) {
            return Result.fail(validateMessage);
        }
        // 检查新旧密码是否相同（password列=盐值, salt列=哈希）
        boolean same = PasswordUtils.decrypt(newPassword, apeUser.getPassword());
        if (!same && PasswordUtils.isMd5Format(apeUser.getSalt())) {
            same = PasswordUtils.decryptMd5(newPassword, apeUser.getPassword(), apeUser.getSalt());
        }
        if (same) {
            return Result.fail("新密码不可和旧密码相同！");
        }
        apeUser.setPassword(PasswordUtils.encrypt(newPassword));
        apeUser.setSalt("");
        apeUser.setPwdUpdateDate(new Date());
        boolean update = apeUserService.updateById(apeUser);
        if (update) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 获取登陆用户信息 */
    @Log(name = "获取登陆用户信息", type = BusinessType.OTHER)
    @GetMapping("getUserInfo")
    @RequiresAuthentication
    public Result getUserInfo() {
        ApeUser user = ShiroUtils.getUserInfo();
        ApeUser apeUser = apeUserService.getById(user.getId());
        return Result.success(apeUser);
    }

    /** 修改个人信息 */
    @Log(name = "修改个人信息", type = BusinessType.UPDATE)
    @PostMapping("setUserInfo")
    @RequiresAuthentication
    public Result setUserInfo(@RequestBody ApeUser apeUser) {
        ApeUser userInfo = ShiroUtils.getUserInfo();
        ApeUser profileUpdate = new ApeUser()
                .setId(userInfo.getId())
                .setUserName(apeUser.getUserName())
                .setEmail(apeUser.getEmail())
                .setTel(apeUser.getTel())
                .setSex(apeUser.getSex())
                .setSchool(apeUser.getSchool())
                .setCountry(apeUser.getCountry())
                .setMajor(apeUser.getMajor())
                .setAgree(apeUser.getAgree())
                .setAge(apeUser.getAge())
                .setBirth(apeUser.getBirth())
                .setFlair(apeUser.getFlair())
                .setAddress(apeUser.getAddress())
                .setWorkDate(apeUser.getWorkDate())
                .setRemark(apeUser.getRemark());
        apeUserService.updateById(profileUpdate);
        return Result.success();
    }

    /** 修改个人头像 */
    @Log(name = "修改个人头像", type = BusinessType.UPDATE)
    @PostMapping("setUserAvatar/{id}")
    @RequiresAuthentication
    public Result setUserAvatar(@PathVariable("id") String id,@RequestParam("file") MultipartFile avatar) {
        if(StringUtils.isBlank(id)){
            return Result.fail("用户id为空!");
        }
        ApeUser currentUser = ShiroUtils.getUserInfo();
        if (currentUser == null || !id.equals(currentUser.getId())) {
            throw new UnauthorizedException("只能修改当前用户头像");
        }
        ApeUser apeUser = apeUserService.getById(id);
        if(avatar.isEmpty()){
            return Result.fail("上传的头像不能为空!");
        }
        String coverType = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
        if ("jpeg".equals(coverType)  || "gif".equals(coverType) || "png".equals(coverType) || "bmp".equals(coverType)  || "jpg".equals(coverType)) {
            //文件路径
            String filePath = System.getProperty("user.dir")+System.getProperty("file.separator")+"img";
            //文件名=当前时间到毫秒+原来的文件名
            String fileName = id + "."+ coverType;
            //如果文件路径不存在，新增该路径
            File file1 = new File(filePath);
            if(!file1.exists()){
                boolean mkdir = file1.mkdir();
            }
            //现在的文件地址
            if (StringUtils.isNotBlank(apeUser.getAvatar())) {
                String oldAvatar = apeUser.getAvatar();
                String oldFileName = oldAvatar.substring(oldAvatar.lastIndexOf("/") + 1);
                if (oldFileName.startsWith(id + ".")) {
                    File now = new File(filePath + System.getProperty("file.separator") + oldFileName);
                    if (now.exists()) {
                        boolean delete = now.delete();
                    }
                }
            }
            //实际的文件地址
            File dest = new File(filePath + System.getProperty("file.separator") + fileName);
            //存储到数据库里的相对文件地址
            String storeImgPath = "/img/"+fileName;
            try {
                avatar.transferTo(dest);
                //更新头像
                apeUser.setAvatar(storeImgPath);
                apeUserService.updateById(apeUser);
                return Result.success(storeImgPath);
            } catch (IOException e) {
                return Result.fail("上传失败");
            }
        } else {
            return Result.fail("请选择正确的图片格式");
        }
    }

    private String resolveDefaultAvatar(Integer userType, Integer sex) {
        if (Integer.valueOf(Constants.USER_TYPE_ADMIN).equals(userType)) {
            return Constants.DEFAULT_ADMIN_AVATAR;
        }
        if (Integer.valueOf(Constants.USER_TYPE_TEACHER).equals(userType)) {
            return isFemale(sex) ? Constants.DEFAULT_TEACHER_FEMALE_AVATAR : Constants.DEFAULT_TEACHER_MALE_AVATAR;
        }
        return isFemale(sex) ? Constants.DEFAULT_STUDENT_FEMALE_AVATAR : Constants.DEFAULT_STUDENT_MALE_AVATAR;
    }

    private boolean isFemale(Integer sex) {
        return Integer.valueOf(1).equals(sex);
    }

}
