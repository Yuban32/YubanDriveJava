package com.yuban32.shiro.realm;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.User;
import com.yuban32.service.UserService;
import com.yuban32.shiro.AccountProfile;
import com.yuban32.shiro.JwtToken;
import com.yuban32.util.JWTUtils;
import io.jsonwebtoken.Claims;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author Yuban32
 * @ClassName AccountRealm
 * @Description Shiro自定义Realm
 * @Date 2023年02月22日
 */
@Slf4j
@Component
public class AccountRealm extends AuthorizingRealm {
    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    UserService userService;

    @Value("${Shiro.Roles.admin}")
    private String RolesAdmin;
    @Value("${Shiro.Roles.user}")
    private String RolesUser;
    @Override
    public boolean supports(AuthenticationToken token){
        log.debug("authenticationToken supports,{}",token);
        return token instanceof JwtToken;
    }

    /***
     * @description 授权 重写这个方法
     * @param principalCollection
     * @return AuthorizationInfo
     **/
    @SneakyThrows
    @Override
    //TODO 权限管理
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.info("进入授权类");
        //从Shiro获取用户名
        AccountProfile username = (AccountProfile) principalCollection.iterator().next();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        User user = userService.getOne(new QueryWrapper<User>().eq("username",username.getUsername()));
        if (user.getRole().equals(RolesAdmin)){
            authorizationInfo.setRoles(Collections.singleton(RolesAdmin));
        } else if (user.getRole().equals(RolesUser)) {
            authorizationInfo.setRoles(Collections.singleton(RolesUser));
        }
        return authorizationInfo;
    }
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //获取
        JwtToken jwtToken = (JwtToken) token;
        Claims claimByToken = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal());
        String userName = claimByToken.getSubject();
        User user = userService.getOne(new QueryWrapper<User>().eq("username",userName));
        //非空判断
        if (user == null) {
            throw new UnknownAccountException("账户不存在");
        }
        if (user.getStatus() == 1) {
            throw new LockedAccountException("账户已被锁定");
        }
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(user, profile);

        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials().toString(), userName);
    }
}
