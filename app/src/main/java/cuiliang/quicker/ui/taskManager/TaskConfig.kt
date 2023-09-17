package cuiliang.quicker.ui.taskManager

/**
 * Created by Voidcom on 2023/9/13 17:13
 * TODO
 */
object TaskConfig {
    var ACTION_LIST: HashMap<String, String> = hashMapOf()

    fun decodeActionMsg(msg: String) {
        val tmp = msg.split(":", "\r\n")
        for (i in tmp.indices step 2) {
            ACTION_LIST[tmp[i]] = tmp[i + 1]
        }
    }
}