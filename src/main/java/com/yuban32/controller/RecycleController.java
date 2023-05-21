package com.yuban32.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.entity.User;
import com.yuban32.entity.UserStorageQuota;
import com.yuban32.mapper.FileInfoMapper;
import com.yuban32.mapper.FolderMapper;
import com.yuban32.mapper.UserStorageQuotaMapper;
import com.yuban32.response.Result;
import com.yuban32.service.FileInfoService;
import com.yuban32.service.FolderService;
import com.yuban32.service.UserService;
import com.yuban32.util.JWTUtils;
import com.yuban32.vo.FileAndFolderVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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
    private UserStorageQuotaMapper userStorageQuotaMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private JWTUtils jwtUtils;

    /***
     * @description 获取回收站内的文件列表
     * @param request
     * @return Result
     **/
    @GetMapping
    @RequiresAuthentication
    public Result getRecycleFileList(HttpServletRequest request) throws IOException {
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        HashSet<FileAndFolderVO> fileAndFolderVO = new HashSet<>();
        List<Folder> foldersList = folderService.list(new QueryWrapper<Folder>().eq("username", userName).eq("folder_status", 0));

        List<FileInfo> fileInfoList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_uploader", userName).eq("f_status", 0));
        HashSet<String> folderUUIDList = new HashSet<>();
        String domain = request.getServerName();
        int port = request.getServerPort();
        //拼接缩略图
        String imagePathURL = domain + ":" + port + "/images/";
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
    @RequiresAuthentication
    public Result removeFileToRecycle(@RequestParam("currentFolderUUID") String currentFolderUUID,
                                      @RequestParam("fileName") String fileName,
                                      HttpServletRequest request) {
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        //根据当前文件夹的UUID和文件名的为条件 将文件移入回收站
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
    @RequiresAuthentication
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
     * @description 永久删除文件 管理员和用户共用方法
     * @param fileUUID
     * @param request
     * @return Result
     **/
    @SneakyThrows
    @PostMapping("/trash")
    @RequiresAuthentication
    public Result permanentlyDelete(@RequestParam("fileUUID") String fileUUID, HttpServletRequest request) {
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        User userInfo = userService.getOne(new QueryWrapper<User>().eq("username", userName));
        //处理文件
        List<FileInfo> fileInfoList = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_md5", fileUUID));
        //如果数据库中查到当前文件存在多份的话  则只从数据库中删除当前文件 当数据库中只剩一份时 则执行IO删除操作
        //判断 先查询文件 当查询结果为空时 执行删除文件夹 不为空时
        log.info("fileInfoList{}", fileInfoList.isEmpty());
        if (!fileInfoList.isEmpty()) {
            //获取文件的大小
            double fileSize = fileInfoList.get(0).getFileSize();
            //调用封装好的方法 删除文件
            boolean deleteFile = deleteFile(fileInfoList, false, null, null,userInfo.getRole());
            if (deleteFile) {
                //先将用户已用的存储配额查询出来
                //然后减去要删除文件的大小
                //再更新到数据库中,完成恢复用户空间
                UserStorageQuota userStorageQuota = userStorageQuotaMapper.selectUserStorageQuotaByUserName(userName);
                userStorageQuotaMapper.updateUsedStorageByUsername(userStorageQuota.getUsedStorage() - fileSize, userName);
                return new Result(200, "文件彻底删除成功", null);
            } else {
                return new Result(205, "文件彻底删除失败", null);
            }
        } else {
            //执行删除文件夹
            log.info("执行删除文件夹");
            QueryWrapper<Folder> folderQueryWrapper = null;
            QueryWrapper<Folder> childrenFolderListQueryWrapper = null;
            QueryWrapper<FileInfo> childrenFileListQueryWrapper = null;
            //这个判断用于判断当前用户是否是管理员
            if (userInfo.getRole().equals("admin")) {
                folderQueryWrapper = new QueryWrapper<Folder>().eq("folder_uuid", fileUUID);
                childrenFolderListQueryWrapper = new QueryWrapper<Folder>().eq("parent_folder_uuid", fileUUID);
                childrenFileListQueryWrapper = new QueryWrapper<FileInfo>().eq("f_parent_id", fileUUID);

            } else {
                folderQueryWrapper = new QueryWrapper<Folder>().eq("folder_uuid", fileUUID).eq("username", userName);
                childrenFolderListQueryWrapper = new QueryWrapper<Folder>().eq("parent_folder_uuid", fileUUID).eq("username", userName);
                childrenFileListQueryWrapper = new QueryWrapper<FileInfo>().eq("f_parent_id", fileUUID).eq("f_uploader", userName);

            }
            //判断是文件夹是否存在
            Folder folderExists = folderService.getOne(folderQueryWrapper);
            if (folderExists != null) {
                //存在 开始判断是否有子文件夹
                List<FileInfo> childrenFileList = fileInfoService.list(childrenFileListQueryWrapper);
                //查询子文件在数据库是否有多个文件

                List<Folder> childrenFolderList = folderService.list(childrenFolderListQueryWrapper);
                //有子文件夹就删除连同子文件夹一块删除

                if (!childrenFolderList.isEmpty()) {
                    folderService.remove(childrenFolderListQueryWrapper);
                }//有子文件就删除连同子文件一块删除
                if (!childrenFileList.isEmpty()) {
                    //调用封装好的方法来删除子文件
                    boolean deleteFile = false;
                    boolean multipleFiles = false;
                    boolean removeFolder = false;
                    List<FileInfo> multipleFile = fileInfoService.list(new QueryWrapper<FileInfo>().eq("f_md5", childrenFileList.get(0).getFileMD5()));
                    if(multipleFile.size()>1){
                        multipleFiles = fileInfoService.remove(new QueryWrapper<FileInfo>().eq("f_parent_id", fileUUID));
                        removeFolder = folderService.remove(folderQueryWrapper);
                    }else {
                        deleteFile = deleteFile(childrenFileList, true, fileUUID, userName,userInfo.getRole());
                        removeFolder = folderService.remove(folderQueryWrapper);
                    }

                    if (deleteFile||multipleFiles||removeFolder) {
                        return new Result(200, "文件彻底删除成功", null);
                    } else {
                        return new Result(205, "文件彻底删除失败", null);
                    }
                }
                //如果不存在子文件和子文件夹的话  就直接把当前文件夹删除就好了
                folderService.remove(folderQueryWrapper);
                return new Result(200, "已彻底删除", null);
            } else {
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
    @RequiresAuthentication
    public Result restore(@RequestParam("fileUUID") String fileUUID, @RequestParam("type") String type, HttpServletRequest request) {
        //只需改变文件和文件夹的status就行 1==不在回收站内  0==在回收站内
        String userName = jwtUtils.getClaimByToken(request.getHeader("Authorization")).getSubject();
        Boolean restoreFile = false;
        Boolean restoreFolder = false;
        UpdateWrapper<Folder> folderUpdateWrapper = new UpdateWrapper<Folder>().eq("parent_folder_uuid", fileUUID);
        UpdateWrapper<FileInfo> fileUpdateWrapper = new UpdateWrapper<FileInfo>().eq("f_parent_id", fileUUID);
        //根据类型判断
        if (type.equals("file")) {
            restoreFile = fileInfoMapper.restoreFile(fileUUID, userName);
        }
        if (type.equals("folder")) {
            restoreFolder = folderMapper.restoreFolder(fileUUID, userName);
            folderUpdateWrapper.set("folder_status",1);
            fileUpdateWrapper.set("f_status",1);
            folderService.update(folderUpdateWrapper);
            fileInfoService.update(fileUpdateWrapper);
        }
        if (restoreFile || restoreFolder) {
            return new Result(200, "恢复成功", null);
        } else {
            return new Result(205, "恢复失败,请联系管理员", null);
        }
    }

    //公用方法
    public boolean deleteFile(List<FileInfo> fileInfoList, Boolean isChildren, String fileUUID, String userName,String role) throws IOException {
        boolean result = false;
        QueryWrapper<FileInfo> fileInfoQueryWrapper = null;
        if(role.equals("admin")){
            //管理员
            if (isChildren) {
                fileInfoQueryWrapper = new QueryWrapper<FileInfo>()
                        .eq("f_parent_id", fileUUID);
            } else {
                fileInfoQueryWrapper = new QueryWrapper<FileInfo>()
                        .eq("f_md5", fileInfoList.get(0).getFileMD5());
            }
        }else {
            //用户
            if (isChildren) {
                fileInfoQueryWrapper = new QueryWrapper<FileInfo>()
                        .eq("f_parent_id", fileUUID)
                        .eq("f_uploader", userName)
                        .eq("f_status", 0);
            } else {
                fileInfoQueryWrapper = new QueryWrapper<FileInfo>()
                        .eq("f_md5", fileInfoList.get(0).getFileMD5())
                        .eq("f_uploader", fileInfoList.get(0).getFileUploader())
                        .eq("f_status", 0);
            }
        }
        //传来的数据集合长度是否大于1
        if (fileInfoList.size() > 1) {
            // 多份删除数据字段
            result = fileInfoService.remove(fileInfoQueryWrapper);
        } else {
            // 单份IO删除
            FileInfo fileInfo = fileInfoList.get(0);

            // 拼接完整路经
            String fileName = fileInfo.getFileMD5() + "." + fileInfo.getFileExtension();
            // 获取文件
            File checkFileType = new File(fileInfo.getFileAbsolutePath(), fileName);
            //利用tika这个库来判断文件类型
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

            // 如果文件存在 用FileUtils工具类强制删除文件
            if (checkFileType.exists()) {
                FileUtils.forceDelete(checkFileType);
            }
            //最后删除数据库里的记录
            result = fileInfoService.remove(fileInfoQueryWrapper);
        }

        return result;
    }
}
