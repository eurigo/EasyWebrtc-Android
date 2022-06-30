package com.eurigo.easyrtclib;

import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.util.List;

/**
 * @author Eurigo
 * Created on 2022/6/14 10:03
 * desc   :
 */
public interface EasyRtcCallBack {

    /**
     * ice创建后的回调，通常需要转发服务器
     *
     * @param iceCandidate ice candidate
     */
    void onSendIce(IceCandidate iceCandidate);

    /**
     * 媒体流处理回调
     *
     * @param mediaStream 媒体流
     * @Demo List<VideoTrack> videoTracks = mediaStream.videoTracks;
     * if (videoTracks != null && videoTracks.size() > 0) {
     * VideoTrack videoTrack = videoTracks.get(0);
     * if (videoTrack != null && mRemoteView != null)
     * videoTrack.addSink(mRemoteView);
     * }
     * }
     * List<AudioTrack> audioTracks = mediaStream.audioTracks;
     * if (audioTracks != null && audioTracks.size() > 0) {
     * AudioTrack audioTrack = audioTracks.get(0);
     * if (audioTrack != null) {
     * audioTrack.setVolume(Constant.VOLUME);
     * }
     * }
     */
    default void onAddStream(MediaStream mediaStream) {
        List<VideoTrack> videoTracks = mediaStream.videoTracks;
        if (videoTracks != null && videoTracks.size() > 0) {
            VideoTrack videoTrack = videoTracks.get(0);
            EasyRtc.setRemoteVideoTrack(videoTrack);
            if (videoTrack != null && EasyRtc.getRemoteView() != null) {
                videoTrack.addSink(EasyRtc.getRemoteView());
            }
        }
        List<AudioTrack> audioTracks = mediaStream.audioTracks;
        if (audioTracks != null && audioTracks.size() > 0) {
            AudioTrack audioTrack = audioTracks.get(0);
            if (audioTrack != null) {
                audioTrack.setVolume(Constant.VOLUME);
            }
        }
    }

    /**
     * 频道已创建
     *
     * @param dataChannel 频道
     */
    default void onDataChannelCreate(DataChannel dataChannel) {
    }

    /**
     * 缓冲刷新
     *
     * @param l 缓冲大小
     */
    default void onChannelBufferedAmountChange(long l) {
    }

    /**
     * 频道状态发生变化
     */
    default void onChannelStateChange() {
    }

    /**
     * 频道收到消息
     *
     * @param buffer 消息
     */
    default void onChannelMessage(DataChannel.Buffer buffer) {
    }

    /**
     * 心灵状态发生变化
     *
     * @param signalingState 信令状态
     */
    default void onSignalingChange(PeerConnection.SignalingState signalingState) {
    }

    /**
     * 发送 offer sdp
     *
     * @param sessionDescription sdp
     */
    void onSendOffer(SessionDescription sessionDescription);

    /**
     * 发送 answer sdp
     *
     * @param sessionDescription sdp
     */
    void onSendAnswer(SessionDescription sessionDescription);

    /**
     * 再次应答 answer sdp
     *
     * @param sessionDescription sdp
     */
    default void onReAnswer(SessionDescription sessionDescription) {
    }

    /**
     * 连接状态发生改变
     *
     * @param newState 状态
     */
    default void onConnectStateChange(PeerConnection.PeerConnectionState newState) {

    }

    /**
     * 连接成功
     */
    void onRtcConnected();

    /**
     * 连接断开
     */
    void onRtcDisconnect();

    /**
     * 连接错误
     */
    default void onRtcConnectFailed() {
    }

}
