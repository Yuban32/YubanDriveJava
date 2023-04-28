package com.yuban32.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuban32.entity.UserStorageQuota;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Yuban32
 * @ClassName UserStorageQuotaMapper
 * @Description
 * @Date 2023年03月10日
 */
@Mapper
public interface UserStorageQuotaMapper extends BaseMapper<UserStorageQuota> {
    UserStorageQuota selectUserStorageQuotaByUserName(String username);
    int initUserStorageQuotaByUUID(UserStorageQuota userStorageQuota);
}
