package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.response.Result;
import com.yuban32.service.FileInfoService;
import com.yuban32.service.FolderService;
import com.yuban32.service.SendFileAndFolderDataService;
import com.yuban32.vo.FileAndFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName AdminManagement
 * @Description 存储管理
 * @RequireRole Admin
 * @Date 2023年05月19日
 */
@Slf4j
@RestController
@RequestMapping("/admin-management")
public class AdminStorageManagementController {
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private SendFileAndFolderDataService sendFileAndFolderDataService;
    //删除文件的API在RecycleController里的trash
    @GetMapping("/storage-list")
    @RequiresAuthentication
    @RequiresRoles("admin")
    public Result getAllUserFile(HttpServletRequest request) throws IOException {
        List<Folder> foldersList = folderService.list();
        List<FileInfo> fileList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_status",1));
        String domain = request.getServerName();
        int port = request.getServerPort();
        //拼接缩略图
        String imagePathURL = domain + ":" + port + "/images/";
        String thumbnailUrl = imagePathURL + "Thumbnail/Thumbnail";
        if (fileList.isEmpty() && foldersList.isEmpty()){
            return new Result(205,"没有查到文件",null);
        }else{
            //调用封装好的方法,来将File和Folder的都封装到一个List对象中,再返回
            List<FileAndFolderVO> fileAndFolderVO = sendFileAndFolderDataService.sendFileInfoListAndFolderListData(foldersList, fileList, imagePathURL, thumbnailUrl, request);

            return new Result(200,"文件列表查询成功",fileAndFolderVO);
        }
    }
}
