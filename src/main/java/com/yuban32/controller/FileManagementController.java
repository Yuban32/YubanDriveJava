package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yuban32.dto.FileRenameDTO;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.response.Result;
import com.yuban32.service.FileInfoService;
import com.yuban32.service.FolderService;
import com.yuban32.util.JWTUtils;
import com.yuban32.util.LocalDateTimeFormatterUtils;
import com.yuban32.vo.FileAndFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName FileManagementController
 * @Description 文件管理
 * @Date 2023年03月24日
 */
@Slf4j
@RestController
@RequestMapping("/fileManagement")
public class FileManagementController {

    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private JWTUtils jwtUtils;

    @GetMapping("/list")
    public Result getAllUserFile(HttpServletRequest request) throws IOException {
        String username = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        List<FileInfo> fileList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_status",1));
        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();
        String domain = request.getServerName();
        int port = request.getServerPort();
        //拼接缩略图
        String imagePathURL = domain + ":" + port + "/download/" + username + "/userUpload/";
        String thumbnailUrl = imagePathURL + "Thumbnail/Thumbnail";

        log.info("{}",domain);
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

                File checkFileType = new File(fileInfo.getFileAbsolutePath() + File.separator + fileInfo.getFileMD5() + "." + fileInfo.getFileType());
                Tika tika = new Tika();
                String detect = tika.detect(checkFileType);
                String[] fileType = detect.split("/");
                temp.setCategory(detect);
                if(fileType[0].equals("image")){
                    temp.setThumbnailURL(thumbnailUrl+ fileInfo.getFileMD5() + "." + fileInfo.getFileType());
                    temp.setFullSizeImageURL(imagePathURL + fileInfo.getFileMD5() + "." + fileInfo.getFileType());

                }

                fileAndFolderVO.add(temp);
            }
            return new Result(200,"文件列表查询成功",fileAndFolderVO);
        }
    }

    @PostMapping("/rename")
//    @RequiresAuthentication
    public Result fileRename(@Validated @RequestBody FileRenameDTO fileRenameDTO , HttpServletRequest request){
        String username = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        fileRenameDTO.setUserName(username);
        Boolean isSuccess = fileInfoService.fileRenameByFileNameAnyFolderUUID(fileRenameDTO);
        if(isSuccess){
            FileInfo one = fileInfoService.getOne(new QueryWrapper<FileInfo>().eq("f_name", fileRenameDTO.getTargetFileName()).eq("f_parent_id", fileRenameDTO.getFolderUUID()).eq("f_uploader",username));
            FileAndFolderVO temp = new FileAndFolderVO();
            temp.setName(one.getFileName());
            temp.setFileUUID(one.getFileMD5());
            return new Result(200,"文件重命名成功",temp);
        }else {
            return new Result(205,"文件重命名失败",null);
        }
    }

    @PostMapping("/copy")
//    @RequiresAuthentication
    public Result fileCopy(@RequestParam("fileName")String fileName,
                           @RequestParam("currentFolderUUID")String currentFolderUUID,
                           @RequestParam("newFolderUUID")String newFolderUUID){
        LocalDateTimeFormatterUtils localDateTimeFormatterUtils = new LocalDateTimeFormatterUtils();
        //通过fileName和currentFolderUUID来查找当前文件
        FileInfo currentFile = fileInfoService.getOne(new QueryWrapper<FileInfo>().eq("f_name", fileName).eq("f_parent_id", currentFolderUUID));
        if (currentFile == null){
            return Result.error(400,"该文件不存在");
        }
        //通过newFolderUUID来查找目标文件夹
        Folder folder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_uuid", newFolderUUID));
        if (folder == null){
            return Result.error(400,"目标文件夹不存在");
        }
        currentFile.setFileParentId(folder.getFolderUUID());
        currentFile.setFileRelativePath(folder.getFolderRelativePath());
        currentFile.setFileUploadTime(localDateTimeFormatterUtils.getStartDateTime());
        fileInfoService.addFile(currentFile);
        return new Result(200,"复制文件成功",currentFile);
    }

    @PostMapping("/cut")
//    @RequiresAuthentication
    public Result cutFile(@RequestParam("fileName")String fileName,
                          @RequestParam("currentFolderUUID")String currentFolderUUID,
                          @RequestParam("newFolderUUID")String newFolderUUID){
        LocalDateTimeFormatterUtils localDateTimeFormatterUtils = new LocalDateTimeFormatterUtils();
        //通过fileName和currentFolderUUID来查找当前文件
        FileInfo currentFile = fileInfoService.getOne(new QueryWrapper<FileInfo>().eq("f_name", fileName).eq("f_parent_id", currentFolderUUID));
        if (currentFile == null){
            return Result.error(400,"该文件不存在");
        }
        //通过newFolderUUID来查找目标文件夹
        Folder folder = folderService.getOne(new QueryWrapper<Folder>().eq("folder_uuid", newFolderUUID));
        if (folder == null){
            return Result.error(400,"目标文件夹不存在");
        }
        currentFile.setFileParentId(folder.getFolderUUID());
        currentFile.setFileRelativePath(folder.getFolderRelativePath());
        currentFile.setFileUploadTime(localDateTimeFormatterUtils.getStartDateTime());
        fileInfoService.addFile(currentFile);
        boolean remove = fileInfoService.remove(new QueryWrapper<FileInfo>().eq("f_name", fileName).eq("f_parent_id", currentFile));
        if (remove){
            return new Result(200,"文件剪贴成功",currentFile);
        }else {
            return new Result(400,"文件剪贴失败",null);
        }

    }

    @DeleteMapping("/delete")
    public Result deleteFile(@RequestParam("fileName")String fileName,
                             @RequestParam("currentFolderUUID")String currentFolderUUID){

        boolean remove = fileInfoService.remove(new QueryWrapper<FileInfo>().eq("f_name", fileName).eq("f_parent_id", currentFolderUUID));
        if(remove){
            return new Result(200,"文件删除成功",null);
        }else {
            return new Result(400,"文件删除失败",null);
        }
    }
}
