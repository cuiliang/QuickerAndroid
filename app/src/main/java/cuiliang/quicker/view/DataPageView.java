package cuiliang.quicker.view;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

import cuiliang.quicker.R;
import cuiliang.quicker.UiButtonItem;
import cuiliang.quicker.client.ClientManager;

/**
 * Created by Void on 2020/3/11 16:57
 * 将展示动作按钮数据的页面抽象，
 * 全局和上下文page的单独操作在其实现类里面进行
 */
public abstract class DataPageView extends GridLayout implements View.OnClickListener {

    protected static final String TAG = DataPageView.class.getSimpleName();
    //存放page的按钮对象，用于更新按钮的图片和文字等
    public Map<Integer, UiButtonItem> actionBtnArray = new HashMap<>();
    //当前页面的行，用于布局
    public int currentPageRow = 0;
    //当前页面的列，用于布局
    public int currentPageCol = 0;

    public DataPageView(Context context) {
        this(context, null);
    }

    public DataPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onClick(View v) {
        int btnIndex = (int) v.getTag();
        Log.d(TAG, "按钮触摸！" + btnIndex);
        ClientManager.getInstance().sendButtonClickMsg(btnIndex);
    }

    /**
     * 根据位置生成按钮索引
     */
    abstract Integer getButtonIndex(int row, int col);

    protected void setColAndRowCount(int row, int col) {
        setRowCount(row);
        setColumnCount(col);
        this.currentPageRow = row;
        this.currentPageCol = col;
        Log.i("QuickPageUi", getClass().getSimpleName() + ";row:" + row + ";col:" + col);
    }

    /**
     * 根据屏幕方向创建按钮
     */
    public void createActionButton() {
        if (!actionBtnArray.isEmpty()) actionBtnArray.clear();
        for (int rowIndex = 0; rowIndex < currentPageRow; rowIndex++)
            for (int colIndex = 0; colIndex < currentPageCol; colIndex++) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_action_button, null);
                ViewGroup actionBtn = view.findViewById(R.id.actionBtn);
                actionBtn.setTag(getButtonIndex(rowIndex, colIndex));
                actionBtn.setOnClickListener(this);
                GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(
                        GridLayout.spec(rowIndex, 1f),
                        GridLayout.spec(colIndex, 1f)
                );
                gridLayoutParam.setMargins(1, 1, 1, 1);
                gridLayoutParam.height = 0;
                gridLayoutParam.width = 0;
                addView(view, gridLayoutParam);
                UiButtonItem item = new UiButtonItem();
                item.button = actionBtn;
                item.imageView = view.findViewById(R.id.actionBtnBg);
                item.textView = view.findViewById(R.id.actionBtnText);
                actionBtnArray.put(getButtonIndex(rowIndex, colIndex), item);
            }
    }
}
