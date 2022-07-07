package com.eurigo.easyrtclib;

import static com.eurigo.easyrtclib.Constant.LOCAL_VIDEO_PATH;
import static com.eurigo.easyrtclib.Constant.REMOTE_VIDEO_PATH;

import android.content.Context;
import android.media.MediaRecorder;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.eurigo.easyrtclib.observer.DateChannelObserver;
import com.eurigo.easyrtclib.observer.EasyRtcSdpObserver;
import com.eurigo.easyrtclib.observer.PeerConnectionObserver;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eurigo
 * Created on 2022/6/10 17:07
 * desc   :
 */
public class EasyRtc {
    public static final String TAG = "EasyRtc";

    private static EglBase.Context eglBaseContext;
    private static PeerConnectionFactory peerConnectionFactory;
    private static List<PeerConnection.IceServer> iceServers;
    private static List<String> streamList;
    private static PeerConnection peerConnection;
    private static DataChannel channel;

    private static SurfaceViewRenderer mLocalView;
    private static SurfaceViewRenderer mRemoteView;

    private static EasyRtcCallBack mCallBack;

    private static CameraVideoCapturer mVideoCapturer;

    private static VideoTrack localVideoTrack;

    private static VideoTrack remoteVideoTrack;

    private static EasyRtcSdpObserver mSdpObserver;

    /**
     * 是否有开启录像功能
     */
    private static boolean enableRecord = false;
    private static VideoFileRenderer mLocalRecorder;
    private static VideoFileRenderer mRemoteRecorder;
    private static String customLocalSavePath;
    private static String customRemoteSavePath;

    public static void setCustomLocalSavePath(String customLocalSavePath) {
        EasyRtc.customLocalSavePath = customLocalSavePath;
    }

    public static String getLocalSavePath() {
        return TextUtils.isEmpty(customLocalSavePath) ? LOCAL_VIDEO_PATH : customLocalSavePath;
    }

    public static void setCustomRemoteSavePath(String customRemoteSavePath) {
        EasyRtc.customRemoteSavePath = customRemoteSavePath;
    }

    public static String getRemoteSavePath() {
        return TextUtils.isEmpty(customRemoteSavePath) ? REMOTE_VIDEO_PATH : customRemoteSavePath;
    }

    public static File getLocalRecorderFile() {
        return FileUtils.getFileByPath(getLocalSavePath());
    }

    public static File getRemoteRecorderFile() {
        return FileUtils.getFileByPath(getRemoteSavePath());
    }

    /**
     * 本地录制是否开启
     */
    private static boolean isRecordingLocal = false;

    /**
     * 远程录制是否开启
     */
    private static boolean isRecordingRemote = false;

    public static boolean isIsRecordingLocal() {
        return isRecordingLocal;
    }

    public static boolean isIsRecordingRemote() {
        return isRecordingRemote && remoteVideoTrack != null;
    }

    public static void setRemoteVideoTrack(VideoTrack remoteVideoTrack) {
        EasyRtc.remoteVideoTrack = remoteVideoTrack;
    }

    public static SurfaceViewRenderer getRemoteView() {
        return mRemoteView;
    }

    public static PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public static DataChannel getChannel() {
        return channel;
    }

    public static void switchCamera() {
        if (mVideoCapturer != null) {
            mVideoCapturer.switchCamera(null);
        }
    }

