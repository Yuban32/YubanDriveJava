package com.yuban32.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Yuban32
 * @ClassName Folder
 * @Description
 * @Date 2023年03月22日
 */
@Data
@TableName("t_folder")
@Accessors(chain = true)
public class Folder implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id" , type = IdType.AUTO)
    private int id;
    private String username;
    @TableField(value = "folder_relative_path")
    private String folderRelativePath;
    @TableField(value = "folder_uuid")
    private String folderUUID;
    @TableField(value = "folder_name")
    private String folderName;
    @TableField(value = "parent_folder_uuid")
    private String parentFolderUUID;
    @TableField(value = "folder_create_time")
    private LocalDateTime folderCreateTime;
    @TableField(value = "folder_status")
    private int folderStatus;
    public Folder(String username, String folderRelativePath, String folderUUID, String folderName, String parentFolderUUID,LocalDateTime folderCreateTime) {
        this.username = username;
        this.folderRelativePath = folderRelativePath;
        this.folderUUID = folderUUID;
        this.folderName = folderName;
        this.parentFolderUUID = parentFolderUUID;
        this.folderCreateTime = folderCreateTime;
    }

    public Folder() {

    }
}
