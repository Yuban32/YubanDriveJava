package com.yuban32.dto;



import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * @author Yuban32
 * @ClassName LoginDto
 * @Description 登录字段校验
 * @Date 2023年02月22日
 */
@Data
public class LoginDto {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

}
