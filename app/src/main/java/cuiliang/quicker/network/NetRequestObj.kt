package cuiliang.quicker.network

/**
 * Created by Void on 2019/7/15 09:38
 * 网络请求对象
 * @param url 请求地址
 * @param requestCallback 结果回调，可为空
 */
class NetRequestObj(var url: String, var requestCallback: NetWorkManager.RequestCallback?) {
    private var data = HashMap<String, String>()
    private var requestHeader = HashMap<String, String>()

    init {
        //默认参数格式
        addHeader("Content-Type", "application/x-www-form-urlencoded")
    }

    /**
     * 请求参数是否进行HTML编码
     */
    var isEncode = false

    fun getData() = data

    fun getRequestHeader() = requestHeader

    fun addBody(keyStr: String, valueStr: String): NetRequestObj {
        data[keyStr] = valueStr
        return this
    }

    fun addHeader(keyStr: String, valueStr: String): NetRequestObj {
        requestHeader[keyStr] = valueStr
        return this
    }

    fun setContentType(string: String): NetRequestObj {
        removeHeader("Content-Type")
        addHeader("Content-Type", string)
        return this
    }

    fun addAllBody(items: Map<String, String>) {
        data.putAll(items)
    }

    fun addAllHeader(items: Map<String, String>) {
        requestHeader.putAll(items)
    }

    fun removeBody(keyStr: String) {
        data.remove(keyStr)
    }

    fun removeHeader(keyStr: String) {
        data.remove(keyStr)
    }

    fun removeAllData() {
        data.clear()
    }

    fun removeAllHeader() {
        requestHeader.clear()
    }

    fun sizeBody() = data.size

    fun sizeHeader() = requestHeader.size

    fun containsKeyBody(keyStr: String) = data.containsKey(keyStr)

    fun containsKeyHeader(keyStr: String) = requestHeader.containsKey(keyStr)

    override fun toString(): String {
        var output = "\n url:\"$url\"; \n"
        for (s in data)
            output += s.key + ":" + s.value + "; \n"
        return output
    }
}