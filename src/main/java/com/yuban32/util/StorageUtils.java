package com.yuban32.util;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;

/**
 * @author Yuban32
 * @ClassName StorageUtils
 * @Description 存储工具类
 * @Date 2023年03月10日
 */
public class StorageUtils {
    @Value("${base-file-path.file-path}")
    private String filePath;

    public double getDriveFreeSpace(){
        StringBuilder stringBuilder = new StringBuilder();
        File drive = new File(stringBuilder.toString());
        long freeSpace = drive.getFreeSpace();
        return freeSpace;
    }
}
