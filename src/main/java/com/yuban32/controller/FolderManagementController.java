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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName FolderManagementController
 * @Description 文件夹管理
 * @Date 2023年03月25日
 */
@Slf4j
@RestController
@RequestMapping("/folderManagement")
public class FolderManagementController {
    @Autowired
    private FolderService folderService;
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private JWTUtils jwtUtils;

    @GetMapping("/list")
    public Result getAllUserFolder(){
        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();
        List<Folder> foldersList = folderService.list(new QueryWrapper<Folder>().eq("folder_status",0));
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

    @PostMapping("/copy")
//    @RequiresAuthentication
    public Result folderCopy(@RequestParam("currentFolderUUID")String currentFolderUUID,
                             @RequestParam("targetFolderUUID")String targetFolderUUID){
        log.info("查询的参数currentFolderUUID=>{}, targetFolderUUID=>{} ",currentFolderUUID,targetFolderUUID);
        /*
        * 查询部分流程如下 必须要做数据去重
        *   找出当前文件夹
        *   使用currentFolderUUID查询t_folder表中parent_folder_uuid字段 查出所有父文件夹是currentFolderUUID的数据
        *   再使用currentFolderUUID查询t_file表中f_parent_id字段 查出所有父文件夹是currentFolderUUID的数据
        *   再使用newFolderUUID查询t_folder表中folder_uuid字段 查出目标文件夹的数据
        * */
        //查询
        QueryWrapper<Folder> folderQueryWrapper = new QueryWrapper<Folder>();
        QueryWrapper<FileInfo> fileInfoQueryWrapper = new QueryWrapper<FileInfo>();
        Folder currentFolder = folderService.getOne(folderQueryWrapper.eq("folder_uuid", currentFolderUUID));
        log.info("当前文件夹,{}",currentFolder);
        //当前文件夹下的子文件和子文件夹
        List<Folder> currentFolderChildrenFolders = folderMapper.selectFolderListByParentFolderUUID(currentFolderUUID);
        log.info("当前文件夹下的子文件夹,{}",currentFolderChildrenFolders);

        List<FileInfo> currentFolderChildrenFiles = fileInfoService.list(fileInfoQueryWrapper.eq("f_parent_id", currentFolderUUID));
        log.info("当前文件夹下的子文件,{}",currentFolderChildrenFiles);

        Folder targetFolder = folderMapper.selectFolderByFolderUUID(targetFolderUUID);
        log.info("目标文件夹,{}",targetFolder);

        //目标文件夹下的子文件和子文件夹
        List<Folder> targetFolderChildrenFolders = folderMapper.selectFolderListByParentFolderUUID(targetFolderUUID);
        log.info("目标文件夹下的子文件夹,{}",targetFolderChildrenFolders);

        List<FileInfo> targetFolderChildrenFiles = fileInfoService.list(fileInfoQueryWrapper.eq("f_parent_id", targetFolderUUID));
        log.info("目标文件夹下的子文件,{}",targetFolderChildrenFiles);

        /*
        * 新增数据原理 遍历子文件和子文件夹的数据来修改
        *   修改currentFolder的相对路径等于newFolder的相对路径加上currentFolder的名字 修改currentFolder的parent_folder_uuid等于newFolder的UUID
        *   修改currentFolderChildrenFolders和currentFolderChildrenFiles下所有的相对路径都等于newFolder的相对路径加上他们自己的名字
        * 通过修改当前子文件的f_parent_id和f_relative_path 使其指向新的文件夹即可
        * 因为是复制粘贴 所以无需考虑删除旧数据
        * */

        /*
        * 去重 只需判断目标文件夹下有没有和当前文件夹同名的子文件夹即可
        * 因为子文件默认都不是重复的
        * 判断文件夹名字就行了 因为每个文件夹的UUID都是唯一的 不管是否同名
        * */
        String[] currentFolderPath = currentFolder.getFolderRelativePath().split("\\\\");
        String[] targetFolderPath = targetFolder.getFolderRelativePath().split("\\\\");
        int i = currentFolderPath.length-1;
        int j = targetFolderPath.length-1;
        String commonFolderPath;
        while(i >= 0 && j >= 0 && currentFolderPath[i].equals(targetFolderPath[j])){
            i--;
            j--;
        }
        if (i >= 0){
            commonFolderPath = String.join("\\",Arrays.copyOfRange(currentFolderPath,0,i+1));
            System.out.println(commonFolderPath);
            System.out.println(currentFolder.getFolderRelativePath());
        }

        // TODO 继续完善复制文件功能 主要是复制文件夹要想如何修改子文件夹的目录
        boolean notRepeat = false;
        for (int k = 0; k < targetFolderChildrenFolders.size(); k++){
            if (targetFolderChildrenFolders.get(k).getFolderName().equals(currentFolder.getFolderName())){
                return new Result(400, "无法复制到目标文件夹,因为目标文件夹的子文件夹和当前文件夹有同名", null);
            } else {
                notRepeat = true;
            }
        }
//        if(notRepeat){
//
////            currentFolder.setFolderRelativePath(newFolder.getFolderRelativePath() + File.separator + currentFolder.getFolderName());
////            for (int j = 0; j < currentFolderChildrenFolders.size(); j++){
//////                currentFolderChildrenFolders.get(j).setFolderRelativePath()
//////                if(currentFolderChildrenFolders.get(j).getFolderRelativePath().replace())
////            }
//
//        }

    return Result.success("修改成功");
    }

}
