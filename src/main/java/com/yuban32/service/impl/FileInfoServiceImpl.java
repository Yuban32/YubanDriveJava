package com.yuban32.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuban32.dto.FileRenameDTO;
import com.yuban32.entity.FileInfo;
import com.yuban32.mapper.FileInfoMapper;
import com.yuban32.service.FileInfoService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
    public Boolean fileRenameByFileNameAnyFolderUUID(FileRenameDTO fileRenameDTO) {
        return fileInfoMapper.fileRenameByFileNameAnyFolderUUID(fileRenameDTO);
    }

    @SneakyThrows
    @Override
    public void createThumbnail(String resultFileName, String username, String md5) {
        //获取文件类型
        String[] split = resultFileName.split("\\.");
        //获取文件后缀名
        String fileExtension = split[split.length - 1];
        //拼接文件路径
        Path thumbnailFolder = Paths.get(filePath + File.separator + "Thumbnail");

        File file = new File(resultFileName);
        Tika tika = new Tika();
        String detect = tika.detect(file);
        String[] split1 = detect.split("/");
        if (!Files.isWritable(thumbnailFolder)) {
            log.info("缩略图路径不存在,新建路径:{}", thumbnailFolder);
            Files.createDirectories(thumbnailFolder);
        }

        if (split1[0].equals("image")) {
            //调用ImageIO库
            BufferedImage img = ImageIO.read(file);
            //如果图片尺寸大于250*250的话
            if (img.getWidth() > 250 && img.getHeight() > 250) {
                //将图片裁剪到250*250 并输出到指定文件夹内
                Thumbnails.of(resultFileName)
                        .size(250, 250)
                        .toFile(thumbnailFolder + File.separator + "Thumbnail" + md5 + "." + fileExtension);
            }else{
                //如果图片尺寸小于250*250的话 直接复制一份到指定目录下 这么做的目的是配合前端显示图片缩略图
                Path sourceFile = Paths.get(resultFileName);
                Path targetFolder = Paths.get(thumbnailFolder + File.separator + "Thumbnail" + md5 + "." + fileExtension);
                Files.copy(sourceFile,targetFolder, StandardCopyOption.REPLACE_EXISTING);
            }

        }

    }

}
