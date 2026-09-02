package com.ape.apeadmin.controller.login;

import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apecommon.utils.JwtUtil;
import com.ape.apecommon.utils.PasswordUtils;
import com.ape.apecommon.utils.UserAgentUtil;
import com.ape.apeadmin.validator.AccountPasswordValidator;
import com.ape.apeframework.event.LoginLogEvent;
import com.ape.apeframework.utils.RedisUtils;
import com.ape.apeframework.utils.RequestUtils;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeLoginLog;
import com.ape.apesystem.domain.ApeMenu;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeLoginLogService;
import com.ape.apesystem.service.ApeMenuService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @description: 登陆
 */
@Controller
@ResponseBody
@RequestMapping("login")
public class LoginController {

    @Autowired
    private ApeUserService apeUserService;

    @Autowired
    private ApeLoginLogService apeLoginLogService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ApeMenuService apeMenuService;

    @Autowired
    private RedisUtils redisUtils;

    @PostMapping()
    public Result login(HttpServletRequest request,@RequestBody JSONObject jsonObject) {
        return authenticate(request, jsonObject, false);
    }

    /**
     * 学生门户使用独立入口，避免教师账号进入学生端页面。
     */
    @PostMapping("student")
    public Result studentLogin(HttpServletRequest request,@RequestBody JSONObject jsonObject) {
        return authenticate(request, jsonObject, true);
    }

    private Result authenticate(HttpServletRequest request, JSONObject jsonObject, boolean studentPortal) {
        String ipAddr = RequestUtils.getRemoteHost(request);
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        QueryWrapper<ApeUser> query = new QueryWrapper<>();
        query.lambda().eq(ApeUser::getLoginAccount,username);
        ApeUser apeUser = apeUserService.getOne(query);
        if (apeUser == null) {
            saveLoginLog(request,"用户名不存在",username,ipAddr,1);
            return Result.fail("用户名不存在！");
        }
        // 验证密码：优先 BCrypt，回退 MD5 兼容旧格式
        // 注意：旧格式中 password 列存的是盐值，salt 列存的是 MD5 哈希
        boolean decrypt = PasswordUtils.decrypt(password, apeUser.getPassword());
        boolean md5Decrypt = false;
        if (!decrypt && PasswordUtils.isMd5Format(apeUser.getSalt())) {
            md5Decrypt = PasswordUtils.decryptMd5(password, apeUser.getPassword(), apeUser.getSalt());
        }
        if (!decrypt && !md5Decrypt) {
            saveLoginLog(request,"用户名或密码错误",username,ipAddr,1);
            return Result.fail("用户名或密码错误！");
        }
        if (apeUser.getStatus() == 1) {
            saveLoginLog(request,"用户被禁用",username,ipAddr,1);
            return Result.fail("用户被禁用！");
        }
        if (!studentPortal && Integer.valueOf(Constants.USER_TYPE_STUDENT).equals(apeUser.getUserType())) {
            String message = "学生账号请从学生端入口登录！";
            saveLoginLog(request, message, username, ipAddr, 1);
            return Result.fail(message);
        }
        if (studentPortal && !Integer.valueOf(Constants.USER_TYPE_STUDENT).equals(apeUser.getUserType())) {
            String message = Integer.valueOf(Constants.USER_TYPE_TEACHER).equals(apeUser.getUserType())
                    ? "教师账号请从管理端入口登录！"
                    : "管理员账号请从管理端入口登录！";
            saveLoginLog(request, message, username, ipAddr, 1);
            return Result.fail(message);
        }
        // MD5旧格式登录成功后，自动升级为BCrypt
        if (md5Decrypt) {
            String bcryptHash = PasswordUtils.encrypt(password);
            apeUser.setPassword(bcryptHash);
            apeUser.setSalt("");
            apeUserService.updateById(apeUser);
        }
        //密码正确生成token返回
        String token = JwtUtil.sign(apeUser.getId());
        JSONObject json = new JSONObject();
        json.put("token", token);
        saveLoginLog(request,"登陆成功",username,ipAddr,0);
        return Result.success(json);
    }

