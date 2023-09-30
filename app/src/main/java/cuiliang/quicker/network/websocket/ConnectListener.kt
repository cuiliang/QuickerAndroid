package cuiliang.quicker.network.websocket

import cuiliang.quicker.client.ConnectionStatus

/**
 * Created by Voidcom on 2023/9/20 22:55
 * TODO
 */
interface ConnectListener {
    fun onStatus(status: ConnectionStatus)
}