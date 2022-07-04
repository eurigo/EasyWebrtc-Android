package com.eurigo.easyrtclib;

import android.content.Context;
import android.media.MediaRecorder;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;

import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

/**
 * @author Eurigo
 * Created on 2022/7/1 14:52
 * desc   :
 */
public class AudioRecorderRenderer implements JavaAudioDeviceModule.SamplesReadyCallback, JavaAudioDeviceModule.AudioRecordErrorCallback
        , JavaAudioDeviceModule.AudioTrackErrorCallback {

    private static final String TAG = "EasyRtc.AudioRecorder";
    private byte[] audio;
    private boolean isRecording = false;

    public AudioRecorderRenderer() {
    }

    public static AudioRecorderRenderer getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private final static AudioRecorderRenderer INSTANCE = new AudioRecorderRenderer();
    }

    private AudioDeviceModule audioDeviceModule;
    private String audioFilePath;

    public void releaseAudioDevice() {
        audioDeviceModule.release();
    }

    protected void startRecording(String audioFilePath) {
        this.audioFilePath = audioFilePath;
        audio = ArrayUtils.newByteArray();
        isRecording = true;
    }

    protected void stopRecording() {
        isRecording = false;
        FileIOUtils.writeFileFromBytesByStream(audioFilePath, audio);
    }

    public AudioDeviceModule createJavaAudioDevice(Context context) {
        if (audioDeviceModule == null) {
            audioDeviceModule = JavaAudioDeviceModule.builder(context)
                    .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    // 默认音频源是适合 VoIP 会话的语音通信。您可以更改为您想要的音频源。
                    .setSamplesReadyCallback(this)
                    .setAudioRecordErrorCallback(this)
                    .setAudioTrackErrorCallback(this)
                    .createAudioDeviceModule();
        }
        return audioDeviceModule;
    }

    @Override
    public void onWebRtcAudioRecordSamplesReady(JavaAudioDeviceModule.AudioSamples audioSamples) {
        if (isRecording) {
            audio = ArrayUtils.add(audio, audioSamples.getData());
        }
    }

    @Override
    public void onWebRtcAudioRecordInitError(String s) {
        LogUtils.eTag(TAG, "WebRtc 音频记录初始化错误", s);
    }

    @Override
    public void onWebRtcAudioRecordStartError(JavaAudioDeviceModule.AudioRecordStartErrorCode code, String s) {
        LogUtils.eTag(TAG, "WebRtc 录音开始错误", "code:" + code, s);
    }

    @Override
    public void onWebRtcAudioRecordError(String s) {
        LogUtils.eTag(TAG, "WebRtc 音频记录错误", s);
    }

    @Override
    public void onWebRtcAudioTrackInitError(String s) {
        LogUtils.eTag(TAG, "WebRtc 音轨初始化错误", s);

    }

    @Override
    public void onWebRtcAudioTrackStartError(JavaAudioDeviceModule.AudioTrackStartErrorCode audioTrackStartErrorCode, String s) {
        LogUtils.eTag(TAG, "WebRtc 音轨开始错误", s);
    }

    @Override
    public void onWebRtcAudioTrackError(String s) {
        LogUtils.eTag(TAG, "WebRtc 音轨错误", s);
    }
}
