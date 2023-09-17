package cuiliang.quicker.ui.taskManager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cuiliang.quicker.R
import cuiliang.quicker.databinding.ActivityTaskListBinding
import cuiliang.quicker.ui.taskEdit.TaskEditActivity

/**
 * Created by Voidcom on 2023/9/13 16:33
 * 用于显示任务列表
 */
class TaskListActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityTaskListBinding
    private val myTaskFragment=MyTaskFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction().let {
            it.add(R.id.my_task_fragment, myTaskFragment)
            it.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_task_manager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.add_new_task -> {
                myTaskFragment.openEditPageAndAddNewTask()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}