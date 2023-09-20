package cuiliang.quicker.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

/**
 * Created by Voidcom on 2023/9/20 14:44
 * TODO
 */
object FileTools {
    private val TAG = FileTools::class.java.name

    /**
     * 文件保存在: /data/data/[application package]/files
     * 该位置不需要读写权限
     */
    fun saveJsonToFile(context: Context, name: String, content: String) {
        if (content.isEmpty()) {
            KLog.e(TAG, "保存的内容不能为空！")
            return
        }
        val jsonFile = getTaskJsonFile(context, name) ?: return
        var fileWriter: FileWriter? = null
        var writer: PrintWriter? = null
        try {
            if (jsonFile.exists()) {
                //如果文件存在，清空文件内容
                fileWriter = FileWriter(jsonFile)
                fileWriter.write("")
                fileWriter.flush()
            } else {
                jsonFile.createNewFile()
            }
            writer = PrintWriter(FileOutputStream(jsonFile))
            writer.print(content)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileWriter?.close()
            writer?.close()
        }
    }

    fun readJsonFromFile(jsonFile: File): String {
        KLog.d(TAG, "readJsonFromFile：${jsonFile.path}")
        var fileReader: FileReader? = null
        var str = ""
        try {
            fileReader = FileReader(jsonFile)
            str = fileReader.readText()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileReader?.close()
        }
        return str
    }

    fun readJsonFromFile(context: Context, name: String): String {
        val jsonFile = getTaskJsonFile(context, name) ?: return ""
        return readJsonFromFile(jsonFile)
    }

    fun getTaskCachePath(context: Context): String = context.filesDir.path + "/taskJson"

    private fun getTaskJsonFile(context: Context, name: String): File? {
        if (name.isEmpty()) {
            KLog.e(TAG, "文件名不能为空！")
            return null
        }
        val folder = File(getTaskCachePath(context))
        folder.mkdirs()
        return File(folder, "$name.json").apply {
            KLog.d(TAG, "JsonFilePath：${this.path}")
        }
    }

    /**
     * 获取getTaskCachePath() 文件夹下的文件列表
     * @return 返回的文件列表进包含后缀为.json
     */
    fun getFileList(context: Context): Array<out File>? {
        val folder = File(getTaskCachePath(context))
        return folder.listFiles { _, name -> name?.endsWith(".json") ?: false }
    }
}