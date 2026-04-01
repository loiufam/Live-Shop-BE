package com.lyh.live.service;

import com.lyh.live.entity.LiveRoom;
import com.lyh.live.vo.PageResult;

import java.util.Optional;

public interface LiveRoomService {
    
    /**
     * 查询所有直播中的房间 (status=1)
     */
    PageResult<LiveRoom> getLiveRoomsPage(int page, int pageSize);

    /**
     * 通过房间 id 找当前直播中的房间 (status=1)
     */
    Optional<LiveRoom> findActiveRoomById(Long roomId);
    
    /**
     * 通过房间 id 查找房间 (不限状态)
     */
    Optional<LiveRoom> findRoomById(Long roomId);

    /**
     * 创建房间
     */
    void save(LiveRoom liveRoom);

    /**
     * 修改房间
     */
    void updateRoom(LiveRoom liveRoom);
    
    /**
     * 更新房间状态
     * @param roomId 房间ID
     * @param status 状态: 0-准备中, 1-直播中, 2-已结束
     */
    void updateRoomStatus(Long roomId, Integer status);
    
    /**
     * 检查用户是否有正在直播的房间
     */
    boolean hasActiveRoom(Long anchorId);
}
