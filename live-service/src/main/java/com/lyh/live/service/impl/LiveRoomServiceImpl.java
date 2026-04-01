package com.lyh.live.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyh.live.entity.LiveRoom;
import com.lyh.live.mapper.LiveRoomMapper;
import com.lyh.live.service.LiveRoomService;
import com.lyh.live.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveRoomServiceImpl implements LiveRoomService {

    private final LiveRoomMapper liveRoomMapper;

    @Override
    public PageResult<LiveRoom> getLiveRoomsPage(int page, int pageSize) {
        Page<LiveRoom> mpPage = new Page<>(page, pageSize);
        // 只查询正在直播的房间 (status = 1)
        liveRoomMapper.selectPage(mpPage, new LambdaQueryWrapper<LiveRoom>()
                .eq(LiveRoom::getStatus, 1)
                .orderByDesc(LiveRoom::getCreateTime));

        List<LiveRoom> records = mpPage.getRecords();
        if (records.isEmpty()) {
            PageResult<LiveRoom> empty = new PageResult<>();
            empty.setTotal(0L);
            empty.setRooms(Collections.emptyList());
            return empty;
        }

        // 修复BUG：之前错误地使用了 room 而非 r，导致数据映射为空
        List<LiveRoom> roomList = records.stream().map(r -> {
            LiveRoom room = new LiveRoom();
            room.setId(r.getId());
            room.setTitle(r.getTitle());
            room.setAnchorId(r.getAnchorId());
            room.setCoverImg(r.getCoverImg());
            room.setStatus(r.getStatus());
            room.setCreateTime(r.getCreateTime());
            // 注意：不返回 streamKey，这是敏感信息
            return room;
        }).collect(Collectors.toList());

        PageResult<LiveRoom> result = new PageResult<>();
        result.setTotal(mpPage.getTotal());
        result.setRooms(roomList);
        return result;
    }

    @Override
    public Optional<LiveRoom> findActiveRoomById(Long roomId) {
        LiveRoom room = liveRoomMapper.selectOne(
                Wrappers.<LiveRoom>lambdaQuery()
                        .eq(LiveRoom::getId, roomId)
                        .eq(LiveRoom::getStatus, 1)
        );
        return Optional.ofNullable(room);
    }
    
    @Override
    public Optional<LiveRoom> findRoomById(Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        return Optional.ofNullable(room);
    }

    @Override
    public void save(LiveRoom liveRoom) {
        liveRoomMapper.insert(liveRoom);
    }

    @Override
    public void updateRoom(LiveRoom liveRoom) {
        liveRoomMapper.updateById(liveRoom);
    }
    
    @Override
    public void updateRoomStatus(Long roomId, Integer status) {
        LiveRoom room = new LiveRoom();
        room.setId(roomId);
        room.setStatus(status);
        liveRoomMapper.updateById(room);
    }
    
    @Override
    public boolean hasActiveRoom(Long anchorId) {
        Long count = liveRoomMapper.selectCount(
                Wrappers.<LiveRoom>lambdaQuery()
                        .eq(LiveRoom::getAnchorId, anchorId)
                        .in(LiveRoom::getStatus, 0, 1) // 准备中或直播中
        );
        return count > 0;
    }
}
