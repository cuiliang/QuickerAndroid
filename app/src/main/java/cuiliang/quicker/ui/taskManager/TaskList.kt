package cuiliang.quicker.ui.taskManager

import android.content.Context
import android.util.ArraySet
import cuiliang.quicker.taskManager.JsonTask
import cuiliang.quicker.taskManager.Task
import cuiliang.quicker.taskManager.TaskDataFactory
import cuiliang.quicker.util.FileTools
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Voidcom on 2023/9/29 10:03
 * 任务列表，用于service中的任务管理
 * 任务列表的数据在 [MyTaskFragment]展示
 *
 * @see cuiliang.quicker.service.TaskManagerService
 */
class TaskList(private val context: Context) {
    private val taskKey: ArraySet<String> = ArraySet()
    private val taskValues: ArrayList<Task> = arrayListOf()
    private var job: Job? = null

    fun put(key: String, value: Task) {
        if (taskKey.add(key)) {
            taskValues.add(value)
        } else {
            taskValues[indexOf(key)] = value
        }
        value.taskInit(context)
    }

    fun remove(key: String) {
        val index = taskKey.indexOf(key)
        if (index < 0) return
        taskValues[index].release(context)
        taskKey.removeAt(index)
        taskValues.removeAt(index)
        saveConfig()
    }

    fun indexOf(key: String): Int = taskKey.indexOf(key)

    fun size(): Int = taskKey.size

    fun get(i: Int): Task = taskValues[i]

    fun get(key: String): Task = taskValues[indexOf(key)]

    fun clear() {
        taskValues.forEach {
            it.release(context)
        }
        taskKey.clear()
        taskValues.clear()
    }

    /**
     * 读取任务配置，仅在服务启动时调用
     * 先 判断是否有配置文件
     * 没有配置文件，直接加载默认配置。
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun readFileConfig() {
        val files = FileTools.getFileList(context)
        //判断是否有配置文件
        if (files.isNullOrEmpty()) {
            //没有配置文件，直接加载默认配置。finish Activity时会保存配置，然后这份默认不论有没有被修改都会变成配置文件
            TaskDataFactory().defaultTaskConfig().forEach {
                put(it.name, it)
            }
        } else {
            GlobalScope.launch {
                files.forEach {
                    val tmp = JsonTask.jsonToTask(FileTools.readJsonFromFile(it)).toTask()
                    put(tmp.name, tmp)
                }
            }
        }
    }

    /**
     * 保存任务到文件
     * 该方法短时间可能被多次调用，所以做了处理，只有最后一次会执行写入文件操作
     * 类似Handler.postDelayed() 和 Handler().removeCallbacks()
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun saveConfig() {
        job?.let {
            if (it.isActive) {
                it.cancel(null)
            }
            job = null
        }
        job = GlobalScope.launch {
            delay(500)
            taskValues.forEach {
                FileTools.saveJsonToFile(context, it.name, JsonTask(it).toString())
            }
        }
        job?.start()
    }
}