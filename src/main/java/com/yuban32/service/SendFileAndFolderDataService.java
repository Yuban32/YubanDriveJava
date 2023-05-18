package com.yuban32.service;

import com.yuban32.entity.FileInfo;
import com.yuban32.entity.Folder;
import com.yuban32.vo.FileAndFolderVO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * @author: Yuban32
 * @Date: 2023年05月19日
 */

public interface SendFileAndFolderDataService {
    List<FileAndFolderVO> sendFileInfoListAndFolderListData(List<Folder> folderList, List<FileInfo> fileInfoList,String imagePathURL,String thumbnailUrl, HttpServletRequest request) throws IOException;
    List<FileAndFolderVO> sendFileInfoListData(List<FileInfo> fileInfoList,String imagePathURL,String thumbnailUrl, HttpServletRequest request) throws IOException;
    List<FileAndFolderVO> sendFolderListData(List<Folder> folderList,HttpServletRequest request) throws IOException;

}
