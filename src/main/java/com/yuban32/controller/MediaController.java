package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.service.FileInfoService;
import com.yuban32.util.JWTUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public ResponseEntity<Resource> streamVideo(@PathVariable("md5") String md5 , HttpServletRequest request){
        log.info("进入在线播放API");
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        FileInfo file = fileInfoService.getOne(new QueryWrapper<FileInfo>().eq("f_md5", md5));
        String resultFileName = filePath + File.separator +userName + File.separator + userUploadFilePath + File.separator + md5 + "." + file.getFileType();
        Path path = Paths.get(resultFileName);
        Resource resource = new InputStreamResource(Files.newInputStream(path));

        //设置 mp4d请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM));

        httpHeaders.add("Access-Control-Allow-Origin","*");
        httpHeaders.add("Access-Control-Allow-Credentials", "true");
        httpHeaders.set(HttpHeaders.TRANSFER_ENCODING,"chunked");
        return new ResponseEntity<>(resource,httpHeaders,HttpStatus.OK);

    }
}
