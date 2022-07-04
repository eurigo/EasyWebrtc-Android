package com.eurigo.easyrtclib.observer;

import com.blankj.utilcode.util.LogUtils;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * @author Eurigo
 * Created on 2022/6/14 10:03
 * desc   : 会话描述协议SDP
 */
public class EasyRtcSdpObserver implements SdpObserver {

    private static final String TAG = "EasyRtc.SdpObserver";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        LogUtils.dTag(TAG, "sdpCreateSuccess--------------------------------------------");
    }

    @Override
    public void onSetSuccess() {
        LogUtils.dTag(TAG, "sdpSetSuccess--------------------------------------------");
    }

    @Override
    public void onCreateFailure(String s) {
        LogUtils.dTag(TAG, "sdpCreateFailure--------------------------------------------" + s);
    }

    @Override
    public void onSetFailure(String s) {
        LogUtils.dTag(TAG, "sdpSetFailure--------------------------------------------" + s);
    }
}
