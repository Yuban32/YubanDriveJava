package com.yuban32.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuban32.entity.FileInfo;
import com.yuban32.mapper.FileInfoMapper;
import com.yuban32.service.FileInfoService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName FileInfoServiceImpl
 * @Description
 * @Date 2023年03月22日
 */
@Service
@Slf4j
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {
    @Value("${base-file-path.file-path}")
    private String filePath;
    @Value("${base-file-path.user-upload-file-path}")
    private String userUploadFilePath;
    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    public Integer addFile(FileInfo fileInfo) {
        return fileInfoMapper.insertFile(fileInfo);
    }

    @Override
    public Boolean selectFileByMd5(String md5) {
        List<FileInfo> fileInfo = fileInfoMapper.selectFileByMd5(md5);
        return !fileInfo.isEmpty();
    }

    @Override
    public List<FileInfo> selectFileList() {
        List<FileInfo> list = fileInfoMapper.selectFileList();
        return list;
    }


    @Override
    public Boolean fileRenameByFileNameAnyFolderUUID(String newFileName, String currentFileName, String folderUUID) {
        return fileInfoMapper.fileRenameByFileNameAnyFolderUUID(newFileName, currentFileName, folderUUID);
    }

    @SneakyThrows
    @Override
    public void createThumbnail(String resultFileName, String username, String md5) {
        //获取文件类型
        String[] split = resultFileName.split("\\.");
        String type = split[split.length - 1];
        Path thumbnailFolder = Paths.get(filePath + File.separator + username + File.separator + userUploadFilePath + File.separator + "Thumbnail");

        File file = new File(resultFileName);
        Tika tika = new Tika();
        String detect = tika.detect(file);
        String[] split1 = detect.split("/");
        if (!Files.isWritable(thumbnailFolder)) {
            log.info("缩略图路径不存在,新建路径:{}", thumbnailFolder);
            Files.createDirectories(thumbnailFolder);
        }
        if (split1[0].equals("image")) {
            BufferedImage img = ImageIO.read(file);
            if (img.getWidth() > 250 && img.getHeight() > 250) {
                Thumbnails.of(resultFileName)
                        .size(250, 250)
                        .toFile(thumbnailFolder + File.separator + "Thumbnail" + md5 + "." + type);
            }else{
                Path sourceFile = Paths.get(resultFileName);
                Path targetFolder = Paths.get(thumbnailFolder + File.separator + "Thumbnail" + md5 + "." + type);
                Files.copy(sourceFile,targetFolder, StandardCopyOption.REPLACE_EXISTING);
            }

        }

    }

}
