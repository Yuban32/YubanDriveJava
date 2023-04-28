package com.yuban32.service;

/**
 * @author: Yuban32
 * @Date: 2023年03月04日
 */

public interface BlackListTokenService {
    void setBlackListToken(String username,String token);

    String isBlackListTokenByUsername(String username);
}
