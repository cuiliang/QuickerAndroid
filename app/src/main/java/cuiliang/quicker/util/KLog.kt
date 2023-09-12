package cuiliang.quicker.util

import android.text.TextUtils
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

object KLog {
    private val LINE_SEPARATOR = System.getProperty("line.separator")
    private const val NULL_TIPS = "Log with null object"
    private const val DEFAULT_MESSAGE = "execute"
    private const val TAG_DEFAULT = "KLog"
    private const val FILE_PREFIX = "KLog_"
    private const val FILE_FORMAT = ".log"

    private const val MAX_LENGTH = 4000
    private const val JSON_INDENT = 4
    private const val STACK_TRACE_INDEX_5 = 5

    const val V = 0x1
    const val D = 0x2
    const val I = 0x3
    const val W = 0x4
    const val E = 0x5
    const val A = 0x6

    private const val JSON = 0x7
    //XML=JSON+E
    private const val XML = 0xC

    private var globalTag: String = TAG_DEFAULT
    private var isShowLog = true

    /*
     * init
     * */
    @JvmStatic
    fun init(isShowLog: Boolean) {
        KLog.isShowLog = isShowLog
        File.separator
    }

    @JvmStatic
    fun init(isShowLog: Boolean, tag: String?) {
        KLog.isShowLog = isShowLog

        tag?.run {
            globalTag = this
        }
    }