    @PostMapping("register")
    public Result register(@RequestBody ApeUser apeUser) {
        if (apeUser == null || (!Integer.valueOf(Constants.USER_TYPE_TEACHER).equals(apeUser.getUserType())
                && !Integer.valueOf(Constants.USER_TYPE_STUDENT).equals(apeUser.getUserType()))) {
            return Result.fail("公开注册不允许创建管理员账号");
        }
        String validateMessage = AccountPasswordValidator.validateUserCredentials(apeUser);
        if (validateMessage != null) {
            return Result.fail(validateMessage);
        }
        boolean account = checkAccount(apeUser);
        if (!account) {
            return Result.fail("登陆账号已存在不可重复！");
        }
        // BCrypt加密
        String bcryptHash = PasswordUtils.encrypt(apeUser.getPassword());
        apeUser.setPassword(bcryptHash);
        apeUser.setSalt("");
        apeUser.setAvatar(resolveDefaultAvatar(apeUser.getUserType(), apeUser.getSex()));
        apeUser.setPwdUpdateDate(new Date());
        boolean save = apeUserService.save(apeUser);
        if (save) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    /** 校验用户 */
    public boolean checkAccount(ApeUser apeUser) {
        QueryWrapper<ApeUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApeUser::getLoginAccount,apeUser.getLoginAccount())
                .eq(ApeUser::getUserType,apeUser.getUserType());
        int count = apeUserService.count(queryWrapper);
        return count <= 0;
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

    @Log(name = "登出", type = BusinessType.OTHER)
    @GetMapping("logout")
    public Result logout() {
        ApeUser user = ShiroUtils.getUserInfo();
        redisUtils.remove(Constants.PREFIX_USER_TOKEN + user.getId());
        return Result.success();
    }

    @Log(name = "验证密码", type = BusinessType.OTHER)
    @GetMapping("verPassword")
    public Result verPassword(HttpServletRequest request,@RequestParam("password") String password) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录");
        }
        ApeUser apeUser = apeUserService.getById(user.getId());
        String ipAddr = RequestUtils.getRemoteHost(request);
        if (apeUser.getStatus() == 1) {
            saveLoginLog(request,"用户被禁用",apeUser.getLoginAccount(),ipAddr,1);
            return Result.fail("用户被禁用！");
        }
        // 支持 BCrypt 和旧 MD5 格式（password列=盐值, salt列=哈希）
        boolean decrypt = PasswordUtils.decrypt(password, apeUser.getPassword());
        if (!decrypt && PasswordUtils.isMd5Format(apeUser.getSalt())) {
            decrypt = PasswordUtils.decryptMd5(password, apeUser.getPassword(), apeUser.getSalt());
        }
        if (!decrypt) {
            saveLoginLog(request,"验证密码错误",apeUser.getLoginAccount(),ipAddr,1);
            return Result.fail("用户名或密码错误！");
        }
        saveLoginLog(request,"验证成功",apeUser.getLoginAccount(),ipAddr,0);
        return Result.success();
    }


    public void saveLoginLog(HttpServletRequest request,String msg,String username,String ipAddr,Integer state) {
        String agent = request.getHeader("User-Agent");
        String userAgent = UserAgentUtil.getUserAgent(agent);
        String browser = UserAgentUtil.judgeBrowser(userAgent);
        ApeLoginLog apeLoginLog = new ApeLoginLog();
        apeLoginLog.setUserName(username);
        apeLoginLog.setLoginIp(ipAddr);
        apeLoginLog.setBrowser(browser);
        apeLoginLog.setOs(userAgent);
        apeLoginLog.setStatus(state);
        apeLoginLog.setLoginTime(new Date());
        apeLoginLog.setMsg(msg);
        eventPublisher.publishEvent(new LoginLogEvent(apeLoginLog));
    }

}
