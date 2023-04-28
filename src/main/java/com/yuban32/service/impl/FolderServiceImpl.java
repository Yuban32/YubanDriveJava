package com.yuban32.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuban32.entity.Folder;
import com.yuban32.mapper.FolderMapper;
import com.yuban32.response.Result;
import com.yuban32.service.FolderService;
import com.yuban32.util.LocalDateTimeFormatterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Yuban32
 * @ClassName FolderServiceImpl
 * @Description
 * @Date 2023年03月22日
 */
@Slf4j
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper,Folder> implements FolderService {
    @Autowired
    private FolderMapper folderMapper;
    @Override
    public Result createFolder(String requestFolderUUID,String requestNewFolderName, String username) {
        String folderPath;
        String root = "root";
        //0 涉及到文件夹的IO操作都是在数据库上操作 不在磁盘上做实际的IO操作
        //1 如果传来的tFolderUUID是不是root root代表user的根目录 也就是以用户名命名的文件夹
        //2 如果传来的FolderUUID不是root 就代表着是有非root的上级文件夹
        //3 根据FolderUUID来查询上级文件夹的具体路径后再拼接folderName
        //4 拼接完后创建文件夹
        //大致流程是这样的
        log.info("用户 {},开始创建文件夹 {},{}", username, requestFolderUUID, requestNewFolderName);
        String newFolderUUID = String.valueOf(UUID.randomUUID());
        LocalDateTimeFormatterUtils localDateTimeFormatterUtils = new LocalDateTimeFormatterUtils();

        if (requestFolderUUID.equals(root)) {
            List<Folder> isEmpty = folderMapper.selectList(new QueryWrapper<Folder>().eq("parent_folder_uuid", requestFolderUUID).eq("username",username));
            if (isEmpty == null) {
                folderPath = root + File.separator + newFolderUUID;
                Folder folder = new Folder(username, folderPath, newFolderUUID, requestNewFolderName, requestFolderUUID,localDateTimeFormatterUtils.getStartDateTime());
                folderMapper.createFolder(folder);
                return new Result(200,"文件夹创建成功",folder);
            } else {
                Folder isSameName = folderMapper.selectOne(new QueryWrapper<Folder>().eq("folder_name", requestNewFolderName).eq("parent_folder_uuid",requestFolderUUID).eq("username",username));
                if (isSameName == null){
                    folderPath = root + File.separator + newFolderUUID;
                    Folder folder = new Folder(username, folderPath, newFolderUUID, requestNewFolderName, requestFolderUUID,localDateTimeFormatterUtils.getStartDateTime());
                    folderMapper.createFolder(folder);
                    return new Result(200,"文件创建成功",folder);
                }
                return Result.error(500, "当前目录下已有同名文件夹");
            }
        } else {
            Folder parentFolderIsExists = folderMapper.selectOne(new QueryWrapper<Folder>().eq("folder_uuid",requestFolderUUID).eq("username",username));
            if (parentFolderIsExists != null) {
                List<Folder> isEmpty = folderMapper.selectList(new QueryWrapper<Folder>().eq("parent_folder_uuid", requestFolderUUID).eq("username",username));
                if (isEmpty == null) {
                    folderPath = parentFolderIsExists.getFolderRelativePath() + File.separator + newFolderUUID;
                    Folder folder = new Folder(username, folderPath, newFolderUUID, requestNewFolderName, parentFolderIsExists.getFolderUUID(),localDateTimeFormatterUtils.getStartDateTime());
                    folderMapper.createFolder(folder);
                    return new Result(200,"文件夹创建成功",folder);
                } else {
                    Folder isSameName = folderMapper.selectOne(new QueryWrapper<Folder>().eq("folder_name", requestNewFolderName).eq("parent_folder_uuid",requestFolderUUID).eq("username",username));
                    if (isSameName == null){
                        folderPath = parentFolderIsExists.getFolderRelativePath() + File.separator + newFolderUUID;
                        Folder folder = new Folder(username, folderPath, newFolderUUID, requestNewFolderName, requestFolderUUID,localDateTimeFormatterUtils.getStartDateTime());
                        folderMapper.createFolder(folder);
                        return new Result(200,"文件创建成功",folder);
                    }
                    return Result.error(500, "当前目录下已有同名文件夹1");
                }
            } else {
                return Result.error(500, "参数传递错误");
            }
        }
    }

    @Override
    public int updateFolderByFolderUUID(Folder folder) {
        return folderMapper.updateFolderByFolderUUID(folder);
    }

    @Override
    public Folder selectFolderByFolderUUID(String folderUUID) {
        return folderMapper.selectFolderByFolderUUID(folderUUID);
    }

    @Override
    public List<Folder> selectFolderListByUserNameAndParentFolderUUID(String username,String parentFolderUUID) {
        return folderMapper.selectFolderAllByUsernameAndParentFolderUUID(username,parentFolderUUID);
    }
}
