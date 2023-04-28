package com.yuban32.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Yuban32
 * @ClassName JWTUtils
 * @Description JWT工具类
 * @Date 2023年02月22日
 */

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "yuban32.jwt")
public class JWTUtils {
    private String secret;
    private long expire;

    public String generateToken(String userName) {
        Date nowDate = new Date();
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + expire);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(userName+"") //用户名
                .setIssuedAt(nowDate) //签发时间
                .setExpiration(expireDate) //过期时间
                .signWith(SignatureAlgorithm.HS512, secret) //设置签名 使用HS512加密和一个secret
                .compact(); //构建完毕返回一个JWT
    }

    /***
     * @description 解析Token
     * @param token
     * @return Claims
     **/
    public Claims getClaimByToken(String token){
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        }catch(Exception e){
            log.warn("validate is token error ,{}",e.getMessage());
            return null;
        }

    }

    /***
     * @description 判断token是否过期
     * @param expiration
     * @return true|false
     **/
    public boolean isTokenExpired(Date expiration){
        return expiration.before(new Date());
    }

}
