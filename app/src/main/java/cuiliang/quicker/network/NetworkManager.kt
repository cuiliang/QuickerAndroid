package cuiliang.quicker.network

import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import android.util.Log
import cuiliang.quicker.util.KLog

/**
 * Created by Void on 2020/4/12 10:35
 * 网络请求管理
 */
class NetWorkManager : Callback {
    private var threadPoolExecutor = ThreadPoolExecutor(3, 10, 120
            , TimeUnit.SECONDS, LinkedBlockingDeque<Runnable>())
    private var requestList = HashMap<Call, NetRequestObj>()
    private var okHttpClient = OkHttpClient()

    companion object {
        private var netWorkManager: NetWorkManager? = null
        fun getInstant(): NetWorkManager {
            if (netWorkManager == null)
                netWorkManager = NetWorkManager()
            return netWorkManager!!
        }
    }

    fun executeRequest(netRequestObj: NetRequestObj) {
        KLog.d(netRequestObj.toString())
        val request = Request.Builder()
        addHeader(request, netRequestObj)
        request.url(netRequestObj.url)
        request.post(addBody(netRequestObj))
        val call = okHttpClient.newCall(request.build())
        requestList[call] = netRequestObj
        threadPoolExecutor.execute {
            call.enqueue(this)
        }
    }

    fun executeRequest1(netRequestObj: NetRequestObj) {
        val request = Request.Builder()
        addHeader(request, netRequestObj)
        request.url(netRequestObj.url)
        val content = StringBuffer("{")
        for (s in netRequestObj.getData())
            content.append("\"${s.key}\":\"${s.value}\",")
        content.delete(content.length - 1, content.length).append("}")
        request.post(RequestBody.create(MediaType.parse("application/json"), content.toString()))
        val call = okHttpClient.newCall(request.build())
        requestList[call] = netRequestObj
        threadPoolExecutor.execute {
            call.enqueue(this)
        }
    }

    fun executeRequestPut(netRequestObj: NetRequestObj) {
        val request = Request.Builder()
        addHeader(request, netRequestObj)
        request.url(netRequestObj.url)
        request.put(addBody(netRequestObj))
        val call = okHttpClient.newCall(request.build())
        requestList[call] = netRequestObj
        threadPoolExecutor.execute {
            call.enqueue(this)
        }
    }

    /**
     * 添加请求头
     */
    private fun addHeader(request: Request.Builder, netRequestObj: NetRequestObj) {
        if (netRequestObj.getRequestHeader().isEmpty()) return
        for (s in netRequestObj.getRequestHeader())
            request.addHeader(s.key, s.value)
    }

    /**
     * 添加请求参数
     */
    private fun addBody(netRequestObj: NetRequestObj): FormBody {
        val formBody = FormBody.Builder()
        if (netRequestObj.isEncode)
            for (s in netRequestObj.getData())
                formBody.addEncoded(s.key, s.value)
        else
            for (s in netRequestObj.getData())
                formBody.add(s.key, s.value)
        return formBody.build()
    }

    override fun onFailure(call: Call, e: IOException) {
        try {
            if (requestList.containsKey(call)) {
                requestList[call]!!.requestCallback?.onError(e, null)
                requestList.remove(call)
            } else {
                Log.e("EVS_LOG", "一个没有被记录的网络请求！！并且请求失败了")
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResponse(call: Call, response: Response) {
        try {
            if (requestList.containsKey(call)) {
                requestList[call]!!.requestCallback?.onSuccess(response)
                requestList.remove(call)
            } else {
                Log.e("EVS_LOG", "一个没有被记录的网络请求！！" + response.message())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface RequestCallback {
        fun onSuccess(response: Response)

        fun onError(e: IOException, errorMessage: String?)
    }

}