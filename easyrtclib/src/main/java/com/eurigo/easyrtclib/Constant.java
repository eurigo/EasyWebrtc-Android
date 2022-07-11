package com.eurigo.easyrtclib;

import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.TimeUtils;

/**
 * @author Eurigo
 * Created on 2022/6/09 17:45
 * desc   :
 */
public class Constant {
    /**
     * 默认STUN服务器地址
     */
    public static final String STUN = "stun:stun.l.google.com:19302";

    public static final String CHANNEL = "channel";

    public static final int VIDEO_RESOLUTION_WIDTH = 480;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int VIDEO_FPS = 30;
    /**
     * 声音调节
     */
    public static final int VOLUME = 3;

    public static final String VIDEO_TRACK_ID = "0";
    public static final String AUDIO_TRACK_ID = "-1";

    public static final String LOCAL_VIDEO_STREAM = "localVideoStream";
    public static final String LOCAL_AUDIO_STREAM = "localAudioStream";

    public static final String LOCAL_VIDEO_PATH = PathUtils.getAppDataPathExternalFirst() + "/local_" + TimeUtils.getNowString() + ".mp4";
    public static final String REMOTE_VIDEO_PATH = PathUtils.getAppDataPathExternalFirst() + "/remote_" + TimeUtils.getNowString() + ".mp4";
}
