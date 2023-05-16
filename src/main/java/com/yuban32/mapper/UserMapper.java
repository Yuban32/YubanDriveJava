package com.yuban32.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuban32.dto.AdminUserEditDTO;
import com.yuban32.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Yuban32
 * @ClassName UserMapper
 * @Description 用户Mapper接口
 * @Date 2023年02月22日
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    int insertUserResultID(User user);
    int updateLastLoginTimeByID(User user);
    boolean userEdit(AdminUserEditDTO userEditDTO);
}