    public static void switchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        if (mVideoCapturer != null) {
            mVideoCapturer.switchCamera(cameraSwitchHandler);
        }
    }

    /**
     * 录制本地视频，必须调用stopRecorder(), 否则视频黑屏
     */
    public static void startRecorderLocal() {
        if (!enableRecord) {
            ToastUtils.showShort("请在初始化时先开启录像功能");
            return;
        }
        if (isRecordingLocal) {
            ToastUtils.showShort("本地录制已开启");
            return;
        }
        try {
            FileUtils.createFileByDeleteOldFile(getLocalSavePath());
            mLocalRecorder = new VideoFileRenderer(getLocalSavePath(), eglBaseContext, true);
            localVideoTrack.addSink(mLocalRecorder);
            isRecordingLocal = true;
        } catch (IOException e) {
            e.printStackTrace();
            isRecordingLocal = false;
        }

    }

    /**
     * 停止录制，
     */
    public static void stopRecorderLocal() {
        if (mLocalRecorder != null) {
            mLocalRecorder.release();
            localVideoTrack.removeSink(mLocalRecorder);
            mLocalRecorder = null;
            isRecordingLocal = false;
        }
    }

    /**
     * 开始录制远端视频
     * <p>
     * 远端的录制在EasyRtc.OnAddStream()
     */
    public static void startRecorderRemote() {
        if (!enableRecord) {
            ToastUtils.showShort("请在初始化时先开启录像功能");
            return;
        }
        if (isRecordingRemote) {
            ToastUtils.showShort("远程录制已开启");
            return;
        }
        if (remoteVideoTrack == null) {
            ToastUtils.showShort("远程连接未建立");
            return;
        }
        try {
            FileUtils.createFileByDeleteOldFile(getRemoteSavePath());
            mRemoteRecorder = new VideoFileRenderer(getRemoteSavePath(), eglBaseContext, true);
            remoteVideoTrack.addSink(mRemoteRecorder);
            isRecordingRemote = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制远端视频
     */
    public static void stopRecorderRemote() {
        if (mRemoteRecorder != null && isRecordingRemote) {
            remoteVideoTrack.removeSink(mLocalRecorder);
            mRemoteRecorder.release();
            mRemoteRecorder = null;
            isRecordingRemote = false;
        }
    }

    public static void create(String stunServer, EasyRtcCallBack callBack) {
        create(stunServer, callBack, false);
    }

    /**
     * 初始化
     *
     * @param stunServer      stun服务器地址
     * @param easyRtcCallBack 回调
     * @param enableRecord    是否开启录像功能
     */
    public static void create(String stunServer, EasyRtcCallBack easyRtcCallBack, boolean enableRecord) {
        mCallBack = easyRtcCallBack;
        EasyRtc.enableRecord = enableRecord;
        // 初始化 PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory
                .InitializationOptions.builder(Utils.getApp())
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = true;
        options.disableNetworkMonitor = true;
        eglBaseContext = EglBase.create().getEglBaseContext();
        // 初始化 PeerConnectionFactory，设置是否开启录像功能
        initPeerConnectionFactory(options);
        // 配置STUN穿透服务器  转发服务器
        iceServers = new ArrayList<>();
        PeerConnection.IceServer iceServer = PeerConnection.IceServer.builder(stunServer).createIceServer();
        iceServers.add(iceServer);
        streamList = new ArrayList<>();
        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnectionObserver connectionObserver = getObserver();
        peerConnection = peerConnectionFactory.createPeerConnection(configuration, connectionObserver);
        // DataChannel.Init 可配参数说明：
        // ordered：是否保证顺序传输；
        // maxRetransmitTimeMs：重传允许的最长时间；
        // maxRetransmits：重传允许的最大次数；
        DataChannel.Init init = new DataChannel.Init();
        if (peerConnection != null) {
            channel = peerConnection.createDataChannel(Constant.CHANNEL, init);
            mCallBack.onDataChannelCreate(channel);
        }
        DateChannelObserver channelObserver = new DateChannelObserver(mCallBack);
        connectionObserver.setObserver(channelObserver);
    }

    /**
     * 初始化PeerConnectionFactory
     * <p>
     * 必须在初始化的时候设置是否开启录像功能
     *
     * @param options PeerConnectionFactory.Options
     */
    protected static void initPeerConnectionFactory(PeerConnectionFactory.Options options) {
        if (enableRecord) {
            peerConnectionFactory = PeerConnectionFactory.builder()
                    .setOptions(options)
                    .setAudioDeviceModule(createJavaAudioDeviceModule())
                    .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBaseContext))
                    .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBaseContext
                            , true, true))
                    .createPeerConnectionFactory();
        } else {
            peerConnectionFactory = PeerConnectionFactory.builder()
                    .setOptions(options)
                    .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBaseContext))
                    .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBaseContext
                            , true, true))
                    .createPeerConnectionFactory();
        }
    }

    /**
     * 设置本地的视频view
     *
     * @param localSurfaceView
     */
    public static void setLocalView(SurfaceViewRenderer localSurfaceView) {
        mLocalView = localSurfaceView;
        mLocalView.init(eglBaseContext, null);
        mLocalView.setMirror(true);
        mLocalView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mLocalView.setKeepScreenOn(true);
        mLocalView.setZOrderMediaOverlay(true);
        mLocalView.setEnableHardwareScaler(false);
    }

    /**
     * 开始本地视频采集
     */
    public static void startLocalVideo() {
        startLocalVideo(true, true);
    }

    /**
     * 开始本地视频采集
     *
     * @param isOpenAudio 是否开启音频
     * @param isUseFront  是否使用前置摄像头
     */
    public static void startLocalVideo(boolean isOpenAudio, boolean isUseFront) {
        VideoSource videoSource = peerConnectionFactory.createVideoSource(true);
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().getName()
                , eglBaseContext);
        mVideoCapturer = createCameraCapturer(isUseFront);
        mVideoCapturer.initialize(surfaceTextureHelper, ActivityUtils.getTopActivity(), videoSource.getCapturerObserver());
        // 宽,高,帧率
        mVideoCapturer.startCapture(Constant.VIDEO_RESOLUTION_WIDTH, Constant.VIDEO_RESOLUTION_HEIGHT, Constant.VIDEO_FPS);
        localVideoTrack = peerConnectionFactory
                .createVideoTrack(Constant.VIDEO_TRACK_ID, videoSource);
        localVideoTrack.addSink(mLocalView);
        MediaStream localMediaStream = peerConnectionFactory
                .createLocalMediaStream(Constant.LOCAL_VIDEO_STREAM);
        localMediaStream.addTrack(localVideoTrack);
        peerConnection.addTrack(localVideoTrack, streamList);
        peerConnection.addStream(localMediaStream);
        if (isOpenAudio) {
            startLocalAudioCapture();
        }
        initObserver();
    }

    public static void setRemoteView(SurfaceViewRenderer mRemoteView) {
        EasyRtc.mRemoteView = mRemoteView;
        mRemoteView.init(eglBaseContext, null);
        mRemoteView.setMirror(true);
        mRemoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mRemoteView.setKeepScreenOn(true);
        mRemoteView.setZOrderMediaOverlay(true);
        mRemoteView.setEnableHardwareScaler(false);
    }

    /**
     * 设置远端sdp
     *
     * @param sessionDescription sdp
     */
    public static void setRemoteSdp(SessionDescription sessionDescription) {
        peerConnection.setRemoteDescription(mSdpObserver, sessionDescription);
    }

    /**
     * 设置远端ice
     *
     * @param iceCandidate ice
     */
    public static void setRemoteIce(IceCandidate iceCandidate) {
        peerConnection.addIceCandidate(iceCandidate);
    }

    private static JavaAudioDeviceModule createJavaAudioDeviceModule() {
        return JavaAudioDeviceModule.builder(ActivityUtils.getTopActivity())
                .setSamplesReadyCallback(new JavaAudioDeviceModule.SamplesReadyCallback() {
                    @Override
                    public void onWebRtcAudioRecordSamplesReady(JavaAudioDeviceModule.AudioSamples audioSamples) {
                        if (mLocalRecorder != null) {
                            mLocalRecorder.onWebRtcAudioRecordSamplesReady(audioSamples);
                        }
                        if (mRemoteRecorder != null) {
                            mRemoteRecorder.onWebRtcAudioRecordSamplesReady(audioSamples);
                        }
                    }
                })
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .createAudioDeviceModule();
    }


    /**
     * 判断使用Camera1还是Camera2
     *
     * @return VideoCapturer
     */
    private static CameraVideoCapturer createCameraCapturer(boolean isUseFront) {
        Context context = ActivityUtils.getTopActivity();
        if (Camera2Enumerator.isSupported(context)) {
            return createCameraCapturer(new Camera2Enumerator(context), isUseFront);
        } else {
            return createCameraCapturer(new Camera1Enumerator(true), isUseFront);
        }
    }

    private static CameraVideoCapturer createCameraCapturer(CameraEnumerator enumerator, boolean isUseFront) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // 首先，尝试找到前置摄像头
        for (String deviceName : deviceNames) {
            if (isUseFront) {
                if (enumerator.isFrontFacing(deviceName)) {
                    LogUtils.eTag(TAG, "前置摄像头捕捉器创建成功");
                    CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                    if (videoCapturer != null) {
                        return videoCapturer;
                    }
                }
            } else {
                if (enumerator.isBackFacing(deviceName)) {
                    LogUtils.eTag(TAG, "后置摄像头捕捉器创建成功");
                    CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                    if (videoCapturer != null) {
                        return videoCapturer;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 创建本地音频
     */
    private static void startLocalAudioCapture() {
        //语音
        MediaConstraints audioConstraints = new MediaConstraints();
        //回声消除
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        //自动增益
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        //高音过滤
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        //噪音处理
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack(Constant.AUDIO_TRACK_ID, audioSource);
        MediaStream localMediaStream = peerConnectionFactory.createLocalMediaStream(Constant.LOCAL_AUDIO_STREAM);
        localMediaStream.addTrack(audioTrack);
        // 设置本地音量
        audioTrack.setVolume(Constant.VOLUME);
        peerConnection.addTrack(audioTrack, streamList);
        peerConnection.addStream(localMediaStream);
    }

    @NonNull
    private static PeerConnectionObserver getObserver() {
        return new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                mCallBack.onSendIce(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                mCallBack.onAddStream(mediaStream);
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                super.onConnectionChange(newState);
                mCallBack.onConnectStateChange(newState);
                switch (newState) {
                    case DISCONNECTED:
                        mCallBack.onRtcDisconnect();
                        break;
                    case CONNECTED:
                    case CONNECTING:
                        mCallBack.onRtcConnected();
                        break;
                    case CLOSED:
                    case FAILED:
                        mCallBack.onRtcConnectFailed();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private static void initObserver() {
        mSdpObserver = new EasyRtcSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                // 将会话描述设置在本地
                peerConnection.setLocalDescription(this, sessionDescription);
                switch (peerConnection.getLocalDescription().type) {
                    case OFFER:
                        mCallBack.onSendOffer(sessionDescription);
                        break;
                    case ANSWER:
                        mCallBack.onSendAnswer(sessionDescription);
                        break;
                    case PRANSWER:
                        mCallBack.onReAnswer(sessionDescription);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 创建offer
     */
    public static void createOffer() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferVideo", "true"));
        peerConnection.createOffer(mSdpObserver, mediaConstraints);
    }

    /**
     * 创建answer
     */
    public static void createAnswer() {
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("AnswerVideo", "true"));
        peerConnection.createAnswer(mSdpObserver, mediaConstraints);
    }

    public static void release() {

        // 断开频道连接
        if (channel != null) {
            channel.unregisterObserver();
            channel.close();
            channel.dispose();
            channel = null;
        }
        // 停止录像
        if (isRecordingLocal) {
            mLocalRecorder.release();
        }
        if (isRecordingRemote) {
            mRemoteRecorder.release();
        }
        mRemoteRecorder = null;
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }
        if (mLocalView != null) {
            mLocalView.release();
        }
        if (mRemoteView != null) {
            mRemoteView.release();
        }
        if (iceServers != null) {
            iceServers.clear();
            iceServers = null;
        }
        if (streamList != null) {
            streamList.clear();
            streamList = null;
        }
        mCallBack = null;
        enableRecord = false;
        mLocalRecorder = null;
        mRemoteRecorder = null;
        customLocalSavePath = null;
        customRemoteSavePath = null;
    }
}
