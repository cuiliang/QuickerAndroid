package cuiliang.quicker.ui.taskEdit

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import com.cuiliang.quicker.ui.BaseViewModel
import cuiliang.quicker.adapter.TaskDetailsItemAdapter
import cuiliang.quicker.databinding.ActivityTaskEditBinding
import cuiliang.quicker.taskManager.BaseEventOrAction
import cuiliang.quicker.taskManager.JsonAction
import cuiliang.quicker.taskManager.JsonEvent
import cuiliang.quicker.taskManager.action.Action
import cuiliang.quicker.taskManager.action.ActionAdd
import cuiliang.quicker.taskManager.event.Event
import cuiliang.quicker.taskManager.event.EventAdd
import cuiliang.quicker.ui.EventOrActionActivity

/**
 * Created by voidcom on 2023/10/7
 *
 */
class TaskEditViewModel : BaseViewModel() {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var ifFactoryAdapter: TaskDetailsItemAdapter<Event>
    private lateinit var ifActionAdapter: TaskDetailsItemAdapter<Action>
    var taskName = MutableLiveData("")
    var title = MutableLiveData("")

    override val model: TaskEditModel by lazy { TaskEditModel() }

    override fun onInit(context: Context) {
        launcher = EventOrActionActivity.getLauncher(context as TaskEditActivity, addEventCallback)

        ifFactoryAdapter = TaskDetailsItemAdapter(context)
        ifFactoryAdapter.setCallback(adapterClickCallback(context,0))
        ifFactoryAdapter.setFooterData(EventAdd())

        ifActionAdapter = TaskDetailsItemAdapter(context)
        ifActionAdapter.setCallback(adapterClickCallback(context,1))
        ifActionAdapter.setFooterData(ActionAdd())
        (vmBinding as ActivityTaskEditBinding).run {
            rvIfFactorList.adapter = ifFactoryAdapter
            rvIfActionList.adapter = ifActionAdapter
        }
    }

    override fun onInitData() {
        ifFactoryAdapter.setFooterData(EventAdd())
        ifActionAdapter.setFooterData(ActionAdd())
    }

    fun saveData(): Boolean {
        model.task.name = taskName.value?:""
        return model.task.name.isNotEmpty() && model.task.events.isNotEmpty() && model.task.taskActions.isNotEmpty()
    }

    fun refreshAllData() {
        ifFactoryAdapter.addItems(model.task.events)
        ifActionAdapter.addItems(model.task.taskActions)
    }

    private val addEventCallback = ActivityResultCallback<ActivityResult> { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) return@ActivityResultCallback
        val dataType = result.data!!.getIntExtra(EventOrActionActivity.DATA_TYPE, 0)
        val type = result.data!!.getStringExtra(EventOrActionActivity.RESULT_DATA)
        if (dataType == 0) {
            ifFactoryAdapter.addItem(JsonEvent.jsonToEvent(type).toEvent())
        } else {
            ifActionAdapter.addItem(JsonAction.jsonToAction(type).toAction())
        }
    }

    private fun <T : BaseEventOrAction> adapterClickCallback(context: Context,type: Int): (List<T>) -> Unit {
        return {
            val array = Array(it.size) { "" }
            for (i in it.indices) {
                array[i] = it[i].getName()
            }
            launcher.launch(EventOrActionActivity.getInstant(context, type, array))
        }
    }
}