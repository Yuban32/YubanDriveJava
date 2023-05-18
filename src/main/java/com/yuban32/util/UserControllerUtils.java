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
        //根据用户注册的时候生成的uuid来创建一个盐
        //将uuid转换成一个字节数组 然后再转换成字符串
        String salt = String.valueOf(ByteSource.Util.bytes(uuid));
        //加密
        //使用Shiro自带的SimpleHash工具类  加密的密码是由 MD5、用户密码后迭代3次 将数据转为16进制字符串
        String encryptionPassword = new SimpleHash("MD5",password,salt,3).toHex();
        return encryptionPassword;
    }

}
