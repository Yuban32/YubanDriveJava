package com.yuban32.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Yuban32
 * @ClassName FileAndFolderVO
 * @Description 文件VO 用于封装成统一数据格式给前端来展示数据  此VO包含了File和Folder类
 * @Date 2023年04月05日
 */
@Data
public class FileAndFolderVO {
    //判断类型 file||folder
    private String type;
    //file的类型 zip||exe
    private String category;
    //文件后缀名 .zip
    private String fileExtension;
    //文件名
    private String name;
    //文件的MD5||文件夹的UUID
    private String fileUUID;
    //父文件夹的UUID
    private String parentFileUUID;
    //文件大小
    private Long size;
    //上传者
    private String uploader;
    //上传时间||创建时间
    private LocalDateTime createdTime;
    // 路径
    private String relativePath;
    //图片URL
    private String thumbnailURL;
    private String fullSizeImageURL;
}
