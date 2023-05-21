package com.yuban32.service.impl;

import com.yuban32.service.BlackListTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Yuban32
 * @ClassName BlackListServiceImpl
// * @Description 用户退出、修改密码等操作会把JWT加入到黑名单中，代表着这JWT无效化
 * @Date 2023年03月04日
 */
@Slf4j
@Service
public class BlackListServiceImpl implements BlackListTokenService {
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public void setBlackListToken(String username, String token) {
        log.info("<++进入设置黑名单++>");
        String blackList = "BlackList_Token_"+username;
        //将jwt加入到redis中 并设置过期时间为2天 和jwt有效期一致就行
        BoundValueOperations boundValueOperations = redisTemplate.boundValueOps(blackList);
        boundValueOperations.set(token, 2,TimeUnit.DAYS);

    }

    @Override
    public String isBlackListTokenByUsername(String username){
        log.info("通过用户名获取黑名单");
        String blackList = "BlackList_Token_"+username;
        BoundValueOperations boundValueOperations = redisTemplate.boundValueOps(blackList);
        String o = (String) boundValueOperations.get();
        //获取黑名单 不存在就返回null
        if (o != null){
            o.replace("\"","");
        }
        log.info("{}",o);
        return o;

    }
}
