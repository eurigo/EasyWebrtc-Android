package com.eurigo.easywebrtc;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * @author Eurigo
 * Created on 2022/6/14 15:56
 * desc   :
 */
public class WsData {

    private SessionDescription sessionDescription;
    private IceCandidate iceCandidate;
    private String phone;
    private int code;
    private String message;
    private String data;
    private boolean isReceived;

    public WsData(String phone, int code, String message) {
        this.phone = phone;
        this.code = code;
        this.message = message;
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public IceCandidate getIceCandidate() {
        return iceCandidate;
    }

    public void setIceCandidate(IceCandidate iceCandidate) {
        this.iceCandidate = iceCandidate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isReceived() {
        return isReceived;
    }
}
