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
    @Value("${base-file-path.file-path}")
    private String filePath;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private FileInfoService fileInfoService;

    /**
     * @description 多媒体在线预览
     * @param md5
     * @param range
     * @return ResponseEntity<InputStreamResource>
     **/
    @SneakyThrows
    @GetMapping("/{md5}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> streamVideo(@PathVariable("md5") String md5,@RequestHeader(value = "Range",required = false) String range){
        //根据MD5查询文件
        FileInfo file = fileInfoService.getOne(new QueryWrapper<FileInfo>().eq("f_md5", md5));
        //拼接完整路经
        String resultFileName = filePath + File.separator  + md5 + "." + file.getFileExtension();
        //创建一个File对象
        File fileResource = new File(resultFileName);
        //文件是否存在
        if (!fileResource.exists()){
            //不存在返回404
            return ResponseEntity.notFound().build();
        }
        //将File文件放入一个文件输入流,再把文件输入流放入一个带缓冲输入流 提高文件读取效率
        InputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileResource));
        //获取文件长度
        long videoLength = fileResource.length();
        //判断range是否为空
        if (range!=null){
            // 获取请求头 Range 字段，即获取客户端请求的字节范围
            // 如果 Range 字段的值不为 null，说明客户端有请求字节范围
            // 否则客户端是请求整个视频文件
            // Range 字段大致格式：bytes=start-end （e.g. bytes=0-1023）
            // 需要解析 Range 字段获取开始字节和结束字节
            String[] ranges = range.split("=")[1].split("-");
            // startByte：开始字节，即 Range 字段中的 "start" 字节
            long startByte = Long.parseLong(ranges[0]);
            // endByte：结束字节，即 Range 字段中的 "end" 字节
            long endByte = videoLength - 1;
            //是为了判断 Range 字段是否包含范围的结束位置。如果 Range 字段中只有一个范围值，那么数组长度为 1，否则长度为 2。
            if (ranges.length == 2){
                //对于长度为 2 的范围，通过 Long.parseLong() 方法将 endByte 转换为long，接着可以用它来计算请求的字节范围内的数据长度。
                endByte = Long.parseLong(ranges[1]);
            }
            // 计算客户端请求的字节范围内的所有字节的长度
        long contentLengtg = endByte - startByte +1;
        //设置请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Range","bytes "+startByte + "-" + endByte + "/" + videoLength);
        httpHeaders.add("Accept-Ranges","bytes");
        httpHeaders.add("Content-Length", String.valueOf(contentLengtg));   //设置Content-Length头信息
        httpHeaders.add("Access-Control-Allow-Origin","*");
        httpHeaders.add("Content-Type",file.getFileType());
        //允许跨域
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
        //从startByte开始读取数据
        inputStream.skip(startByte);

        //读取分段数据
        // 返回读取的字节数
        int readLength = inputStream.read(bytes);
        // 初始化已读取的字节数为 readLength，即第一次读取的字节数
        int  totalReadLength = readLength;
        // 当已读取的字节数小于要读取的总字节数，并且上一次读取成功时
        while (totalReadLength < bytes.length && readLength >=0){
            // 从输入流中读取剩余的字节，并追加到 byte 数组中，返回读取的字节数
            readLength = inputStream.read(bytes,totalReadLength,bytes.length - totalReadLength);
            if (readLength > 0){
                // 更新已读取的字节数
                totalReadLength += readLength;
            }
        }
        //返回数据
        return new ByteArrayInputStream(bytes);
    }
}
