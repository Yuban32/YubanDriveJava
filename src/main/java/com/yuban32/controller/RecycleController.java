package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.mapper.FileInfoMapper;
import com.yuban32.mapper.FolderMapper;
import com.yuban32.response.Result;
import com.yuban32.service.FileInfoService;
import com.yuban32.service.FolderService;
import com.yuban32.util.JWTUtils;
import com.yuban32.vo.FileAndFolderVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yuban32
 * @ClassName RecycleController
 * @Description 回收站
 * @Date 2023年04月22日
 */
@Slf4j
@RestController
@RequestMapping("/recycle")
public class RecycleController {
    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FileInfoMapper fileInfoMapper;
    @Autowired
    private JWTUtils jwtUtils;

    /***
     * @description 获取回收站内的文件列表
     * @param request
     * @return Result
     **/
    @GetMapping
    public Result getRecycleFileList(HttpServletRequest request) throws IOException {
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        HashSet<FileAndFolderVO> fileAndFolderVO = new HashSet<>();
        List<Folder> foldersList = folderService.list(new QueryWrapper<Folder>().eq("username", userName).eq("folder_status", 0));

        List<FileInfo> fileInfoList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_uploader", userName).eq("f_status", 0));
        HashSet<String> folderUUIDList = new HashSet<>();
        String domain = request.getServerName();
        int port = request.getServerPort();
        //拼接缩略图
        String imagePathURL = domain + ":" + port + "/download/" + userName + "/userUpload/";
        String thumbnailUrl = imagePathURL + "Thumbnail" + File.separator + "Thumbnail";

        if (foldersList.isEmpty() && fileInfoList.isEmpty()) {
            return new Result(205, "没有查到文件", null);
        } else {

            for (Folder folders : foldersList) {
                //将查询到的文件UUID存入数组
                folderUUIDList.add(folders.getFolderUUID());
            }

            for (Folder folders : foldersList) {
                //只查询父文件夹
                if (!folderUUIDList.contains(folders.getParentFolderUUID())) {
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
            }

            for (FileInfo fileInfo : fileInfoList) {
                // 只查询父文件，并且重复数据已被过滤
                if (!folderUUIDList.contains(fileInfo.getFileParentId())) {
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
                    if (fileType[0].equals("image")) {
                        temp.setThumbnailURL(thumbnailUrl + fileInfo.getFileMD5() + "." + fileInfo.getFileType());
                        temp.setFullSizeImageURL(imagePathURL + fileInfo.getFileMD5() + "." + fileInfo.getFileType());
                    }
                    fileAndFolderVO.add(temp); // 添加到Set集合中
                }
            }
            return new Result(200, "文件列表查询成功", fileAndFolderVO);
        }
    }

    /***
     * @description 将文件移动到回收站
     * @param currentFolderUUID
     * @param fileName
     * @param request
     * @return Result
     **/
    @PostMapping("/file")
    public Result removeFileToRecycle(@RequestParam("currentFolderUUID") String currentFolderUUID,
                                      @RequestParam("fileName") String fileName,
                                      HttpServletRequest request) {
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        //根据当前文件夹的UUID和文件名还有用户名为条件 将文件移入回收站
        Boolean isSuccess = fileInfoMapper.removeFileToRecycle(currentFolderUUID, fileName, userName);
        if (isSuccess) {
            return Result.success("文件已移入回收站", null);
        } else {
            return new Result(205, "移动失败,请检查文件是否存在", null);
        }
    }

    /***
     * @description 将文件夹移动到回收站
     * @param currentFolderUUID
     * @param request
     * @return Result
     **/
    @PostMapping("/folder")
    public Result removeFolderToRecycle(@RequestParam("currentFolderUUID") String currentFolderUUID, HttpServletRequest request) {
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        //根据当前文件夹的UUID 查询文件夹下有没有子文件和子文件夹
        Folder isExist = folderService.getOne(new QueryWrapper<Folder>().eq("folder_uuid", currentFolderUUID).eq("username", userName));
        if (isExist != null) {
            List<Folder> folderList = folderService.selectFolderListByUserNameAndParentFolderUUID(userName, currentFolderUUID);
            List<FileInfo> fileInfoList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_uploader", userName).eq("f_parent_id", currentFolderUUID));
            log.info("folders {}", folderList);
            log.info("fileInfoList {}", fileInfoList);
            if (!folderList.isEmpty() && !folderList.isEmpty()) {
                folderMapper.removeFolderToRecycle(currentFolderUUID, userName);
                folderMapper.removeChildrenFolderToRecycle(currentFolderUUID, userName);
                fileInfoMapper.removeChildrenFileToRecycle(currentFolderUUID, userName);
                return Result.success("文件夹已移入回收站", null);
            } else {
                folderMapper.removeFolderToRecycle(currentFolderUUID, userName);
                return Result.success("文件夹已移入回收站", null);
            }
        } else {
            return new Result(205, "移动失败,文件夹不存在", null);
        }
    }

    /***
     * @description 永久删除文件
     * @param fileUUID
     * @param request
     * @return Result
     **/
    //TODO 完善删除后恢复用户空间
    @SneakyThrows
    @PostMapping("/trash")
    public Result permanentlyDelete(@RequestParam("fileUUID") String fileUUID, HttpServletRequest request) {
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        //处理文件
        List<FileInfo> fileInfoList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_md5", fileUUID));
        log.info("{}", fileInfoList);
        //如果数据库中查到当前文件存在多份的话  则只从数据库中删除当前文件 当数据库中只剩一份时 则执行IO删除操作
        //判断 先查询文件 当查询结果为空时 执行删除文件夹 不为空时 执行删除文件
        if (!fileInfoList.isEmpty()) {
            boolean deleteFile = deleteFile(fileInfoList , false , null,null);
            if (deleteFile){
                return new Result(200,"文件彻底删除成功",null);
            }else {
                return new Result(205,"文件彻底删除失败",null);
            }

        } else {
            //执行删除文件夹
            QueryWrapper<FileInfo> childrenFileListQueryWrapper = new QueryWrapper<FileInfo>().eq("f_parent_id", fileUUID).eq("f_uploader", userName).eq("f_status", 0);
            QueryWrapper<Folder> childrenFolderListQueryWrapper = new QueryWrapper<Folder>().eq("parent_folder_uuid", fileUUID).eq("username", userName).eq("folder_status", 0);
            QueryWrapper<Folder> folderQueryWrapper = new QueryWrapper<Folder>().eq("folder_uuid", fileUUID).eq("username", userName).eq("folder_status", 0);
            Folder one = folderService.getOne(folderQueryWrapper);
            if(one != null){
                List<FileInfo> childrenFileList = fileInfoService.list(childrenFileListQueryWrapper);
                List<Folder> childrenFolderList = folderService.list(childrenFolderListQueryWrapper);
                if (!childrenFolderList.isEmpty()){
                    folderService.remove(childrenFolderListQueryWrapper);
                }
                if (!childrenFileList.isEmpty()) {
                    boolean deleteFile = deleteFile(childrenFileList , true , fileUUID,userName);
                    if (deleteFile){
                        return new Result(200,"文件彻底删除成功",null);
                    }else {
                        return new Result(205,"文件彻底删除失败",null);
                    }
                }
                folderService.remove(folderQueryWrapper);
                return new Result(200, "已彻底删除", null);
            }else {
                return new Result(205, "删除失败，文件夹不存在", null);
            }
        }
    }
    /***
     * @description 恢复文件
     * @param fileUUID
     * @param type
     * @param request
     * @return Result
     **/

    @PostMapping("/restore")
    public Result restore(@RequestParam("fileUUID")String fileUUID , @RequestParam("type")String type , HttpServletRequest request){
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        Boolean restoreFile = false;
        Boolean restoreFolder = false;
        if(type.equals("file")){
            restoreFile = fileInfoMapper.restoreFile(fileUUID, userName);
        }
        if(type.equals("folder")){
            restoreFolder = folderMapper.restoreFolder(fileUUID, userName);
        }
        if(restoreFile || restoreFolder){
            return new Result(200,"恢复成功",null);
        }else {
            return new Result(205,"恢复失败,请联系管理员",null);
        }
    }

    //公用方法
    public boolean deleteFile(List<FileInfo> fileInfoList,Boolean isChildren,String fileUUID,String userName) throws IOException {
        boolean result = false;
        QueryWrapper<FileInfo> fileInfoQueryWrapper = null;
        if(isChildren){
            fileInfoQueryWrapper = new QueryWrapper<FileInfo>()
                    .eq("f_parent_id", fileUUID)
                    .eq("f_uploader", userName)
                    .eq("f_status", 0);
        }else {
            fileInfoQueryWrapper = new QueryWrapper<FileInfo>()
                    .eq("f_md5", fileInfoList.get(0).getFileMD5())
                    .eq("f_uploader", fileInfoList.get(0).getFileUploader())
                    .eq("f_status", 0);
        }
        if (fileInfoList.size() > 1) {
            // 多份删除数据字段
            result = fileInfoService.remove(fileInfoQueryWrapper);
        } else {
            // 单份IO删除
            FileInfo fileInfo = fileInfoList.get(0);

            // 拼接文件名
            String fileName = fileInfo.getFileMD5() + "." + fileInfo.getFileType();
            // 获取文件
            File checkFileType = new File(fileInfo.getFileAbsolutePath(), fileName);

            Tika tika = new Tika();
            String detect = tika.detect(checkFileType);
            String[] fileType = detect.split("/");
            // 如果是图片，删除缩略图
            if (fileType[0].equals("image")) {
                File thumbnailFile = new File(fileInfo.getFileAbsolutePath(), "Thumbnail" + File.separator + "Thumbnail" + fileName);
                if (thumbnailFile.exists()) {
                    FileUtils.forceDelete(thumbnailFile);
                }
            }

            // 删除文件
            if (checkFileType.exists()) {
                FileUtils.forceDelete(checkFileType);
            }

            result = fileInfoService.remove(fileInfoQueryWrapper);
        }

        return result;
    }
}
