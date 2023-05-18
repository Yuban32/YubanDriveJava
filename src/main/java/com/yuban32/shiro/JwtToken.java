package com.yuban32.shiro;

import org.apache.shiro.authc.AuthenticationToken;

import java.io.Serializable;

/**
 * @author Yuban32
 * @ClassName JwtToken
 * @Description 自定义AuthenticationToken实现类
 * @Date 2023年02月22日
 */
public class JwtToken implements AuthenticationToken, Serializable {
    private static final long serialVersionUID = 1841491628743017587L;
    private String token;
    public JwtToken(String token){
        this.token = token;
    }
    //当前用户身份信息
    @Override
    public Object getPrincipal() {
        return token;
    }

    //当前用户凭证信息
    @Override
    public Object getCredentials() {
        return token;
    }
}
