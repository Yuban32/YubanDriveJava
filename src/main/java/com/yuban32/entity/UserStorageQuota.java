package com.yuban32.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Yuban32
 * @ClassName UserStorageQuota
 * @Description 用户存储空间分配
 * @Date 2023年03月10日
 */
@Data
@TableName("t_user_storage_quota")
@Accessors(chain = true)
public class UserStorageQuota implements Serializable {
    private  static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "uuid")
    private String uuid;
    @TableField(value = "total_storage")
    private double totalStorage;
    @TableField(value = "used_storage")
    private double usedStorage;
    //逻辑删除
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
