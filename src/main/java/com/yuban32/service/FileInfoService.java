package com.yuban32.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuban32.entity.FileInfo;

import java.util.List;

/**
 * @author: Yuban32
 * @Date: 2023年03月22日
 */

public interface FileInfoService extends IService<FileInfo> {
    Integer addFile(FileInfo fileInfo);
    Boolean selectFileByMd5(String md5);
    List<FileInfo> selectFileList();
    Boolean fileRenameByFileNameAnyFolderUUID(String newFileName ,String currentFileName,String folderUUID);

    void createThumbnail(String resultFileName,String username ,String md5);

}
