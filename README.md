&emsp;&emsp;<a href="#2">集成</a>  
&emsp;&emsp;<a href="#3">启动本地视频</a>  
&emsp;&emsp;<a href="#4">建立连接</a>  
&emsp;&emsp;<a href="#5">销毁</a>  
&emsp;&emsp;<a href="#6">关于几个重要回调说明</a>  
&emsp;&emsp;<a href="#7">其他问题</a>  
# EasyWebrtc

### Android的WebRtc实现

> 只需要几行带代码就可以视频通话

### <a name="2">集成</a><a style="float:right;text-decoration:none;" href="#index"></a>

+ 在项目级 `build.gradle`添加

```groovy
allprojects {
   repositories {
      maven { url 'https://jitpack.io' }
	}
}
```

+ 在app模块下的`build.gradle`文件中加入，Tag[![](https://jitpack.io/v/eurigo/EasyWebrtc.svg)](https://jitpack.io/#eurigo/EasyWebrtc)
```groovy
dependencies {
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
### <a name="3">启动本地视频</a><a style="float:right;text-decoration:none;" href="#index"></a>
```java
// 配置STUN服务器地址，设置回调	
EasyRtc.create(Constant.STUN, this);
EasyRtc.setLocalView(localVideoView);
EasyRtc.setRemoteView(remoteVideoView);
// 开启本地视频
EasyRtc.startLocalVideo();
```
### <a name="4">建立连接</a><a style="float:right;text-decoration:none;" href="#index"></a>
```java
// 配置STUN服务器地址，设置回调	
EasyRtc.createOffer();
```
### <a name="5">销毁</a><a style="float:right;text-decoration:none;" href="#index"></a>
```
// 在onDestory时release
EasyRtc.release();
```
### <a name="6">关于几个重要回调说明</a><a style="float:right;text-decoration:none;" href="#index"></a>

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

### <a name="7">其他问题</a><a style="float:right;text-decoration:none;" href="#index"></a>
  参考MainActivity
