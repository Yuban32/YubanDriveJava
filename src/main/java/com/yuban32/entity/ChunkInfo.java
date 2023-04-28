package com.yuban32.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Yuban32
 * @ClassName ChunkInfo
 * @Description
 * @Date 2023年03月21日
 */
@Data
@TableName("t_chunk")
@Accessors(chain = true)
public class ChunkInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "c_id",type = IdType.AUTO)
    private Integer id;
    @TableField(value = "c_md5")
    private String chunkMD5;
    @TableField(value = "c_index")
    private Integer chunkIndex;
    @TableField(value = "c_location")
    private String chunkLocation;
}
