package com.eurigo.easyrtclib;

import com.blankj.utilcode.util.LogUtils;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

/**
 * @author Eurigo
 * Created on 2022/6/10 17:07
 * desc   :
 */
public class PeerConnectionObserver implements PeerConnection.Observer {

    private DateChannelObserver channelObserver;

    private static final String TAG = "EasyRtc.PeerConnectionObserver";

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        channelObserver.onSignalingChange(signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        LogUtils.dTag(TAG, "onIceConnectionReceivingChange : " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        LogUtils.dTag(TAG, "onIceGatheringChange : " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        LogUtils.dTag(TAG, "onIceCandidatesRemoved : " + iceCandidates.toString());
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        dataChannel.registerObserver(channelObserver);
    }

    @Override
    public void onRenegotiationNeeded() {
        LogUtils.dTag(TAG, "onRenegotiationNeeded : ");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        LogUtils.dTag(TAG, "onAddTrack : " + rtpReceiver);
    }

    public void setObserver(DateChannelObserver channelObserver) {
        this.channelObserver = channelObserver;
    }
}
