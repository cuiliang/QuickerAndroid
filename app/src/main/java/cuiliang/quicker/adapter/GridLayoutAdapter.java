package cuiliang.quicker.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cuiliang.quicker.UiButtonItem;
import cuiliang.quicker.client.ClientManager;
import cuiliang.quicker.messages.send.CommandMessage;
import cuiliang.quicker.messages.send.ToggleMuteMessage;
import cuiliang.quicker.view.DataPageContextView;
import cuiliang.quicker.view.DataPageGlobalView;
import cuiliang.quicker.view.DataPageView;

/**
 * Created by Void on 2020/3/3 15:36
 */
public class GridLayoutAdapter extends PagerAdapter {
    private List<DataPageView> items = new ArrayList<>();
    //用于记录当前viewpager索引
    private int currentPageIndex = 0;

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(items.get(position));
        return items.get(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(items.get(position));
    }

    public UiButtonItem getActionBtnObject(int index, int currentPageIndex) {
        try {
            return items.get(currentPageIndex).actionBtnArray.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 因为{@link cuiliang.quicker.messages.recv.UpdateButtonsMessage}消息下发的比较频繁，
     * 且数据格式都是一样的，因此抛弃每次收到下发消息后清除所有view重新根据数据生成view的思路。
     * 而使用根据下发的页数动态生成 {@link DataPageView}。
     * 最终展示数据的DataPageView仅仅是数据的载体，和数据没有联系。
     *
     * @param num 总页数
     */
    public void createPages(Context context, int num, boolean isGlobal) {
        while (true) {
            int tmp = items.size() - num;
            if (tmp > 0) {
                items.remove(items.size() - 1);
            } else if (tmp < 0 && isGlobal) {
                items.add(new DataPageGlobalView(context));
            } else if (tmp < 0) {
                items.add(new DataPageContextView(context));
            } else {
                break;
            }
        }
    }

    /**
     * 根据索引刷新数据页
     */
    public void updateDatePage(int index, boolean isGlobal) {
        if (items.isEmpty()) return;
        int tmp = currentPageIndex - index;
        if (isGlobal) {
            //全局数据页
            if (tmp > 0) {
                ClientManager.getInstance().requestUpdateDataPage(CommandMessage.DATA_PAGE_GLOBAL_LEFT);
            } else {
                ClientManager.getInstance().requestUpdateDataPage(CommandMessage.DATA_PAGE_GLOBAL_RIGHT);
            }
        } else {
            //上下文页
            if (tmp > 0) {
                ClientManager.getInstance().requestUpdateDataPage(CommandMessage.DATA_PAGE_CONTEXT_LEFT);
            } else {
                ClientManager.getInstance().requestUpdateDataPage(CommandMessage.DATA_PAGE_CONTEXT_RIGHT);
            }
        }
        currentPageIndex = index;
    }
}
