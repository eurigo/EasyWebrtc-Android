package com.eurigo.easywebrtc;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
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
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.List;

/**
 * @author Eurigo
 * Created on 2022/6/09 17:45
 * desc   :
 */
public class MainActivity extends AppCompatActivity implements EasyRtcCallBack, IWsListener {

    private static final String WS_URL = "ws://a3tqj6.natappfree.cc/myWs/" + DeviceUtils.getAndroidID();
    private static final String VIDEO_PATH = PathUtils.getExternalDownloadsPath()+ "/"+TimeUtils.getNowString()+".mp4";
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
                .setPingInterval(60)
                .build();
        WsManager.getInstance().init(wsClient).start();
        startWebRtc();
        mBinding.btnWebrtcConnect.setOnClickListener(v -> {
            createOffer();
        });
        mBinding.remoteVideoView.setOnClickListener(v -> {
            EasyRtc.startRecorderRemote(VIDEO_PATH);
        });
        mBinding.btnWebrtcSwitchCamera.setOnClickListener(v -> {
            EasyRtc.stopRecorderRemote();
        });
    }

    private void startWebRtc() {
        PermissionUtils.permission(PermissionConstants.CAMERA
                        , PermissionConstants.STORAGE
                        , PermissionConstants.MICROPHONE)
                .callback(new PermissionUtils.SingleCallback() {
                    @Override
                    public void callback(boolean isAllGranted, @NonNull List<String> granted, @NonNull List<String> deniedForever, @NonNull List<String> denied) {
                        if (isAllGranted) {
                            EasyRtc.create(Constant.STUN, (MainActivity) ActivityUtils.getTopActivity());
                            EasyRtc.setLocalView(mBinding.localVideoView);
                            EasyRtc.setRemoteView(mBinding.remoteVideoView);
                            EasyRtc.startLocalVideo();
                        }
                    }
                })
                .request();
    }

    /**
     * 检查权限并申请
     */
    private void createOffer() {
        PermissionUtils.permission(PermissionConstants.CAMERA
                        , PermissionConstants.STORAGE
                        , PermissionConstants.MICROPHONE)
                .callback(new PermissionUtils.SingleCallback() {
                    @Override
                    public void callback(boolean isAllGranted, @NonNull List<String> granted, @NonNull List<String> deniedForever, @NonNull List<String> denied) {
                        if (isAllGranted) {
                            EasyRtc.createOffer();
                        }
                    }
                })
                .request();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EasyRtc.getPeerConnection() != null) {
            EasyRtc.getRemoteView().clearImage();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EasyRtc.release();
        WsManager.getInstance().destroy();
    }

    @Override
    public void onSendIce(IceCandidate iceCandidate) {
        WsData wsData = new WsData("13888886666", CodeConstant.ICE, "ice");
        wsData.setIceCandidate(iceCandidate);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onSendOffer(SessionDescription sessionDescription) {
        WsData wsData = new WsData("13888886666", CodeConstant.SDP, "sdp");
        wsData.setSessionDescription(sessionDescription);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onSendAnswer(SessionDescription sessionDescription) {
        WsData wsData = new WsData("13888886666", CodeConstant.SDP, "sdp");
        wsData.setSessionDescription(sessionDescription);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onConnectStateChange(PeerConnection.PeerConnectionState newState) {
        LogUtils.eTag("xxx", newState.name());
    }

    @Override
    public void onRtcConnected() {
        ThreadUtils.runOnUiThread(() -> {
            mBinding.btnWebrtcConnect.setEnabled(false);
            mBinding.btnWebrtcConnect.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.getColor(R.color.green)));
        });
    }

    @Override
    public void onRtcDisconnect() {
        ThreadUtils.runOnUiThread(() -> {
            mBinding.btnWebrtcConnect.setEnabled(true);
            mBinding.btnWebrtcConnect.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.getColor(R.color.red)));
        });
    }

    @Override
    public void onRtcConnectFailed() {
        EasyRtcCallBack.super.onRtcConnectFailed();
        ThreadUtils.runOnUiThread(() -> {
            ToastUtils.showShort("Rtc连接失败");
            mBinding.btnWebrtcConnect.setEnabled(true);
        });

    }

    @Override
    public void onConnected(WsClient client) {
        // 连接成功发送注册事件
        WsData wsData = new WsData("13888886666", CodeConstant.REGISTER, "register");
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onDisconnect(WsClient client, DisConnectReason reason) {
        LogUtils.eTag("xxx", "WS连接断开");
    }

    @Override
    public void onMessage(WsClient client, String message) {
        LogUtils.eTag("xxx", message);
        // 1、解析消息
        WsData wsData = GsonUtils.fromJson(message, WsData.class);
        switch (wsData.getCode()) {
            case CodeConstant.REGISTER_SUCCESS:
            case CodeConstant.NOT_ONLINE:
            case CodeConstant.MUST_RECONNECT:
                ToastUtils.showShort(wsData.getMessage());
                break;
            case CodeConstant.BEAUTY:
            case CodeConstant.SDP:
                if (wsData.isReceived()){
                    EasyRtc.setRemoteSdp(wsData.getSessionDescription());
                    EasyRtc.createAnswer();
                }
                break;
            case CodeConstant.ICE:
                if (wsData.isReceived()) {
                    EasyRtc.setRemoteIce(wsData.getIceCandidate());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPing(WsClient wsClient, Framedata frameData) {
        LogUtils.eTag("xxx", "onPing");
    }

    @Override
    public void onPong(WsClient client, Framedata frameData) {
        LogUtils.eTag("xxx", "onPong");
    }

    @Override
    public void onSendMessage(WsClient client, String message) {
    }
}