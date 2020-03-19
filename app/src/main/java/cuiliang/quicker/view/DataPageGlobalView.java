package cuiliang.quicker.view;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import cuiliang.quicker.R;
import cuiliang.quicker.UiButtonItem;
import cuiliang.quicker.client.ClientManager;

/**
 * Created by Void on 2020/3/9 16:37
 * 上下文数据页面
 * 继承GridLayout是为了能够储存，该页面一些数据。
 * 如当前页面索引，全局页面总数量等
 */
public class DataPageGlobalView extends DataPageView {

    public DataPageGlobalView(Context context) {
        this(context, null);
    }

    public DataPageGlobalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataPageGlobalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setColAndRowCount(3, 4);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setColAndRowCount(3, 4);
        }else {
            Log.e("DataPageGlobalView","获取屏幕方向结果异常！");
        }
        createActionButton();
    }

    protected Integer getButtonIndex(int row, int col) {
        return row * 1000 + col;
    }
}
