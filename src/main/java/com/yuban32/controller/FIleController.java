package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.mapper.FolderMapper;
import com.yuban32.response.Result;
import com.yuban32.service.FileInfoService;
import com.yuban32.service.FolderService;
import com.yuban32.util.JWTUtils;
import com.yuban32.vo.FileAndFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName FIleController
 * @Description 文件控制类 用于查询文件列表
 * @Date 2023年05月18日
 */
@Slf4j
@RestController
@RequestMapping("/fileList")
public class FIleController {
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private FolderService folderService;
    @Autowired
    FileInfoService fileInfoService;

    /***
     * @description 查询文件列表
     * @param parentFolderUUID
     * @param request
     * @return Result
     **/
    @GetMapping
    @RequiresAuthentication
    public Result getFileAndFolderList(@RequestParam("parentFolderUUID") String parentFolderUUID, HttpServletRequest request) throws IOException {
        log.info("查询文件列表");
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        if (parentFolderUUID.equals("root")) {
            parentFolderUUID = userName;
        }
        List<FileInfo> fileList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_uploader", userName).eq("f_parent_id", parentFolderUUID).eq("f_status", 1));
        List<Folder> foldersList = folderService.list(new QueryWrapper<Folder>().eq("username", userName).eq("parent_folder_uuid", parentFolderUUID).eq("folder_status", 1));
        //拼接缩略图
        String imagePathURL = request.getServerName() + ":" + request.getServerPort() + "/images/";
        String thumbnailUrl = imagePathURL + "Thumbnail/Thumbnail";

        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();

        if (foldersList.isEmpty() && fileList.isEmpty()) {
            return new Result(205, "没有查到文件", null);
        } else {
            for (Folder folders : foldersList) {
                FileAndFolderVO temp = new FileAndFolderVO();
                temp.setType("folder");
                temp.setCategory(null);
                temp.setName(folders.getFolderName());
                temp.setFileUUID(folders.getFolderUUID());
                temp.setParentFileUUID(folders.getParentFolderUUID());
                temp.setSize(null);
                temp.setUploader(folders.getUsername());
                temp.setCreatedTime(folders.getFolderCreateTime());
                temp.setRelativePath(folders.getFolderRelativePath());
                fileAndFolderVO.add(temp);
            }
            for (FileInfo fileInfo : fileList) {
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
                if (fileType[0].equals("image")) {
                    temp.setThumbnailURL(thumbnailUrl + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
                    temp.setFullSizeImageURL(imagePathURL + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
                }
                fileAndFolderVO.add(temp);
            }
            return new Result(200, "文件列表查询成功", fileAndFolderVO);
        }
    }
}
