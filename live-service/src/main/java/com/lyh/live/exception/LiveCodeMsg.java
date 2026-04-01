package com.lyh.live.exception;

import com.lyh.common.result.CodeMsg;

public class LiveCodeMsg extends CodeMsg {
    
    private LiveCodeMsg(Integer code, String msg) {
        super(code, msg);
    }
    
    // 直播相关错误码 2001-2999
    public static final LiveCodeMsg ROOM_NOT_FOUND = new LiveCodeMsg(2001, "直播间不存在");
    public static final LiveCodeMsg ROOM_NOT_ACTIVE = new LiveCodeMsg(2002, "直播间不存在或已结束");
    public static final LiveCodeMsg ROOM_ALREADY_EXISTS = new LiveCodeMsg(2003, "您已经有一个正在直播的房间");
    public static final LiveCodeMsg NO_PERMISSION = new LiveCodeMsg(2004, "无权操作此直播间");
    public static final LiveCodeMsg STREAM_KEY_INVALID = new LiveCodeMsg(2005, "推流密钥无效");
    public static final LiveCodeMsg PLEASE_LOGIN = new LiveCodeMsg(2006, "请先登录");
}
