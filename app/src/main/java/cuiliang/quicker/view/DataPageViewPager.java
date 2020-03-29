package cuiliang.quicker.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.util.prefs.BackingStoreException;

import cuiliang.quicker.R;
import cuiliang.quicker.UiButtonItem;
import cuiliang.quicker.adapter.GridLayoutAdapter;
import cuiliang.quicker.util.DataPageValues;

/**
 * Created by Void on 2020/3/19 13:16
 */
public class DataPageViewPager extends ViewPager {

    private ViewPagerCuePoint cuePointView;
    private GridLayoutAdapter dataPageAdapter;
    /*判断页面是代码切换还是手势手动切换是手势滑动切换page，
    当调用setCurrentItem()方法时，为false*/
    private boolean isGesture = false;
    //这个view是否是全局page
    private boolean isGlobal;

    public DataPageViewPager(@NonNull Context context) {
        this(context, null);
    }

    public DataPageViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView(ViewPagerCuePoint view, final boolean isGlobal) {
        this.cuePointView = view;
        this.isGlobal = isGlobal;
        dataPageAdapter = new GridLayoutAdapter();
        setAdapter(dataPageAdapter);
        /*
         * 注：在 ViewPager 内有个onPageScrolled方法，这与接口OnPageChangeListener中方法重名，
         * 这样会造成异常，并导致堆溢出，所以推荐这样写。
         * */
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (isGesture) dataPageAdapter.updateDatePage(position, isGlobal);
                if (isGlobal)
                    cuePointView.updateGlobalCuePoint();
                else
                    cuePointView.updateContextCuePoint();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    isGesture = false;
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    isGesture = true;
                }
            }
        });
    }

    /**
     * 更新page页数。
     * 全局和上下文page的页数是不固定的，会根据场景变化。
     * viewpager更新page页数不会影响显示的数据，以为数据和page是分开的。
     * pc端会根据情况下发显示的action按钮数据
     *
     * @param num   总页数
     * @param index 当前page索引
     */
    public void updatePage(int num, int index) {
        dataPageAdapter.createPages(getContext(), num, isGlobal);
        dataPageAdapter.notifyDataSetChanged();
        setCurrentItem(index);
        if (isGlobal)
            cuePointView.updateGlobalCuePoint();
        else
            cuePointView.updateContextCuePoint();
    }

    public UiButtonItem getActionBtnObject(int index, int currentPageIndex) {
        return dataPageAdapter.getActionBtnObject(index, currentPageIndex);
    }
}
