package com.yuban32.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Yuban32
 * @ClassName AdminUserDeleteDTO
 * @Description
 * @Date 2023年05月16日
 */
@Data
public class AdminUserDeleteDTO {
    @NotNull(message = "用户ID不能为空")
    private Integer userId;
}
