package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.mapper.FolderMapper;
import com.yuban32.response.Result;
import com.yuban32.service.FileInfoService;
import com.yuban32.service.FolderService;
import com.yuban32.service.SendFileAndFolderDataService;
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
    @Autowired
    private SendFileAndFolderDataService sendFileAndFolderDataService;

    /***
     * @description 查询文件列表 包含了文件和文件夹
     * @param parentFolderUUID
     * @param request
     * @return Result
     **/
    @GetMapping
    @RequiresAuthentication
    public Result getFileAndFolderList(@RequestParam("parentFolderUUID") String parentFolderUUID, HttpServletRequest request) throws IOException {
        log.info("查询文件列表");
        //通过请求头获取JWT后解密获取用户名
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        if (parentFolderUUID.equals("root")) {
            parentFolderUUID = userName;
        }
        //查询
        List<FileInfo> fileList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_uploader", userName).eq("f_parent_id", parentFolderUUID).eq("f_status", 1));
        List<Folder> foldersList = folderService.list(new QueryWrapper<Folder>().eq("username", userName).eq("parent_folder_uuid", parentFolderUUID).eq("folder_status", 1));
        //拼接缩略图的地址
        String imagePathURL = request.getServerName() + ":" + request.getServerPort() + "/images/";
        String thumbnailUrl = imagePathURL + "Thumbnail/Thumbnail";
        //两个都为空时,返回
        if (foldersList.isEmpty() && fileList.isEmpty()) {
            return new Result(205, "没有查到文件", null);
        } else {
            //调用封装好的方法,来将File和Folder的都封装到一个List对象中,再返回
            List<FileAndFolderVO> fileAndFolderVO = sendFileAndFolderDataService.sendFileInfoListAndFolderListData(foldersList, fileList, imagePathURL, thumbnailUrl, request);
            //最后将数据集合返回给前端
            return new Result(200, "文件列表查询成功", fileAndFolderVO);
        }
    }
}
