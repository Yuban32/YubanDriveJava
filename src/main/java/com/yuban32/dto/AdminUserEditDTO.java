package com.yuban32.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Yuban32
 * @ClassName AdminUserEditDTO
 * @Description 用户编辑DTO
 * @Date 2023年05月10日
 */
@Data

public class AdminUserEditDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    @NotNull(message = "用户名不能为空")
    private String username;
    private String password;
    @NotNull(message = "用户头像不能为空")
    private String avatar;
    private String email;
    @NotNull(message = "用户角色不能为空")
    private String role;
    @NotNull(message = "用户状态不能为空")
    private Integer status;
    @NotNull(message = "总存储配额不能为空")
    private Double totalStorage;

}
