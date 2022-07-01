<a href="#2">集成</a>  
<a href="#3">开启摄像头</a>  
<a href="#4">建立连接</a>  
<a href="#4-1">视频录制</a>  
<a href="#5">销毁</a>  
<a href="#6">关于几个重要回调说明</a>  
<a href="#7">其他问题</a>  

# EasyWebrtc-Android[![](https://jitpack.io/v/eurigo/EasyWebrtc.svg)](https://jitpack.io/#eurigo/EasyWebrtc)

### Android的WebRtc实现

> 只需要几行带代码就可以视频通话

### <a name="2">集成</a>

+ 在项目级 `build.gradle`添加

```groovy
allprojects {
   repositories {
      maven { url 'https://jitpack.io' }
	}
}
```

+ 在app模块下的`build.gradle`文件中加入
```groovy
dependencies {
    // 请用最后release版本替换Tag
    implementation 'com.github.eurigo:EasyWebrtc:Tag'
}
```

+ 在app模块下的AndroidManifest.xml添加权限
```xml
<manifest
    ...
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
/>
```
### <a name="3">开启摄像头</a>
```java
// 配置STUN服务器地址，设置回调	
EasyRtc.create(Constant.STUN, new EasyRtcCallBack);
EasyRtc.setLocalView(localVideoView);
EasyRtc.setRemoteView(remoteVideoView);
// 开启本地视频
EasyRtc.startLocalVideo();
```
### <a name="4">建立连接</a>
```java
// 配置STUN服务器地址，设置回调	
EasyRtc.createOffer();
```
### <a name="4-1">视频录制</a>
> 视频录制分为录制远程和录制本地，start和stop需成对出现
```java
// 开启录制本地源 	
EasyRtc.startRecorderLocal(String savePath);
EasyRtc.stopRecorderLocal();
// 开启录制远程源
EasyRtc.startRecorderRemote(String savePath);
EasyRtc.stopRecorderRemote();
```
### <a name="5">销毁</a>
```
// 在onDestory时release
EasyRtc.release();
```
### <a name="6">关于几个重要回调说明</a>

```java
	/**
     * 发送 offer sdp
     * @param sessionDescription sdp
     * 在创建offer之后需要把sdp发送给对方，可以使用webSocket、http等都可以
     * 被呼叫方收到offer之后需要EasyRtc.setRemoteSdp(SessionDescription sessionDescription);
     */
    @Override
    public void onSendOffer(SessionDescription sessionDescription) {
        WsData wsData = new WsData(EventType.SEND_OFFER, DeviceUtils.getAndroidID(), "");
        wsData.setSessionDescription(sessionDescription);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    /**
     * 发送 answer sdp
     * @param sessionDescription sdp
     * 本质上也是sdp的发送
     * 被呼叫方收到Answer之后需要EasyRtc.setRemoteSdp(SessionDescription sessionDescription);
     */
    @Override
    public void onSendAnswer(SessionDescription sessionDescription) {
        WsData wsData = new WsData(EventType.SEND_ANSWER, DeviceUtils.getAndroidID(), "");
        wsData.setSessionDescription(sessionDescription);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    /**
     * 发送 Ice
     * 类似sdp,需要发STUN返回的Ice发送给远端
     * 被呼叫方收到Ice之后需要EasyRtc.setRemoteIce(IceCandidate iceCandidate);
     */		
    @Override
    public void onSendIce(IceCandidate iceCandidate) {
        WsData wsData = new WsData(EventType.SEND_ICE, DeviceUtils.getAndroidID(), "");
        wsData.setIceCandidate(iceCandidate);
        WsManager.getInstance().send(GsonUtils.toJson(wsData));
    }

    @Override
    public void onConnectStateChange(PeerConnection.PeerConnectionState newState) {
        
    }

    @Override
    public void onRtcConnected() {
        // ice已连接
    }

    @Override
    public void onRtcDisconnect() {
        // ice已断开
    }

    @Override
    public void onRtcConnectFailed() {
        // ice连接失败，通常fail发生在连接断开之后
    }

    @Override
    public void onChannelMessage(DataChannel.Buffer buffer) {
        // P2P Channel接收到消息
    }
```

### <a name="7">其他问题</a>
  参考MainActivity
