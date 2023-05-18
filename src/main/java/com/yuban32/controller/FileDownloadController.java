package com.yuban32.controller;


import com.yuban32.service.ChunkInfoService;
import com.yuban32.service.FileInfoService;
import com.yuban32.util.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Yuban32
 * @ClassName FileDownloadController
 * @Description 文件下载控制层
 * @Date 2023年03月11日
 */
@Slf4j
@RestController
@RequestMapping("/fileDownload")
public class FileDownloadController {

    @Value("${base-file-path.file-path}")
    private String filePath;
    @Autowired
    private ChunkInfoService chunkInfoService;
    @Autowired
    FileInfoService fileInfoService;
    @Autowired
    private JWTUtils jwtUtils;


//此接口暂时废弃

//    @GetMapping("/fileList")
//    @RequiresAuthentication
//    public Result getFileListByUserNameAndParentFolderUUID(@RequestParam("parentFolderUUID")String parentFolderUUID,HttpServletRequest request) throws IOException {
//        log.info("查询文件");
//        String username = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
//        if(parentFolderUUID.equals("root")){
//            parentFolderUUID=username;
//        }
//        List<FileInfo> fileList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_uploader",username).eq("f_parent_id",parentFolderUUID).eq("f_status",1));
//        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();
//        String domain = request.getServerName();
//        int port = request.getServerPort();
//        //拼接缩略图
//        String imagePathURL = domain + ":" + port + "/images/";
//        String thumbnailUrl = imagePathURL + "Thumbnail/Thumbnail";
//        if (fileList.isEmpty()){
//            return new Result(205,"没有查到文件",null);
//        }else{
//            for( FileInfo fileInfo : fileList){
//                FileAndFolderVO temp = new FileAndFolderVO();
//                temp.setType("file");
//                temp.setName(fileInfo.getFileName());
//                temp.setFileUUID(fileInfo.getFileMD5());
//                temp.setParentFileUUID(fileInfo.getFileParentId());
//                temp.setSize(fileInfo.getFileSize());
//                temp.setUploader(fileInfo.getFileUploader());
//                temp.setCreatedTime(fileInfo.getFileUploadTime());
//                temp.setRelativePath(fileInfo.getFileRelativePath());
//                temp.setFileExtension(fileInfo.getFileExtension());
//                File checkFileType = new File(fileInfo.getFileAbsolutePath() + File.separator + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
//                Tika tika = new Tika();
//                String detect = tika.detect(checkFileType);
//                String[] fileType = detect.split("/");
//                temp.setCategory(detect);
//                if(fileType[0].equals("image")){
//                    temp.setThumbnailURL(thumbnailUrl+ fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
//                    temp.setFullSizeImageURL(imagePathURL + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
//
//                }
//
//                fileAndFolderVO.add(temp);
//            }
//            return new Result(200,"文件列表查询成功",fileAndFolderVO);
//        }
//    }
/**
 * @description 文件下载
 * @param md5
 * @param fileName
 * @param chunkSize
 * @param chunkTotal
 * @param index
 * @param response
 * @return void
 **/
    @PostMapping("/file")
    @RequiresAuthentication
    public void download(@RequestParam("md5") String md5,
                         @RequestParam("fileName") String fileName,
                         @RequestParam("chunkSize") Integer chunkSize,
                         @RequestParam("chunkTotal") Integer chunkTotal,
                         @RequestParam("index")Integer index,
                         HttpServletResponse response
    ) throws FileNotFoundException {
        //将前端传来的文件名进行分割,取出文件的拓展名
        String[] split = fileName.split("\\.");
        String fileExtension = split[split.length-1];

        String resultFileName = filePath + File.separator  + md5 + "." + fileExtension;
        //拼接后的文件完整存储路径传给File对象
        File resultFile = new File(resultFileName);
        if (!resultFile.exists()){
            throw new FileNotFoundException("文件不存在,无法下载");
        }
        //计算当前文件偏移量
        long offset = chunkSize * (index -1);
        //如果当前分片是最后一块的话
        if(Objects.equals(index,chunkTotal)){
            //计算一个分片的起始偏移量
            offset = resultFile.length() - chunkSize;
        }
        //进入到Service 获取分片
        byte[] chunk = chunkInfoService.getChunk(index,chunkSize,resultFileName,offset);
//        log.info("开始下载文件:{},{}.{}.{}.{}",resultFileName,index,chunkSize,chunk.length,offset);
        //添加响应头
        //告知浏览器这个请求是下载,不能打开
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        //暴露上面的这个响应头,以便于前端获取文件名
        response.addHeader("Access-Control-Expose-Headers","Content-Disposition");
        //设置分片数据的字节数
        response.addHeader("Content-Length", "" + (chunk.length));
        //指示浏览器下载该文件
        response.setHeader("filename", fileName);
        //设置响应体的MIME类型为 二进制流
        response.setContentType("application/octet-stream");
        //实体化响应输出流
        ServletOutputStream out = null;
        try {
            //获取响应流输出对象
            out = response.getOutputStream();
            //将当前的分片数据写入到响应输出流中
            out.write(chunk);
            //刷新缓冲区
            out.flush();
            //关闭流
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
