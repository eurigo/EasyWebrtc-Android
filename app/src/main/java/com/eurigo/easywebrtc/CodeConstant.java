package com.eurigo.easywebrtc;

import com.blankj.utilcode.util.PathUtils;

/**
 * @author Eurigo
 * Created on 2022/6/29 16:01
 * desc   :
 */
public class CodeConstant {

    // 注册
    public static final int REGISTER = 0;
    // 注册成功
    public static final int REGISTER_SUCCESS = 1;
    // 是否在线
    public static final int IS_ONLINE = 2;
    // 必须重连
    public static final int MUST_RECONNECT = 5;
    public static final int NOT_ONLINE = 6;
    // 发送ice
    public static final int ICE = 103;
    // 发送sdp
    public static final int SDP = 104;
    // 切换摄像头
    public static final int SWITCH_CAMERA = 105;
    // 滤镜
    public static final int FILTER = 106;
    // 美颜
    public static final int BEAUTY = 107;

    public static final String LOCAL_VIDEO_PATH = PathUtils.getExternalDownloadsPath() + "/local_";
    public static final String REMOTE_VIDEO_PATH = PathUtils.getExternalDownloadsPath() + "/remote_";

}
