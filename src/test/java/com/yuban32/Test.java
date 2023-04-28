package com.yuban32;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.Folder;
import com.yuban32.response.Result;
import com.yuban32.util.LocalDateTimeFormatterUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Yuban32
 * @ClassName Test
 * @Description
 * @Date 2023年03月02日
 */
@Slf4j

public class Test {

    public static void main(String []args) {
        List<String> list = new ArrayList<>();
        list.add("\\213\\213\\222");
        list.add("\\444\\441\\561");
        System.out.println(list.get(0));
    }
//    public Result createFolder(String requestFolderUUID, String requestNewFolderName, String username) {
//        log.info("用户 {}, 开始创建文件夹 {}, {}", username, requestFolderUUID, requestNewFolderName);
//        String newFolderUUID = UUID.randomUUID().toString();
//        LocalDateTimeFormatterUtils localDateTimeFormatterUtils = new LocalDateTimeFormatterUtils();
//        QueryWrapper<Folder> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("folder_uuid", requestFolderUUID);
//        Folder parentFolder = folderMapper.selectOne(queryWrapper);
//        if (parentFolder == null) {
//            return Result.error(500, "参数传递错误");
//        }
//        List<Folder> folders = folderMapper.selectList(new QueryWrapper<Folder>().eq("parent_folder_uuid", requestFolderUUID));
//        if (folders != null && folders.size() > 0) {
//            Folder folder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("folder_name", requestNewFolderName).eq("parent_folder_uuid", requestFolderUUID));
//            if (folder != null) {
//                return Result.error(500, "当前目录下已有同名文件夹");
//            }
//        }
//        String folderPath = parentFolder.getFolderRelativePath() + File.separator + requestNewFolderName;
//        Folder folder = new Folder(username, folderPath, newFolderUUID, requestNewFolderName, parentFolder.getFolderUUID(), localDateTimeFormatterUtils.getStartDateTime());
//        folderMapper.createFolder(folder);
//        return new Result(200, "文件夹创建成功", folder);
//    }
}
