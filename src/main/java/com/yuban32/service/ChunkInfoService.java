package com.yuban32.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuban32.entity.ChunkInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author: Yuban32
 * @Date: 2023年03月21日
 */

public interface ChunkInfoService{
    Integer saveChunk(MultipartFile chunk,String md5,Integer index,Long chunkSize,String resultFileName,String directory);
    List<Integer> selectChunkListByMd5(String md5);

    void deleteChunkByMd5(String md5);

    byte[] getChunk(Integer index, Integer chunkSize, String resultFileName,long offset);

}
