package com.yuban32.shiro;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yuban32
 * @ClassName AccountProfile
 * @Description Shiro自定义用户文件
 * @Date 2023年02月22日
 */
@Data
public class AccountProfile implements Serializable {
    private  static final long serialVersionUID = 1L;
    private Long id;
    private String uuid;
    private String username;
    private String password;
    private String email;
    private String role;
}
