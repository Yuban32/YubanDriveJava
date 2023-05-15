package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yuban32.dto.UserEditDTO;
import com.yuban32.entity.User;
import com.yuban32.entity.UserStorageQuota;
import com.yuban32.mapper.UserStorageQuotaMapper;
import com.yuban32.response.Result;
import com.yuban32.service.UserService;
import com.yuban32.util.UserControllerUtils;
import com.yuban32.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

    //管理员权限
    @PostMapping("/edit")
    public Result userEdit(@Valid @RequestBody UserEditDTO userEditDTO) {
        log.info("{}", userEditDTO);
        boolean userStorageEditSuccess = false;

        String encryptionPassword = null;
        if(userEditDTO.getPassword()!=null){
            User getUUID = userService.getOne(new QueryWrapper<User>().eq("id", userEditDTO.getId()));
            encryptionPassword = new UserControllerUtils().encryptionPassword(getUUID.getUuid(), userEditDTO.getPassword());
            userEditDTO.setPassword(encryptionPassword);
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        if (userEditDTO.getUserName() != null) {
            updateWrapper.set("username", userEditDTO.getUserName());
        }
        if (userEditDTO.getPassword() != null) {
            updateWrapper.set("password", userEditDTO.getPassword());
        }
        if (userEditDTO.getAvatar() != null) {
            updateWrapper.set("avatar", userEditDTO.getAvatar());
        }
        if (userEditDTO.getEmail() != null) {
            updateWrapper.set("email", userEditDTO.getEmail());
        }
        if (userEditDTO.getRole() != null) {
            updateWrapper.set("role", userEditDTO.getRole());
        }
        if (userEditDTO.getStatus() != null) {
            updateWrapper.set("status", userEditDTO.getStatus());
        }
        updateWrapper.eq("id", userEditDTO.getId());

        log.info("{}",encryptionPassword);
        boolean success = userService.update(new User(),updateWrapper);
        if (!success) {
            return new Result(500, "修改失败", null);
        }
        User exitUser = userService.getOne(new QueryWrapper<User>().eq("id", userEditDTO.getId()));
        if (userEditDTO.getTotalStorage() != null) {
            userStorageEditSuccess = userStorageQuotaMapper.userTotalStorageEdit(userEditDTO.getTotalStorage(), exitUser.getUuid());
        }
        if (success || userStorageEditSuccess) {
            UserStorageQuota userStorageQuota = userStorageQuotaMapper.selectOne(new QueryWrapper<UserStorageQuota>().eq("uuid", exitUser.getUuid()));
            UserVO userVO = new UserVO();
            userVO.setId(exitUser.getId());
            userVO.setUsername(exitUser.getUsername());
            userVO.setAvatar(exitUser.getAvatar());
            userVO.setEmail(exitUser.getEmail());
            userVO.setTotalStorage(userStorageQuota.getTotalStorage());
            userVO.setUsedStorage(userStorageQuota.getUsedStorage());
            userVO.setRole(exitUser.getRole());

            return new Result(200, "修改成功", userVO);
        } else {
            return new Result(500, "修改失败", null);
        }

    }
}
