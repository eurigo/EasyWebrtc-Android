package com.eurigo.easywebrtc;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * @author Eurigo
 * Created on 2022/6/14 15:56
 * desc   :
 */
public class WsData {

    public WsData(EventType type, String from, String to){
        this.type = type;
        this.from = from;
        this.to = to;
    }

    private IceCandidate iceCandidate;
    private SessionDescription sessionDescription;
    private String from;
    private String to;
    private EventType type;

    public IceCandidate getIceCandidate() {
        return iceCandidate;
    }

    public void setIceCandidate(IceCandidate iceCandidate) {
        this.iceCandidate = iceCandidate;
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public void setSessionDescription(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public WsData(IceCandidate iceCandidate, SessionDescription sessionDescription, String from, String to, EventType type) {
        this.iceCandidate = iceCandidate;
        this.sessionDescription = sessionDescription;
        this.from = from;
        this.to = to;
        this.type = type;
    }
}
