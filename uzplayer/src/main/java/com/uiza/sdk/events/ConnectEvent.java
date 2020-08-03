package com.uiza.sdk.events;


import java.io.Serializable;
import java.util.Objects;

/**
 * Created by www.muathu@gmail.com on 10/21/2017.
 */

public class ConnectEvent implements  Serializable {

    private final boolean connected;
    private final boolean connectedFast;
    private final boolean connectedWifi;
    private final boolean connectedMobile;

    public ConnectEvent(){
        this(false, false, false, false);
    }

    public ConnectEvent(boolean connected, boolean connectedWifi, boolean connectedMobile, boolean connectedFast){
        this.connected = connected;
        this.connectedWifi = connectedWifi;
        this.connectedMobile = connectedMobile;
        this.connectedFast = connectedFast;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isConnectedFast() {
        return connectedFast;
    }

    public boolean isConnectedWifi() {
        return connectedWifi;
    }

    public boolean isConnectedMobile() {
        return connectedMobile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectEvent that = (ConnectEvent) o;
        return connected == that.connected &&
                connectedFast == that.connectedFast &&
                connectedWifi == that.connectedWifi &&
                connectedMobile == that.connectedMobile;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connected, connectedFast, connectedWifi, connectedMobile);
    }
}
