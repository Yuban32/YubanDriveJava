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
    @PostMapping("/create")
//    @RequiresAuthentication
    public Result createFolder(
            @RequestParam("folderUUID") String requestFolderUUID,
            @RequestParam("newFolderName") String requestNewFolderName,
            HttpServletRequest request){
        String username = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        if(requestFolderUUID.equals("root")){
            requestFolderUUID = username;
        }
        return folderService.createFolder(requestFolderUUID,requestNewFolderName, username);
    }
    @GetMapping("/list")
    @RequiresAuthentication
    public Result selectFolderByUserNameAndParentFolderUUID(@RequestParam("parentFolderUUID")String parentFolderUUID,HttpServletRequest request){
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        if(parentFolderUUID.equals("root")){
            parentFolderUUID = userName;
        }
        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();
        List<Folder> foldersList = folderService.list(new QueryWrapper<Folder>().eq("username",userName).eq("parent_folder_uuid",parentFolderUUID).eq("folder_status",1));
        if (foldersList.isEmpty()){
            return new Result(205,"没有查到文件",null);
        }else{
            for( Folder folders : foldersList){
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
            return new Result(200,"文件列表查询成功",fileAndFolderVO);
        }
    }
    @PostMapping("/rename")
    @RequiresAuthentication
    public Result folderRename(@Validated @RequestBody FolderRenameDTO renameDTO , HttpServletRequest request){
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        Boolean folderRename = folderMapper.folderRename(renameDTO.getCurrentFolderUUID(), renameDTO.getFolderName(), userName);
        log.info("{},{}",renameDTO,folderRename);
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
