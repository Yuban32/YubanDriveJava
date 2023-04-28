package com.yuban32.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

/**
 * @author Yuban32
 * @ClassName UserControllerUtils
 * @Description 用户控制层工具类
 * @Date 2023年03月02日
 */
public class UserControllerUtils {

    public String encryptionPassword(String uuid,String password){
        //使用UUID工具类来随机生成 然后依据uuid来做salt
        String salt = String.valueOf(ByteSource.Util.bytes(uuid));
        //加密
        String encryptionPassword = new SimpleHash("MD5",password,salt,3).toHex();
        return encryptionPassword;
    }

}
