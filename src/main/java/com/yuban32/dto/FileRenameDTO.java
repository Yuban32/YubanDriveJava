package com.yuban32.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author Yuban32
 * @ClassName FileRenameDTO
 * @Description
 * @Date 2023年04月29日
 */
@Data
public class FileRenameDTO {
    @NotBlank(message = "新文件名不能为空")
    private String targetFileName;
    @NotBlank(message = "当前文件名不能为空")
    private String currentFileName;
    @NotBlank(message = "当前文件夹UUID不能为空")
    private String folderUUID;
    private String userName;
}
