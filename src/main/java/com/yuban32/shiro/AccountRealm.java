package com.yuban32.shiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.User;
import com.yuban32.service.UserService;
import com.yuban32.util.JWTUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Yuban32
 * @ClassName AccountRealm
 * @Description Shiro自定义Realm
 * @Date 2023年02月22日
 */
//@Component
public class AccountRealm extends AuthorizingRealm {
    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    UserService userService;

    @Override
    public boolean supports(AuthenticationToken token){return token instanceof JwtToken;}

    /***
     * @description 授权 重写这个方法
     * @param principalCollection
     * @return AuthorizationInfo
     **/
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //从Shiro获取用户名
        AccountProfile username = (AccountProfile) principalCollection.iterator().next();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        User user = userService.getOne(new QueryWrapper<User>().eq("username",username.getUsername()));

        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        return null;
    }
}
