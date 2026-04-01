package com.lyh.live.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("live_room")
public class LiveRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long anchorId;
    private String title;
    private String coverImg;
    private Integer status;
    private String streamKey;
    private LocalDateTime createTime;
}
