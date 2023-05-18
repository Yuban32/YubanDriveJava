package com.yuban32.shiro;

import cn.hutool.json.JSONUtil;
import com.yuban32.response.Result;
import com.yuban32.service.BlackListTokenService;
import com.yuban32.util.JWTUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Yuban32
 * @ClassName JWTFilter
 * @Description
 * @Date 2023年02月22日
 */
@Component
@Slf4j
public class JWTFilter extends AuthenticatingFilter {

    @Autowired
    JWTUtils jwtUtils;
    @Autowired
    BlackListTokenService blackListTokenService;

    /**
     * @description 第一步执行 处理跨域的预处理操作
     * @param request
     * @param response
     * @return boolean
     **/
    @Override
    protected boolean preHandle(ServletRequest request,ServletResponse response) throws Exception{
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }

        return super.preHandle(request, response);
    }
    /**
     * @description 第二步执行 判断当前请求是否允许访问
     * @param request
     * @param response
     * @param mappedValue
     * @return boolean
     **/
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        //获取jwt token
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader("Authorization");
        if(StringUtils.isEmpty(token)){
            return true;
        }
        Claims claim = jwtUtils.getClaimByToken(token);

        if(claim != null){
            //调用查询黑名单方法 当前jwt在黑名单内则不允许访问控制器 直接转到onAccessDenied方法
            String blackListTokenByUsername = blackListTokenService.isBlackListTokenByUsername(claim.getSubject());
            if(token.equals(blackListTokenByUsername)){
                return false;
            }
        }
        try{
            SecurityUtils.getSubject().login(new JwtToken(token));
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @description 第三步 执行登录操作,因为每次访问后端都相当于是拿着jwt重新登录
     * @param request
     * @param response
     * @return boolean
     **/
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader("Authorization");
        JwtToken jwtToken = new JwtToken(token);

        try{
            //获取当前访问系统的对象
            Subject subject = this.getSubject(request,response);
            //Shiro 会委托给 SecurityManager 进行身份认证
            subject.login(jwtToken);
            //然后调用身份认证成功的方法
            return this.onLoginSuccess(jwtToken,subject,request,response);
        }catch (AuthenticationException e){
            return this.onLoginFailure(jwtToken,e,request,response);
        }

    }
    /**
     * @description 在onAccessDenied中调用
     * @param servletRequest
     * @param servletResponse
     * @return AuthenticationToken
     **/
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if(StringUtils.isEmpty(jwt)){
            return null;
        }
        return new JwtToken(jwt);
    }
    /**
     * @description 在isAccessAllowed返回false时被调用 用于处理拒绝访问的异常情况
     * @param servletRequest
     * @param servletResponse
     * @return boolean
     **/
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if(StringUtils.isEmpty(jwt)){
            return true;
        }else{
//            校验jwt
            Claims claims = jwtUtils.getClaimByToken(jwt);
            if(claims == null || jwtUtils.isTokenExpired(claims.getExpiration())){
                throw new ExpiredCredentialsException("token已失效,请重新登录");
            }
            //jwt有效的话 返回去执行executeLogin
            return executeLogin(servletRequest,servletResponse);
        }
    }
    /**
     * @description 当身份认证失败的时候被调用,用来处理身份认证异常情况
     * @param token
     * @param e
     * @param request
     * @param response
     * @return boolean
     **/
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        Throwable throwable = e.getCause() == null ? e : e.getCause();
        Result result = Result.error(throwable.getMessage());
        String json = JSONUtil.toJsonStr(result);
        try{
            httpServletResponse.getWriter().print(json);
        }catch(IOException ioException){
            e.printStackTrace();
        }
        return false;
    }
}
