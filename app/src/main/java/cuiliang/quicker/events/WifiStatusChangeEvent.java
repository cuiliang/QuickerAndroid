package cuiliang.quicker.events;

/**
 * Wifi状态改变通知
 */
public class WifiStatusChangeEvent {

    public boolean isConnected;

    public WifiStatusChangeEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
