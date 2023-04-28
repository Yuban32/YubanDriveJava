package com.yuban32.util;


import com.yuban32.entity.Folder;
import com.yuban32.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @author Yuban32
 * @ClassName FilePathUtils
 * @Description
 * @Date 2023年03月22日
 */
public class FilePathUtils {
    @Autowired
    private FolderService folderService;
    public String getFilePath(String folderUUID,String filePath,String userName){
        String directory;
        //判断前端传来的folderUUID是否为root 如果是root的话就默认存在用户根目录下
        if(folderUUID.equals("root")){
            //存储文件的文件夹
            directory = filePath + File.separator + userName;

        }else{
            Folder folder = folderService.selectFolderByFolderUUID(folderUUID);
            directory = folder.getFolderRelativePath();
        }
        return directory;
    }
}