    /*
     * base
     * */
    @JvmStatic
    fun v() {
        printLog(V, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun v(msg: Any) {
        printLog(V, null, msg)
    }

    @JvmStatic
    fun v(tag: String, objects: Any) {
        printLog(V, tag, objects)
    }

    @JvmStatic
    fun d() {
        printLog(D, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun d(msg: Any) {
        printLog(D, null, msg)
    }

    @JvmStatic
    fun d(tag: String, objects: Any) {
        printLog(D, tag, objects)
    }

    @JvmStatic
    fun i() {
        printLog(I, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun i(msg: Any) {
        printLog(I, null, msg)
    }

    @JvmStatic
    fun i(tag: String, objects: Any) {
        printLog(I, tag, objects)
    }

    @JvmStatic
    fun w() {
        printLog(W, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun w(msg: Any) {
        printLog(W, null, msg)
    }

    @JvmStatic
    fun w(tag: String, objects: Any) {
        printLog(W, tag, objects)
    }

    @JvmStatic
    fun e() {
        printLog(E, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun e(msg: Any) {
        printLog(E, null, msg)
    }

    @JvmStatic
    fun e(tag: String, objects: Any) {
        printLog(E, tag, objects)
    }

    @JvmStatic
    fun a() {
        printLog(A, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun a(msg: Any) {
        printLog(A, null, msg)
    }

    @JvmStatic
    fun a(tag: String, objects: Any) {
        printLog(A, tag, objects)
    }

    @JvmStatic
    fun json(level: Int, jsonFormat: String) {
        printLog(JSON + level, null, jsonFormat)
    }

    @JvmStatic
    fun json(level: Int, tag: String, jsonFormat: String) {
        printLog(JSON + level, tag, jsonFormat)
    }

    @JvmStatic
    fun xml(level: Int, xml: String) {
        printLog(XML + level, null, xml)
    }

    @JvmStatic
    fun xml(level: Int, tag: String, xml: String) {
        printLog(XML + level, tag, xml)
    }

    @JvmStatic
    fun map(level: Int, map: Map<String, Any>) {
        printLog(JSON + level, null, map)
    }

    @JvmStatic
    fun mapString(level: Int, tag: String, map: Map<String, String>) {
        printLog(JSON + level, tag, map)
    }

    @JvmStatic
    fun map(level: Int, tag: String, map: Map<String, Any>?) {
        if (map == null || map.isEmpty()) {
            printLog(level, tag, null)
            return
        }
        val jsonObject = JSONObject()
        try {
            for ((key, value) in map) {
                jsonObject.put(key, value.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        printLog(JSON + level, tag, jsonObject.toString())
    }

    @JvmStatic
    fun file(targetDirectory: File, msg: Any) {
        printFile(null, targetDirectory, null, msg)
    }

    @JvmStatic
    fun file(tag: String, targetDirectory: File, msg: Any) {
        printFile(tag, targetDirectory, null, msg)
    }

    @JvmStatic
    fun file(tag: String, targetDirectory: File, fileName: String, msg: Any) {
        printFile(tag, targetDirectory, fileName, msg)
    }

    @JvmStatic
    fun debug() {
        printDebug(null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun debug(msg: Any) {
        printDebug(null, msg)
    }

    @JvmStatic
    fun debug(tag: String, objects: Any) {
        printDebug(tag, objects)
    }

    /*
     * log内容处理
     * */
    private fun wrapperContent(stackTraceIndex: Int, tagStr: String?, objects: Any?): Array<String> {
        val targetElement = Thread.currentThread().stackTrace[stackTraceIndex]
        val className = targetElement.fileName
        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber

        if (lineNumber < 0) {
            lineNumber = 0
        }

        val tag = if (TextUtils.isEmpty(tagStr)) globalTag else tagStr!!
        val msg = objects?.toString() ?: NULL_TIPS
        val headString = "[($className:$lineNumber)#$methodName] "

        return arrayOf(tag, msg, headString)
    }

    /*
     * log输出
     * */
    private fun printLog(type: Int, tagStr: String?, objects: Any?) {
        if (!isShowLog) {
            return
        }

        val contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objects)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]

        when (type) {
            V, D, I, W, E, A -> checkLenPrint(type, tag, headString + msg)
            JSON + V, JSON + D, JSON + I, JSON + W, JSON + E -> printJson(type, tag, msg, headString)
            XML + V, XML + D, XML + I, XML + W, XML + E -> printXml(type, tag, msg, headString)
        }
    }

    private fun printDebug(tagStr: String?, objects: Any) {
        val contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objects)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]
        checkLenPrint(D, tag, headString + msg)
    }

    /**
     * 检查长度，如果超出MAX_LENGTH则分段输出
     *
     * @param type
     * @param tag
     * @param msg
     */
    private fun checkLenPrint(type: Int, tag: String, msg: String) {
        val length = msg.length
        val countOfSub = length / MAX_LENGTH

        if (countOfSub == 0) {
            //length<MAX_LENGTH
            print(type, tag, msg)
            return
        }
        //length>MAX_LENGTH
        var index = 0
        for (i in 0 until countOfSub) {
            val sub = msg.substring(index, index + MAX_LENGTH)
            print(type, tag, sub)
            index += MAX_LENGTH
        }
        print(type, tag, msg.substring(index, length))
    }

    private fun print(type: Int, tag: String, sub: String) {
        when (type) {
            V -> Log.v(tag, sub)
            D -> Log.d(tag, sub)
            I -> Log.i(tag, sub)
            W -> Log.w(tag, sub)
            E -> Log.e(tag, sub)
            A -> Log.wtf(tag, sub)
        }
    }

    private fun printJsonXml(level: Int, tag: String, msg: String, headString: String) {
        print(level, tag, "╔═══════════════════════════════════════════════════════════════════════════════════════")

        val message = headString + LINE_SEPARATOR + msg

        if (message.length > MAX_LENGTH) {
            checkLenPrint(level, tag, message)
        } else {
            val lines = message.split(LINE_SEPARATOR!!.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in lines) {
                print(level, tag, "║ $line")
            }
        }

        print(level, tag, "╚═══════════════════════════════════════════════════════════════════════════════════════")
    }

    /*
     * file
     * */

    /**
     * @param tagStr
     * @param targetDirectory
     * @param fileName
     * @param objectMsg
     */
    private fun printFile(tagStr: String?, targetDirectory: File, fileName: String?, objectMsg: Any) {
        if (!isShowLog) {
            return
        }
        val contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objectMsg)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]

        printFile(tag, targetDirectory, fileName, headString, msg)
    }

    /**
     * @param tag
     * @param targetDirectory
     * @param fileName
     * @param headString
     * @param msg
     */
    private fun printFile(tag: String, targetDirectory: File, fileName: String?, headString: String, msg: String) {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.CHINA)
        val nFileName =
                if (TextUtils.isEmpty(fileName)) FILE_PREFIX + formatter.format(System.currentTimeMillis()) + FILE_FORMAT
                else fileName!!

        if (save(targetDirectory, nFileName, msg)) {
            Log.d(tag, headString + " save log success ! location is >>>" + targetDirectory.absolutePath + "/" + nFileName)
        } else {
            Log.e(tag, headString + "save log fails !")
        }
    }

    /**
     * @param dic
     * @param fileName
     * @param msg
     * @return
     */
    private fun save(dic: File, fileName: String, msg: String): Boolean {
        val file = File(dic, fileName)

        return try {
            val outputStream = FileOutputStream(file)
            val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")
            outputStreamWriter.write(msg)
            outputStreamWriter.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 输出json格式
     * @param type Int
     * @param tag String
     * @param msg String
     * @param headString String
     */
    private fun printJson(type: Int, tag: String, msg: String, headString: String) {
        val message: String = try {
            when {
                msg.startsWith("{") -> JSONObject(msg).toString(JSON_INDENT)
                msg.startsWith("[") -> JSONArray(msg).toString(JSON_INDENT)
                else -> msg
            }
        } catch (e: JSONException) {
            msg
        }

        printJsonXml(type - JSON, tag, message, headString)
    }

    /*
     * xml
     * */
    private fun printXml(type: Int, tag: String, xml: String, headString: String) {
        var nXml = xml
        nXml = if (TextUtils.isEmpty(nXml)) NULL_TIPS else formatXML(nXml)
        printJsonXml(type - XML, tag, nXml, headString)
    }

    private fun formatXML(inputXML: String): String {
        return try {
            val xmlInput = StreamSource(StringReader(inputXML))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(xmlInput, xmlOutput)
            xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n")
        } catch (e: Exception) {
            e.printStackTrace()
            inputXML
        }
    }
}