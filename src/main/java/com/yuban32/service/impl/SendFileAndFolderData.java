package com.yuban32.service.impl;

import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.service.SendFileAndFolderDataService;
import com.yuban32.vo.FileAndFolderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName SendFileAndFolderData
 * @Description
 * @Date 2023年05月19日
 */
@Slf4j
@Service
public class SendFileAndFolderData implements SendFileAndFolderDataService {
    /**
     * @description 封装文件数据
     * @param folderList
     * @param fileInfoList
     * @param imagePathURL
     * @param thumbnailUrl
     * @param request
     * @return List<FileAndFolderVO>
     **/
    @Override
    public List<FileAndFolderVO> sendFileInfoListAndFolderListData(List<Folder> folderList, List<FileInfo> fileInfoList, String imagePathURL, String thumbnailUrl, HttpServletRequest request) throws IOException {
        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            FileAndFolderVO temp = convertToVO(fileInfo, null, imagePathURL, thumbnailUrl);
            fileAndFolderVO.add(temp);
        }

        for (Folder folder : folderList) {
            FileAndFolderVO temp = convertToVO(folder, null, null, null);
            fileAndFolderVO.add(temp);
        }

        return fileAndFolderVO;
    }

    @Override
    public List<FileAndFolderVO> sendFileInfoListData(List<FileInfo> fileInfoList, String imagePathURL, String thumbnailUrl, HttpServletRequest request) throws IOException {
        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            FileAndFolderVO temp = convertToVO(fileInfo, null, imagePathURL, thumbnailUrl);
            fileAndFolderVO.add(temp);
        }
        return fileAndFolderVO;
    }

    @Override
    public List<FileAndFolderVO> sendFolderListData(List<Folder> folderList, HttpServletRequest request) throws IOException {
        List<FileAndFolderVO> fileAndFolderVO = new ArrayList<>();
        for( Folder folders : folderList){
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
        return fileAndFolderVO;
    }
    private FileAndFolderVO convertToVO(Object object, String category, String imagePathURL, String thumbnailUrl) throws IOException {
        FileAndFolderVO temp = new FileAndFolderVO();
        temp.setCategory(category);

        if (object instanceof Folder) {
            Folder folder = (Folder) object;
            temp.setType("folder");
            temp.setName(folder.getFolderName());
            temp.setFileUUID(folder.getFolderUUID());
            temp.setParentFileUUID(folder.getParentFolderUUID());
            temp.setSize(null);
            temp.setUploader(folder.getUsername());
            temp.setCreatedTime(folder.getFolderCreateTime());
            temp.setRelativePath(folder.getFolderRelativePath());
        } else if (object instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) object;
            temp.setType("file");
            temp.setName(fileInfo.getFileName());
            temp.setFileUUID(fileInfo.getFileMD5());
            temp.setParentFileUUID(fileInfo.getFileParentId());
            temp.setSize(fileInfo.getFileSize());
            temp.setUploader(fileInfo.getFileUploader());
            temp.setCreatedTime(fileInfo.getFileUploadTime());
            temp.setRelativePath(fileInfo.getFileRelativePath());
            temp.setFileExtension(fileInfo.getFileExtension());

            File checkFileType = new File(fileInfo.getFileAbsolutePath(), fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
            Tika tika = new Tika();
            String detect = tika.detect(checkFileType);
            String[] fileType = detect.split("/");
            temp.setCategory(detect);

            if (fileType[0].equals("image")) {
                temp.setThumbnailURL(thumbnailUrl + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
                temp.setFullSizeImageURL(imagePathURL + fileInfo.getFileMD5() + "." + fileInfo.getFileExtension());
            }
        }
        return temp;
    }
}
