package cuiliang.quicker.events;

import cuiliang.quicker.client.ConnectionStatus;

/**
 * 到pc的连接状态改变了
 */
public class ConnectionStatusChangedEvent {

    public ConnectionStatus status;

    public String message;

    public ConnectionStatusChangedEvent(ConnectionStatus status, String message){
        this.status = status;
        this.message = message;
    }
}
