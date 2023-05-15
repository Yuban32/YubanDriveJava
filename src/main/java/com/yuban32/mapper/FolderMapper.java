package com.yuban32.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuban32.entity.Folder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: Yuban32
 * @Date: 2023年03月22日
 */
@Mapper
public interface FolderMapper extends BaseMapper<Folder> {
    int createFolder(Folder folder);
    int updateFolderByFolderUUID(Folder folder);
    Folder selectFolderByFolderUUID(String folderUUID);
    Folder selectOneByFolderUUIDFolder(String folderUUID);
    Folder selectFolderByParentFolderUUIDAndNewFolderName(String parentFolderUUID,String newFolderName);
    List<Folder> selectFolderListByFolderNameAndParentFolderUUID(String folderName,String parentFolderUUID);
    List<Folder> selectFolderAllByUsernameAndParentFolderUUID(String username , String parentFolderUUID);
    List<Folder> selectFolderListByParentFolderUUID(String parentFolderUUID);
    Boolean removeFolderToRecycle(String currentFolderUUID , String userName);
    Boolean removeChildrenFolderToRecycle(String parentFolderUUID , String userName);
    Boolean restoreFolder(String folderUUID , String userName);
    Boolean folderRename(String currentFolderUUID , String folderName , String userName);
}
