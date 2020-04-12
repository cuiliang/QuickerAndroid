package cuiliang.quicker.network.shareToPc

/**
 * Created by Void on 2020/4/12 12:19
 */
object ShareApi {
    //域名
    private const val baseUrl = "https://push.getquicker.net"
    //分享接口(get、post都是用这个接口，详情请查看文档)。https://www.getquicker.net/KC/Help/Doc/connection
    const val shareUrl = "$baseUrl/push"
}