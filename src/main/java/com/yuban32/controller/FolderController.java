package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.dto.FolderRenameDTO;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.mapper.FolderMapper;
import com.yuban32.response.Result;
import com.yuban32.service.FolderService;
import com.yuban32.util.JWTUtils;
import com.yuban32.vo.FileAndFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName FolderController
 * @Description
 * @Date 2023年03月22日
 */
@Slf4j
@RestController
@RequestMapping("/folder")
public class FolderController {
    @Autowired
    private FolderService folderService;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private FolderMapper folderMapper;
    private static final String ROOT = "root";
    @PostMapping("/create")
    @RequiresAuthentication
    public Result createFolder(
            @RequestParam("folderUUID") String requestFolderUUID,
            @RequestParam("newFolderName") String requestNewFolderName,
            HttpServletRequest request){
        String username = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        return folderService.createFolder(requestFolderUUID,requestNewFolderName, username);
    }

    @PostMapping("/rename")
    @RequiresAuthentication
    public Result folderRename(@Validated @RequestBody FolderRenameDTO renameDTO , HttpServletRequest request){
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        Boolean folderRename = folderMapper.folderRename(renameDTO.getCurrentFolderUUID(), renameDTO.getFolderName(), userName);
        log.info("{},{}",renameDTO,folderRename);
        //成功的话 封装数据返回前端
        if (folderRename){
            Folder folder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("folder_uuid", renameDTO.getCurrentFolderUUID()).eq("username", userName));
            FileAndFolderVO temp = new FileAndFolderVO();
            temp.setType("folder");
            temp.setCategory(null);
            temp.setName(folder.getFolderName());
            temp.setFileUUID(folder.getFolderUUID());
            temp.setParentFileUUID(folder.getParentFolderUUID());
            temp.setSize(null);
            temp.setUploader(folder.getUsername());
            temp.setCreatedTime(folder.getFolderCreateTime());
            temp.setRelativePath(folder.getFolderRelativePath());
            return new Result(200,"文件重命名成功",temp);
        }else {
            return new Result(205,"文件重命名失败,请检查参数是否正确",null);
        }

    }
}
