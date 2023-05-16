package com.yuban32.vo;

import lombok.Data;

/**
 * @author Yuban32
 * @ClassName UserVO
 * @Description 返回用户登录成功后的信息
 * @Date 2023年02月27日
 */
@Data
public class UserVO {
    private static final long serialVersionUID = -668666237985927833L;
    private Long id;
    private String username;
    private String avatar;
    private String email;
    private int status;
    private double totalStorage;
    private double usedStorage;
    private String role;
}
