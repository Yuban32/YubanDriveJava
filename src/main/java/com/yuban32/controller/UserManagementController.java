package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yuban32.dto.AdminUserEditDTO;
import com.yuban32.dto.AdminUserDeleteDTO;
import com.yuban32.entity.User;
import com.yuban32.entity.UserStorageQuota;
import com.yuban32.mapper.UserStorageQuotaMapper;
import com.yuban32.response.Result;
import com.yuban32.service.UserService;
import com.yuban32.util.JWTUtils;
import com.yuban32.util.UserControllerUtils;
import com.yuban32.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName UserManagementController
 * @Description 用户管理
 * @Date 2023年05月10日
 */

@Slf4j
@RestController
@RequestMapping("/userManagement")
public class UserManagementController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserStorageQuotaMapper userStorageQuotaMapper;
    @Autowired
    private JWTUtils jwtUtils;

    //管理员权限
    @PostMapping("/edit")
    public Result userEdit(@Valid @RequestBody AdminUserEditDTO userEditDTO) {
        boolean userStorageEditSuccess = false;



        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        if (!userEditDTO.getUsername().isEmpty()) {
            updateWrapper.set("username", userEditDTO.getUsername());
        }
        if (!userEditDTO.getPassword().isEmpty()) {
//            加密加盐
            User getUUID = userService.getOne(new QueryWrapper<User>().eq("id", userEditDTO.getId()));
            String encryptionPassword = new UserControllerUtils().encryptionPassword(getUUID.getUuid(), userEditDTO.getPassword());
            updateWrapper.set("password", encryptionPassword);
        }
        if (!userEditDTO.getAvatar().isEmpty()) {
            updateWrapper.set("avatar", userEditDTO.getAvatar());
        }
        if (!userEditDTO.getEmail().isEmpty()) {
            updateWrapper.set("email", userEditDTO.getEmail());
        }
        if (!userEditDTO.getRole().isEmpty()) {
            updateWrapper.set("role", userEditDTO.getRole());
        }
        if (userEditDTO.getStatus() != null) {
            updateWrapper.set("status", userEditDTO.getStatus());
        }
        updateWrapper.eq("id", userEditDTO.getId());
        boolean success = userService.update(new User(),updateWrapper);
        if (!success) {
            return new Result(500, "修改失败", null);
        }
        User existUser = userService.getOne(new QueryWrapper<User>().eq("id", userEditDTO.getId()));
        if (userEditDTO.getTotalStorage() != null) {
            userStorageEditSuccess = userStorageQuotaMapper.userTotalStorageEdit(userEditDTO.getTotalStorage(), existUser.getUuid());
        }
        if (success || userStorageEditSuccess) {
            UserStorageQuota userStorageQuota = userStorageQuotaMapper.selectOne(new QueryWrapper<UserStorageQuota>().eq("uuid", existUser.getUuid()));
            UserVO userVO = new UserVO();
            userVO.setId(existUser.getId());
            userVO.setUsername(existUser.getUsername());
            userVO.setAvatar(existUser.getAvatar());
            userVO.setEmail(existUser.getEmail());
            userVO.setStatus(userVO.getStatus());
            userVO.setTotalStorage(userStorageQuota.getTotalStorage());
            userVO.setUsedStorage(userStorageQuota.getUsedStorage());
            userVO.setRole(existUser.getRole());

            return new Result(200, "修改成功", userVO);
        } else {
            return new Result(500, "修改失败", null);
        }

    }

    @PostMapping("/delete")
    public Result userDelete(@Validated @RequestBody AdminUserDeleteDTO userId , HttpServletRequest request){
        String user = jwtUtils.getClaimByToken(request.getHeader("authorization")).getSubject();
        QueryWrapper<User> uidQueryWrapper = new QueryWrapper<User>().eq("id", userId.getUserId());
        User getUserInfo = userService.getOne(uidQueryWrapper);
        if(getUserInfo.getUsername().equals(user)){
            return Result.error("不能删除当前登录的账号");
        }
        if(getUserInfo.getRole().equals("admin")){
            List<User> adminList = userService.list(new QueryWrapper<User>().eq("role", "admin"));
            if (adminList.size()==1){
                return Result.error("不能删除最后一个角色为管理员的用户");
            }
        }
        boolean id = userService.remove(uidQueryWrapper);
        int userStorage = userStorageQuotaMapper.delete(new QueryWrapper<UserStorageQuota>().eq("uuid", getUserInfo.getUuid()));
        if (id && userStorage==1){
            return new Result(200,"用户已删除",null);
        }else {
            return Result.error("删除失败");
        }

    }
    @GetMapping("/userList")
    public Result getUserList(HttpServletRequest httpServletRequest){
        ArrayList<UserVO> userVOS = new ArrayList<>();
        List<User> list = userService.list();

        if(!list.isEmpty()){
            for (User user : list) {
                UserStorageQuota userStorageQuotas = userStorageQuotaMapper.selectOne(new QueryWrapper<UserStorageQuota>().eq("uuid", user.getUuid()));
//                log.info("{}",userStorageQuotas);
                if(userStorageQuotas!=null){
                    UserVO temp = new UserVO();
                    temp.setId(user.getId());
                    temp.setUsedStorage(userStorageQuotas.getUsedStorage());
                    temp.setTotalStorage(userStorageQuotas.getTotalStorage());
                    temp.setAvatar(user.getAvatar());
                    temp.setRole(user.getRole());
                    temp.setUsername(user.getUsername());
                    temp.setEmail(user.getEmail());
                    temp.setStatus(user.getStatus());
                    userVOS.add(temp);
                }
            }
            return Result.success("查询成功", userVOS);
        }
        return Result.error("查询失败,无数据",null);
    }
}
