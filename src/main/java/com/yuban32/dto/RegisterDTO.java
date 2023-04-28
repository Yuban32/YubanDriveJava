package com.yuban32.dto;



import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * @author Yuban32
 * @ClassName RegisterDTO
 * @Description 注册字段校验
 * @Date 2023年02月22日
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    //todo  暂时这些,以后有需要再加新的字段
}
