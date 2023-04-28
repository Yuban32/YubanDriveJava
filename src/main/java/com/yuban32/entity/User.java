package com.yuban32.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;
import lombok.experimental.Accessors;


import javax.validation.constraints.Email;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Yuban32
 * @ClassName User
 * @Description 用户实体类
 * @Date 2023年02月22日
 */
@Data
@TableName("t_user")
@Accessors(chain = true)
public class User implements Serializable {
    private  static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "uuid")
    private String uuid;
    private String username;
    private String password;
    private String avatar;

    @Email(message = "邮箱格式不正确")
    private String email;

    private Integer status;

    private String role;

    private LocalDateTime created;

    @TableField(value = "last_login")
    private LocalDateTime lastLogin;

    //逻辑删除
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
