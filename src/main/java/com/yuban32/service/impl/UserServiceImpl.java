package com.yuban32.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuban32.dto.AdminUserEditDTO;
import com.yuban32.entity.User;
import com.yuban32.mapper.UserMapper;
import com.yuban32.service.UserService;
import com.yuban32.util.UserControllerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Yuban32
 * @ClassName UserServiceImpl
 * @Description 用户Service实现类
 * @Date 2023年02月22日
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public int updateLastLoginTimeByID(User user) {
        return userMapper.updateLastLoginTimeByID(user);
    }

    @Override
    public boolean userEdit(AdminUserEditDTO userEditDTO) {
        log.info("{}",userEditDTO);
        return userMapper.userEdit(userEditDTO);
    }
}
