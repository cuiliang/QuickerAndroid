package test

import androidx.test.ext.junit.runners.AndroidJUnit4
import cuiliang.quicker.client.ClientConfig
import cuiliang.quicker.network.websocket.WebSocketClient
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Voidcom on 2023/9/11 18:21
 *
 * Description: TODO
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun connect() {
        ClientConfig.getInstance().enableHttps = true
        ClientConfig.getInstance().mServerHost = "192.168.1.100"
        ClientConfig.getInstance().mServerPort = "668"
        ClientConfig.getInstance().ConnectionCode = "aaa"
        WebSocketClient.instance().connectRequest { result, msg ->
            if (!result)
                println("服务连接失败：$msg")
        }
        //因为单元测试方法运行完会直接结束，而其他线程工作还没结束，这里加一个延时
        Thread.sleep(3000)
    }

}