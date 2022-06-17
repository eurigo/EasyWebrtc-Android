package com.eurigo.easyrtclib;

import org.webrtc.DataChannel;
import org.webrtc.PeerConnection;

/**
 * @author Eurigo
 * Created on 2022/6/10 17:07
 * desc   :
 */
public class DateChannelObserver implements DataChannel.Observer {

    private final EasyRtcCallBack callBack;

    public DateChannelObserver(EasyRtcCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onBufferedAmountChange(long l) {
        callBack.onChannelBufferedAmountChange(l);
    }

    @Override
    public void onStateChange() {
        callBack.onChannelStateChange();
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        callBack.onChannelMessage(buffer);
    }

    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        callBack.onSignalingChange(signalingState);
    }
}
