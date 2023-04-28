package com.yuban32.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuban32.entity.Folder;
import com.yuban32.response.Result;

import java.util.List;

/**
 * @author: Yuban32
 * @Date: 2023年03月22日
 */

public interface FolderService extends IService<Folder> {
    Result createFolder(String requestFolderUUID,String requestNewFolderName, String username);
    int updateFolderByFolderUUID(Folder folder);
    Folder selectFolderByFolderUUID(String folderUUID);
    List<Folder> selectFolderListByUserNameAndParentFolderUUID(String username,String parentFolderUUID);
}
