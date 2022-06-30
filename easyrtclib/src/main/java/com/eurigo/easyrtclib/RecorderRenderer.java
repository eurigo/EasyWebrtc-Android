package com.eurigo.easyrtclib;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import org.webrtc.EglBase;
import org.webrtc.GlRectDrawer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoFrameDrawer;
import org.webrtc.VideoSink;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * @author Eurigo
 * Created on 2022/6/30 16:35
 * desc   : 视频录制器
 */
public class RecorderRenderer implements VideoSink {
    private static final String TAG = "FileRenderer";
    private final HandlerThread renderThread;
    private final Handler renderThreadHandler;
    private int outputFileWidth = -1;
    private int outputFileHeight = -1;
    private ByteBuffer[] encoderOutputBuffers;
    private EglBase eglBase;
    private EglBase.Context sharedContext;
    private VideoFrameDrawer frameDrawer;
    // H.264 Advanced Video Coding
    private static final String MIME_TYPE = "video/avc";
    // 30fps
    private static final int FRAME_RATE = 30;
    // 5 seconds between I-frames
    private static final int IFRAME_INTERVAL = 5;
    private MediaMuxer mediaMuxer;
    private MediaCodec encoder;
    private MediaCodec.BufferInfo bufferInfo;
    private int trackIndex = -1;
    private boolean isRunning = true;
    private GlRectDrawer drawer;
    private Surface surface;

    RecorderRenderer(String outputFile, final EglBase.Context sharedContext) throws IOException {
        renderThread = new HandlerThread(TAG + "RenderThread");
        renderThread.start();
        renderThreadHandler = new Handler(renderThread.getLooper());
        bufferInfo = new MediaCodec.BufferInfo();
        this.sharedContext = sharedContext;

        mediaMuxer = new MediaMuxer(outputFile,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    private void initVideoEncoder() {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, 480, 720);

        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        try {
            encoder = MediaCodec.createEncoderByType(MIME_TYPE);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            renderThreadHandler.post(() -> {
                eglBase = EglBase.create(sharedContext, EglBase.CONFIG_RECORDABLE);
                surface = encoder.createInputSurface();
                eglBase.createSurface(surface);
                eglBase.makeCurrent();
                drawer = new GlRectDrawer();
            });
        } catch (Exception e) {
            Log.wtf(TAG, e);
        }
    }

    @Override
    public void onFrame(VideoFrame frame) {
        frame.retain();
        if (outputFileWidth == -1) {
            outputFileWidth = frame.getRotatedWidth();
            outputFileHeight = frame.getRotatedHeight();
            initVideoEncoder();
        }
        renderThreadHandler.post(() -> renderFrameOnRenderThread(frame));
    }

    private void renderFrameOnRenderThread(VideoFrame frame) {
        if (frameDrawer == null) {
            frameDrawer = new VideoFrameDrawer();
        }
        frameDrawer.drawFrame(frame, drawer, null, 0, 0, outputFileWidth, outputFileHeight);
        frame.release();
        drainEncoder();
        eglBase.swapBuffers();
    }

    /**
     * Release all resources. All already posted frames will be rendered first.
     */
    void release() {
        isRunning = false;
        renderThreadHandler.post(() -> {
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }
            eglBase.release();
            mediaMuxer.stop();
            mediaMuxer.release();
            renderThread.quit();
        });
    }

    private boolean encoderStarted = false;
    private volatile boolean muxerStarted = false;
    private long videoFrameStart = 0;

    private void drainEncoder() {
        if (!encoderStarted) {
            encoder.start();
            encoderOutputBuffers = encoder.getOutputBuffers();
            encoderStarted = true;
            return;
        }
        while (true) {
            int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, 10000);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.getOutputBuffers();
                Log.e(TAG, "encoder output buffers changed");
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // not expected for an encoder
                MediaFormat newFormat = encoder.getOutputFormat();

                Log.e(TAG, "encoder output format changed: " + newFormat);
                trackIndex = mediaMuxer.addTrack(newFormat);
                if (!muxerStarted) {
                    mediaMuxer.start();
                    muxerStarted = true;
                }
                if (!muxerStarted) {
                    break;
                }
            } else if (encoderStatus < 0) {
                Log.e(TAG, "unexpected result fr om encoder.dequeueOutputBuffer: " + encoderStatus);
            } else { // encoderStatus >= 0
                try {
                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        Log.e(TAG, "encoderOutputBuffer " + encoderStatus + " was null");
                        break;
                    }
                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    if (videoFrameStart == 0 && bufferInfo.presentationTimeUs != 0) {
                        videoFrameStart = bufferInfo.presentationTimeUs;
                    }
                    bufferInfo.presentationTimeUs -= videoFrameStart;
                    if (muxerStarted) {
                        mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    }
                    isRunning = isRunning && (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == 0;
                    encoder.releaseOutputBuffer(encoderStatus, false);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                } catch (Exception e) {
                    Log.wtf(TAG, e);
                    break;
                }
            }
        }
    }

    private long presTime = 0L;

}
