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
 * @ClassName FileInfo
 * @Description
 * @Date 2023年03月21日
 */
@Data
@TableName("t_file")
@Accessors(chain = true)
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "f_id" ,type = IdType.AUTO)
    private long id;
    @TableField(value = "f_md5")
    private String fileMD5;
    @TableField(value = "f_name")
    private String fileName;
    @TableField(value = "f_size")
    private long fileSize;
    @TableField(value = "f_type")
    private String fileType;
    @TableField(value = "f_parent_id")
    private String fileParentId;
    @TableField(value = "f_absolute_path")
    private String fileAbsolutePath;
    @TableField(value = "f_relative_path")
    private String fileRelativePath;
    @TableField(value = "f_uploader")
    private String fileUploader;
    @TableField(value = "f_upload_time")
    private LocalDateTime fileUploadTime;
    @TableField(value = "f_status")
    private int fileStatus;

}
