package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.response.Result;
import com.yuban32.service.ChunkInfoService;
import com.yuban32.service.FileInfoService;
import com.yuban32.util.JWTUtils;
import com.yuban32.vo.FileAndFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    @Value("${base-file-path.user-upload-file-path}")
    private String userUploadFilePath;
    @Autowired
    private ChunkInfoService chunkInfoService;
    @Autowired
    FileInfoService fileInfoService;
    @Autowired
    private JWTUtils jwtUtils;


    @GetMapping("/fileList")
    @RequiresAuthentication
    public Result getFileListByUserNameAndParentFolderUUID(@RequestParam("parentFolderUUID")String parentFolderUUID,HttpServletRequest request) throws IOException {
        log.info("查询文件");
        String username = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        if(parentFolderUUID.equals("root")){
            parentFolderUUID=username;
        }
        List<FileInfo> fileList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_uploader",username).eq("f_parent_id",parentFolderUUID).eq("f_status",1));
        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();
        String domain = request.getServerName();
        int port = request.getServerPort();
        //拼接缩略图
        String imagePathURL = domain + ":" + port + "/images/";
        String thumbnailUrl = imagePathURL + "Thumbnail/Thumbnail";
        if (fileList.isEmpty()){
            return new Result(205,"没有查到文件",null);
        }else{
            for( FileInfo fileInfo : fileList){
                FileAndFolderVO temp = new FileAndFolderVO();
                temp.setType("file");
                temp.setName(fileInfo.getFileName());
                temp.setFileUUID(fileInfo.getFileMD5());
                temp.setParentFileUUID(fileInfo.getFileParentId());
                temp.setSize(fileInfo.getFileSize());
                temp.setUploader(fileInfo.getFileUploader());
                temp.setCreatedTime(fileInfo.getFileUploadTime());
                temp.setRelativePath(fileInfo.getFileRelativePath());
                temp.setFileExtension(fileInfo.getFileExtension());
                File checkFileType = new File(fileInfo.getFileAbsolutePath() + File.separator + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
                Tika tika = new Tika();
                String detect = tika.detect(checkFileType);
                String[] fileType = detect.split("/");
                temp.setCategory(detect);
                if(fileType[0].equals("image")){
                    temp.setThumbnailURL(thumbnailUrl+ fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
                    temp.setFullSizeImageURL(imagePathURL + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());

                }

                fileAndFolderVO.add(temp);
            }
            return new Result(200,"文件列表查询成功",fileAndFolderVO);
        }
    }
    @PostMapping("/file")
    @RequiresAuthentication
    public void download(@RequestParam("md5") String md5,
                         @RequestParam("fileName") String fileName,
                         @RequestParam("chunkSize") Integer chunkSize,
                         @RequestParam("chunkTotal") Integer chunkTotal,
                         @RequestParam("index")Integer index,
                         HttpServletRequest request,
                         HttpServletResponse response
    ){
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        String[] split = fileName.split("\\.");
        String type = split[split.length-1];
        //判断前端是否传来完整的文件路径 如果没传的话就默认在用户文件夹下 否则就按照前端传来的存储
        //此方案有BUG 不能按照用户名分类存储
//        String resultFileName = filePath + File.separator +userName + File.separator + userUploadFilePath + File.separator + md5 + "." + type;
        String resultFileName = filePath + File.separator  + md5 + "." + type;
        File resultFile = new File(resultFileName);
        if (!resultFile.exists()){
            new Throwable("");
        }
        long offset = chunkSize * (index -1);
        if(Objects.equals(index,chunkTotal)){
            offset = resultFile.length() - chunkSize;
        }
        byte[] chunk = chunkInfoService.getChunk(index,chunkSize,resultFileName,offset);
        log.info("开始下载文件:{},{}.{}.{}.{}",resultFileName,index,chunkSize,chunk.length,offset);
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.addHeader("Access-Control-Expose-Headers","Content-Disposition");
        response.addHeader("Content-Length", "" + (chunk.length));
        response.setHeader("filename", fileName);
        response.setContentType("application/octet-stream");
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            out.write(chunk);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
