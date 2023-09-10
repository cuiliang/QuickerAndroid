package cuiliang.quicker.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Void on 2020/3/9 16:37
 * 上下文数据页面
 * 用于管理上下文page  View的显示和数据更新等
 * 如当前页面索引，上下文页面总数量等
 */
public class DataPageContextView extends DataPageView {

    public DataPageContextView(Context context) {
        this(context, null);
    }

    public DataPageContextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataPageContextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setColAndRowCount(4, 4);
        createActionButton();
    }

    protected Integer getButtonIndex(int row, int col) {
        return 1000000 + row * 1000 + col;
    }
}
