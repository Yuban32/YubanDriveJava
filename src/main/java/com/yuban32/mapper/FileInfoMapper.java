package com.yuban32.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: Yuban32
 * @Date: 2023年03月22日
 */

@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
    Integer insertFile(FileInfo fileInfo);

    List<FileInfo> selectFileByMd5(String md5);

    List<FileInfo> selectFileList();
    Boolean fileRenameByFileNameAnyFolderUUID(String newFileName ,String currentFileName,String folderUUID);

    Boolean removeFileToRecycle(String currentFolderUUID,String fileName,String userName);
    Boolean removeChildrenFileToRecycle(String parentFolderUUID,String userName);
    Boolean restoreFile(String fileUUID,String userName);
}
