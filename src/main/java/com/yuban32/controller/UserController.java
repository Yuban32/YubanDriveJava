package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.dto.LoginDto;
import com.yuban32.dto.RegisterDTO;
import com.yuban32.entity.User;
import com.yuban32.entity.UserStorageQuota;
import com.yuban32.mapper.UserStorageQuotaMapper;
import com.yuban32.response.Result;
import com.yuban32.service.BlackListTokenService;
import com.yuban32.service.UserService;
import com.yuban32.util.JWTUtils;
import com.yuban32.util.LocalDateTimeFormatterUtils;
import com.yuban32.util.UserControllerUtils;
import com.yuban32.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * @author Yuban32
 * @ClassName UserController
 * @Description 用户控制类
 * @Date 2023年02月22日
 */
@Slf4j
@RestController
@RequestMapping("/user")
//@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    BlackListTokenService blackListTokenService;
    @Autowired
    UserStorageQuotaMapper userStorageQuotaMapper;

    @Value("${yuban32.jwt.expire}")
    private int expire;

    @RequiresAuthentication
    @GetMapping("/test")
    public Result test(HttpServletRequest request){

        return Result.success("OK");
    }
    @RequiresRoles(value = "admin")
    @GetMapping("/testRole")
    public Result testRole(){
        return Result.success("OK");
    }

    @GetMapping("/logout")
    public Result logout(HttpServletRequest request){
        SecurityUtils.getSubject().logout();
        String token = request.getHeader("authorization");
        String user = jwtUtils.getClaimByToken(token).getSubject();
//        //退出后把token添加到黑名单
        blackListTokenService.setBlackListToken(user,token);
        return Result.success("用户退出成功");
    }

    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDto loginDto ,HttpServletResponse response){
        UserVO userVO = new UserVO();
        //获取subject对象
        User existsUser = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername()));
        if(existsUser == null){
            return Result.error("用户不存在");
        }
        //加密加盐后再对比
        String salt = String.valueOf(ByteSource.Util.bytes(existsUser.getUuid()));
        String simpleHash = new SimpleHash("MD5",loginDto.getPassword(),salt,3).toHex();
        if(!existsUser.getPassword().equals(simpleHash)){
            return Result.error("密码不正确");
        }
        UserStorageQuota userStorageQuota = userStorageQuotaMapper.selectUserStorageQuotaByUserName(loginDto.getUsername());
        User updateUserLoginTime = new User();
        updateUserLoginTime.setId(existsUser.getId());
        LocalDateTimeFormatterUtils localDateTimeFormatterUtils = new LocalDateTimeFormatterUtils();
        updateUserLoginTime.setLastLogin(localDateTimeFormatterUtils.getStartDateTime());
        userService.updateLastLoginTimeByID(updateUserLoginTime);
        userVO.setId(existsUser.getId());
        userVO.setUsername(existsUser.getUsername());
        userVO.setAvatar(existsUser.getAvatar());
        userVO.setEmail(existsUser.getEmail());
        userVO.setUsedStorage(userStorageQuota.getUsedStorage());
        userVO.setTotalStorage(userStorageQuota.getTotalStorage());
        userVO.setRole(existsUser.getRole());
        String jwt = jwtUtils.generateToken(existsUser.getUsername());
        response.setHeader("Authorization",jwt);
        response.setHeader("Access-Control-Expose-Headers","Authorization");
        return Result.success("登录成功", userVO);
    }
    /***
     * @description 注册控制类
     * @param registerDTO
     * @param response
     * @return Result
     **/
    @PostMapping("/register")
    public Result register(@Validated @RequestBody RegisterDTO registerDTO, HttpServletResponse response){
        log.info("进入注册");
        //用作加密的盐
        String uuid = String.valueOf(UUID.randomUUID());
        //默认用户头像
        String defaultAvatar = "data:image/png;base64,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";
        //先查询所有
        List<User> isFirstAccount = userService.list();
        //先查询前端传过来的用户名是否唯一
        User userName = userService.getOne(new QueryWrapper<User>().eq("username",registerDTO.getUsername()));
        if(isFirstAccount.size() <=0){
            log.info("系统首次注册,分配管理员角色");
            //查询数据库内是否有用户存在,没有则认定为首次注册,默认分配管理员权限
            User tempUser = userRegisterUtil(uuid,registerDTO.getUsername(), registerDTO.getPassword(), defaultAvatar, registerDTO.getEmail(), "admin");
            userService.save(tempUser);

            UserStorageQuota tempUserStorageQuota = new UserStorageQuota();
            tempUserStorageQuota.setUuid(tempUser.getUuid());
            tempUserStorageQuota.setTotalStorage(53687091200D); //给管理员默认分配50GB的存储空间
            userStorageQuotaMapper.insert(tempUserStorageQuota);
            return Result.success("系统首次注册，分配管理员角色");
        }else if (userName == null){
            //用户名不重复的时候
            log.info("新用户注册成功");
            User tempUser = userRegisterUtil(uuid,registerDTO.getUsername(), registerDTO.getPassword(), defaultAvatar, registerDTO.getEmail(), "user");
            userService.save(tempUser);
            UserStorageQuota tempUserStorageQuota = new UserStorageQuota();
            tempUserStorageQuota.setUuid(tempUser.getUuid());
            tempUserStorageQuota.setTotalStorage(21474836480D); //给用户默认分配20GB的存储空间
            userStorageQuotaMapper.initUserStorageQuotaByUUID(tempUserStorageQuota);
            return Result.success("注册成功");
        }else{
            return Result.error("用户名已存在");
        }

    }
    @GetMapping
    public Result getUserInfo(HttpServletRequest request){
        String userName;
        try{
            userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        }catch(Exception e){
            e.printStackTrace();
            return Result.error("用户未登录");
        }
        User users = userService.getOne(new QueryWrapper<User>().eq("username", userName));
        UserStorageQuota userStorageQuota = userStorageQuotaMapper.selectUserStorageQuotaByUserName(userName);
        UserVO userVO = new UserVO();
        userVO.setId(users.getId());
        userVO.setUsername(users.getUsername());
        userVO.setAvatar(users.getAvatar());
        userVO.setEmail(users.getEmail());
        userVO.setTotalStorage(userStorageQuota.getTotalStorage());
        userVO.setUsedStorage(userStorageQuota.getUsedStorage());
        userVO.setRole(users.getRole());
        return Result.success("查询成功", userVO);
    }
    @GetMapping("/userList")
    public Result getUserList(HttpServletRequest httpServletRequest){
        ArrayList<UserVO> userVOS = new ArrayList<>();
        List<User> list = userService.list();

        if(!list.isEmpty()){
            for (User user : list) {
                UserStorageQuota userStorageQuotas = userStorageQuotaMapper.selectOne(new QueryWrapper<UserStorageQuota>().eq("uuid", user.getUuid()));
                log.info("{}",userStorageQuotas);
                if(userStorageQuotas!=null){
                    UserVO temp = new UserVO();
                    temp.setId(user.getId());
                    temp.setUsedStorage(userStorageQuotas.getUsedStorage());
                    temp.setTotalStorage(userStorageQuotas.getTotalStorage());
                    temp.setAvatar(user.getAvatar());
                    temp.setRole(user.getRole());
                    temp.setUsername(user.getUsername());
                    temp.setEmail(user.getEmail());
                    userVOS.add(temp);
                }
            }
            return Result.success("查询成功", userVOS);
        }
        return Result.error("查询失败,无数据",null);
    }
    public User userRegisterUtil(String uuid,String username,String password,String avatar,String email,String roles){
        //此工具类包含了雪花算法和加密加盐的操作

        User tempUser = new User();
        String encryptionPassword = new UserControllerUtils().encryptionPassword(uuid,password);
        tempUser.setUuid(uuid);
        tempUser.setUsername(username);
        tempUser.setPassword(encryptionPassword);
        tempUser.setAvatar(avatar);
        tempUser.setEmail(email);
        tempUser.setRole(roles);
        LocalDateTimeFormatterUtils localDateTimeFormatterUtils = new LocalDateTimeFormatterUtils();
        tempUser.setCreated(localDateTimeFormatterUtils.getStartDateTime());
        return tempUser;
    }
}
