package com.eurigo.easywebrtc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.eurigo.easyrtclib.Constant;
import com.eurigo.easyrtclib.EasyRtc;
import com.eurigo.easyrtclib.EasyRtcCallBack;
import com.eurigo.easywebrtc.databinding.ActivityMainBinding;
import com.eurigo.websocketlib.DisConnectReason;
import com.eurigo.websocketlib.IWsListener;
import com.eurigo.websocketlib.WsClient;
import com.eurigo.websocketlib.WsManager;

import org.java_websocket.framing.Framedata;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

/**
 * @author Eurigo
 * Created on 2022/6/09 17:45
 * desc   :
 */
public class MainActivity extends AppCompatActivity implements EasyRtcCallBack, IWsListener {

    private static final String WS_URL = "ws://aa787e.natappfree.cc/use/" + DeviceUtils.getAndroidID();
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        // 创建WsClient
        WsClient wsClient = new WsClient.Builder()
                .setServerUrl(WS_URL)
                .setListener(this)
                .setPingInterval(15)
                .build();
//        WsManager.getInstance().init(wsClient).start();
        startWebRtc();
        mBinding.btnWebrtcConnect.setOnClickListener(v -> {
            checkPermission();
            EasyRtc.createOffer();
        });
        mBinding.btnWebrtcSwitchCamera.setOnClickListener(v ->
                EasyRtc.switchCamera()
        );
    }

    private void startWebRtc() {
        checkPermission();
        EasyRtc.create(Constant.STUN, this);
        EasyRtc.setLocalView(mBinding.localVideoView);
        EasyRtc.setRemoteView(mBinding.remoteVideoView);
        EasyRtc.startLocalVideo();
    }

    /**
     * 检查权限并申请
     */
    private void checkPermission() {
        PermissionUtils.permission(PermissionConstants.CAMERA
                        , PermissionConstants.STORAGE
                        , PermissionConstants.MICROPHONE)
                .request();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EasyRtc.getRemoteView().clearImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EasyRtc.release();
    }

    @Override
    public void onSendIce(IceCandidate iceCandidate) {
        WsData wsData = new WsData(EventType.SEND_ICE, DeviceUtils.getAndroidID(), "");
        wsData.setIceCandidate(iceCandidate);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onSendOffer(SessionDescription sessionDescription) {
        WsData wsData = new WsData(EventType.SEND_OFFER, DeviceUtils.getAndroidID(), "");
        wsData.setSessionDescription(sessionDescription);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onSendAnswer(SessionDescription sessionDescription) {
        WsData wsData = new WsData(EventType.SEND_ANSWER, DeviceUtils.getAndroidID(), "");
        wsData.setSessionDescription(sessionDescription);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onConnectStateChange(PeerConnection.PeerConnectionState newState) {
        LogUtils.eTag("State", newState.name());
    }

    @Override
    public void onRtcConnected() {
        ThreadUtils.runOnUiThread(() -> {
            mBinding.btnWebrtcConnect.setEnabled(false);
            mBinding.btnWebrtcConnect.setText("已连接");
        });
    }

    @Override
    public void onRtcDisconnect() {
        ThreadUtils.runOnUiThread(() -> {
            mBinding.btnWebrtcConnect.setEnabled(true);
            mBinding.btnWebrtcConnect.setText("连接");
        });
    }

    @Override
    public void onRtcConnectFailed() {
        EasyRtcCallBack.super.onRtcConnectFailed();
        ToastUtils.showShort("连接失败");
        finish();
    }

    @Override
    public void onConnected(WsClient client) {
        ToastUtils.showShort("连接成功");
    }

    @Override
    public void onDisconnect(WsClient client, DisConnectReason reason) {
        ToastUtils.showShort("连接断开");
    }

    @Override
    public void onMessage(WsClient client, String message) {
        // 收到服务器的广播消息
        // 1、解析消息
        WsData wsData = GsonUtils.fromJson(message, WsData.class);
        switch (wsData.getType()) {
            // 2、如果不是自己发的offer，则设置为远程sdp
            // 3、EasyRtcCallBack.onSendAnswer发送answer给服务器
            case SEND_OFFER:
                // 4、如果不是自己发的Answer，则设置为远程sdp
            case SEND_ANSWER:
                // offer和answer都是sdp, 他是相对于发送方和接收方的
                if (!wsData.getFrom().equals(DeviceUtils.getAndroidID())) {
                    EasyRtc.setRemoteSdp(wsData.getSessionDescription());
                    EasyRtc.createAnswer();
                }
                break;
            case SEND_ICE:
                if (!wsData.getFrom().equals(DeviceUtils.getAndroidID())) {
                    EasyRtc.setRemoteIce(wsData.getIceCandidate());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPing(WsClient wsClient, Framedata frameData) {
    }

    @Override
    public void onPong(WsClient client, Framedata frameData) {
    }

    @Override
    public void onSendMessage(WsClient client, String message) {
    }
}