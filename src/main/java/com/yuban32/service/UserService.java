package com.yuban32.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.yuban32.dto.AdminUserEditDTO;
import com.yuban32.entity.User;

/**
 * @author Yuban32
 * @ClassName UserService
 * @Description 用户Service接口
 * @Date 2023年02月22日
 */
public interface UserService extends IService<User> {
    int updateLastLoginTimeByID(User user);
    boolean userEdit(AdminUserEditDTO userEditDTO);
}
