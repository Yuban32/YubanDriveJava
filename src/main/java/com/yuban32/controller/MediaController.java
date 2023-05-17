package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.service.FileInfoService;
import com.yuban32.util.JWTUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.*;


/**
 * @author Yuban32
 * @ClassName MediaController
 * @Description 在线播放多媒体控制类
 * @Date 2023年05月17日
 */

@Slf4j
@RestController
@RequestMapping("/media")
public class MediaController {
    @Value("${base-file-path.user-upload-file-path}")
    private String userUploadFilePath;
    @Value("${base-file-path.file-path}")
    private String filePath;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private FileInfoService fileInfoService;

    @SneakyThrows
    @GetMapping("/{md5}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> streamVideo(@PathVariable("md5") String md5,@RequestHeader(value = "Range",required = false) String range){
        FileInfo file = fileInfoService.getOne(new QueryWrapper<FileInfo>().eq("f_md5", md5));
        String resultFileName = filePath + File.separator  + md5 + "." + file.getFileExtension();
        File fileResource = new File(resultFileName);
        if (!fileResource.exists()){
            return ResponseEntity.notFound().build();
        }
        InputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileResource));
        long videoLength = fileResource.length();
        if (range!=null){
            String[] ranges = range.split("=")[1].split("-");
            long startByte = Long.parseLong(ranges[0]);
            long endByte = videoLength - 1;
            if (ranges.length == 2){
                endByte = Long.parseLong(ranges[1]);
            }
        long contentLengtg = endByte - startByte +1;
        //设置请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Range","bytes "+startByte + "-" + endByte + "/" + videoLength);
        httpHeaders.add("Accept-Ranges","bytes");
        httpHeaders.add("Content-Length", String.valueOf(contentLengtg));   //设置Content-Length头信息
        httpHeaders.add("Access-Control-Allow-Origin","*");
        httpHeaders.add("Content-Type",file.getFileType());
        httpHeaders.add("Access-Control-Allow-Credentials", "true");
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(httpHeaders).body(new InputStreamResource(getInputStream(bufferedInputStream,startByte,endByte)));
        }
        //如果range头无效 则返回整个视频文件
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type",file.getFileType());
        httpHeaders.add("Content-Length", String.valueOf(videoLength));
        httpHeaders.add("Accept-Ranges","bytes");
        return ResponseEntity.ok().headers(httpHeaders).body(new InputStreamResource(bufferedInputStream));
    }
    @SneakyThrows
    public InputStream getInputStream(InputStream inputStream, long startByte , long endByte){
        byte[] bytes = new byte[(int)(endByte - startByte +1)];
        inputStream.skip(startByte);

        //读取分段数据
        int readLength = inputStream.read(bytes);
        int  totalReadLength = readLength;
        while (totalReadLength < bytes.length && readLength >=0){
            readLength = inputStream.read(bytes,totalReadLength,bytes.length - totalReadLength);
            if (readLength > 0){
                totalReadLength += readLength;
            }
        }
        return new ByteArrayInputStream(bytes);
    }
}
