package cuiliang.quicker.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.ArrayList

/**
 * Created by Voidcom on 2023/9/10 17:43
 *
 * Description: Json工具类
 */
object GsonUtils {
    private const val TAG = "GsonUtils"
    private val gson: Gson by lazy {
        GsonBuilder().create()
    }

    /**
     * 将对象内容转为json格式字符串
     *
     * @param object
     * @return
     */
    @JvmStatic
    fun toString(`object`: Any): String {
        return gson.toJson(`object`)
    }

    /**
     * json字符串转为实体类
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
    </T> */
    @JvmStatic
    fun <T> toBean(json: String, cls: Class<T>): T {
        return gson.fromJson(json, cls)
    }

    /**
     * json字符串转为实体类list
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
    </T> */
    fun <T> toList(json: String, cls: Class<T>): List<T> {
        val list = ArrayList<T>()
        val array = JsonParser().parse(json).asJsonArray
        for (elem in array)
            list.add(gson.fromJson(elem, cls))
        return list
    }

    /**
     * json字符串转为map list
     *
     * @param json
     * @param <T>
     * @return
    </T> */
    fun <T> toMapList(json: String): List<Map<String, T>> {
        return gson.fromJson(json, object : TypeToken<List<Map<String, T>>>() {

        }.type)
    }

    /**
     * json字符串转为map
     *
     * @param json
     * @param <T>
     * @return
    </T> */
    fun <T> toMap(json: String): Map<String, T> {
        return gson.fromJson(json, object : TypeToken<Map<String, T>>() {
        }.type)
    }


    @Throws(JsonSyntaxException::class)
    fun <T> deSerializedFromJson(json: String, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    @Throws(JsonSyntaxException::class)
    fun <T> deSerializedFromJson(json: String, type: Type): T {
        return gson.fromJson(json, type)
    }

    fun serializedToJson(`object`: Any?): String {
        return if (`object` != null) {
            gson.toJson(`object`)
        } else {
            ""
        }
    }

    /**
     * 获取JsonObject
     *
     * @return JsonObject
     */
    private fun parseJson(json: String): JsonObject? {
        return try {
            JsonParser().parse(json).asJsonObject
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "parseJson Exception===$e")
            null
        }
    }

    /**
     * 获取jsonArray
     *
     * @return jsonArray
     */
    private fun parseJsonArray(json: String): JsonArray? {
        return try {
            JsonParser().parse(json).asJsonArray
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "parseJson Exception===$e")
            null
        }
    }

    /**
     * json字符串转成Bean对象
     *
     * @param str
     * @param type
     * @return
     */
    fun <T> jsonToBean(str: String, type: Class<T>): T? {
        return try {
            Gson().fromJson(str, type)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "jsonToBean Exception===$e\n$str")
            null
        }
    }

    fun getArrayFromJson(json: String, key: String): JsonArray {
        return try {
            parseJson(json)!!.get(key) as JsonArray
        } catch (e: Exception) {
            e.printStackTrace()
            JsonArray()
        }
    }

    /**
     * 从JSON字符串提取出对应 Key的 字符串
     */
    fun getStringFromJSON(json: String?, key: String): String {
        if (json == null) return ""
        return try {
            try {
                parseJson(json)!!.get(key).asString
            } catch (e: Exception) {
                //Log.e(TAG, "getStringFromJSON Exception===\n$e\n【key=$key】json=$json");
                parseJson(json)!!.get(key).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 检查是否包含该key
     *
     * @return true包含
     */
    fun checkContainsKey(json: String, key: String): Boolean {
        return try {
            JSONObject(json).has(key)
        } catch (e: JSONException) {
            e.printStackTrace()
            false
        }

    }
}
