package com.yuban32.mapper;

import com.yuban32.entity.ChunkInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Yuban32
 * @ClassName ChunkMapper
 * @Description
 * @Date 2023年03月21日
 */
@Mapper
public interface ChunkMapper {
    List<ChunkInfo> selectChunkListByMd5(String md5);

    Integer insertChunk(ChunkInfo chunkinfo);

    void deleteChunkByMd5(String md5);
}
