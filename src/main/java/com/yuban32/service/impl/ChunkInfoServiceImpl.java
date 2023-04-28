package com.yuban32.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuban32.entity.ChunkInfo;
import com.yuban32.mapper.ChunkMapper;
import com.yuban32.service.ChunkInfoService;
import com.yuban32.service.FolderService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuban32
 * @ClassName ChunkInfoServiceImpl
 * @Description
 * @Date 2023年03月21日
 */
@Slf4j
@Service
public class ChunkInfoServiceImpl implements ChunkInfoService {

    @Autowired
    private ChunkMapper chunkMapper;
    @Autowired
    private FolderService folderService;
    @Value("${base-file-path.file-path}")
    private String filePath;
    @Value("${base-file-path.user-upload-file-path}")
    private String userUploadFilePath;
    @Override
    public Integer saveChunk(MultipartFile chunk, String md5, Integer index, Long chunkSize, String resultFileName, String username) {
        String directory = filePath + File.separator + username +File.separator + userUploadFilePath;
        if(!Files.isWritable(Paths.get(directory))){
            log.info("路径不存在,新建路径:{}",directory);
            try{
                Files.createDirectories(Paths.get(directory));
            } catch (IOException e) {
                log.error("创建路径失败:{},{}",e.getMessage(),e);
            }
        }
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(resultFileName,"rw")){
            //定义偏移量
            long offset = chunkSize * (index-1);
            //定位到该分片的偏移量
            randomAccessFile.seek(offset);
            //写入文件
            randomAccessFile.write(chunk.getBytes());
            ChunkInfo chunkInfo = new ChunkInfo();
            chunkInfo.setChunkMD5(md5);
            chunkInfo.setChunkIndex(index);
            chunkInfo.setChunkLocation(resultFileName);
            return chunkMapper.insertChunk(chunkInfo);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Integer> selectChunkListByMd5(String md5) {
        List<ChunkInfo> chunkInfoList = chunkMapper.selectChunkListByMd5(md5);
        ArrayList<Integer> indexList = new ArrayList<>();
        for(ChunkInfo chunkInfo : chunkInfoList){
            indexList.add(chunkInfo.getChunkIndex());
        }
        return indexList;
    }

    @Override
    public void deleteChunkByMd5(String md5) {
        chunkMapper.deleteChunkByMd5(md5);
    }

    @Override
    public byte[] getChunk(Integer index, Integer chunkSize, String resultFileName, long offset) {
        File resultFile = new File(resultFileName);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(resultFileName,"r")){
            //根据偏移量定位分片
            randomAccessFile.seek(offset);
            //开始读取文件
            byte[] buffer = new byte[chunkSize];
            randomAccessFile.read(buffer);
            return buffer;
        }catch(IOException e){
            e.printStackTrace();;
        }
        return null;
    }
}
