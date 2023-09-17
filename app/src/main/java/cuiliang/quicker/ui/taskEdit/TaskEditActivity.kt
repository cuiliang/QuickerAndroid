package cuiliang.quicker.ui.taskEdit

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.google.android.material.snackbar.Snackbar
import cuiliang.quicker.R
import cuiliang.quicker.adapter.FactorItemAdapter
import cuiliang.quicker.databinding.ActivityTaskEditBinding
import cuiliang.quicker.ui.taskManager.TaskData
import cuiliang.quicker.util.GsonUtils

class TaskEditActivity : AppCompatActivity() {
    private lateinit var ifFactoryAdapter: FactorItemAdapter
    private lateinit var ifActionAdapter: FactorItemAdapter

    private lateinit var mBinding: ActivityTaskEditBinding
    private lateinit var taskData: TaskData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityTaskEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val d = intent.getStringExtra("data")
        if (d.isNullOrEmpty()) {
            mBinding.toolbar.title = "新建任务"
        } else {
            taskData = GsonUtils.toBean(d, TaskData::class.java)
            mBinding.toolbar.title = "编辑任务"
        }


        ifFactoryAdapter = FactorItemAdapter(this)
        ifActionAdapter = FactorItemAdapter(this)
        mBinding.rvIfFactorList.adapter = ifFactoryAdapter.apply {
            setFooterData("添加条件", "如：当电量低于20%")
        }
        mBinding.rvIfActionList.adapter = ifActionAdapter.apply {
            setFooterData("添加结果", "例如：发送充电提示通知")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_task_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.btn_save -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun getLauncher(
            activity: ComponentActivity,
            callback: ActivityResultCallback<ActivityResult>
        ): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                callback
            )
        }

        /**
         * @param data 当data=null时，表示新增任务；否则是编辑任务
         */
        fun getIntent(context: Context, data: TaskData? = null): Intent {
            return Intent(context, TaskEditActivity::class.java).apply {
                if (data != null) putExtra("data", data.toString())
            }
        }
    }
}