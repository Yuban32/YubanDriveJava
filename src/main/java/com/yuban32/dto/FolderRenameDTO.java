package com.yuban32.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author Yuban32
 * @ClassName FolderRenameDTO
 * @Description
 * @Date 2023年04月29日
 */
@Data
public class FolderRenameDTO {
    @NotBlank(message = "当前文件UUID不能为空")
    private String currentFolderUUID;
    @NotBlank(message = "新文件名不能为空")
    private String folderName;
}
