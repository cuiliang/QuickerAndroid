package cuiliang.quicker.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cuiliang.quicker.R;
import cuiliang.quicker.util.DataPageValues;

/**
 * Created by Void on 2020/3/22 09:42
 * viewpager的导航点
 */
public class ViewPagerCuePoint extends LinearLayout {
    private LinearLayout gLayout;
    private LinearLayout cLayout;

    public ViewPagerCuePoint(Context context) {
        this(context, null);
    }

    public ViewPagerCuePoint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPagerCuePoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
        gLayout = new LinearLayout(context);
        cLayout = new LinearLayout(context);

        addView(gLayout);
        addView(cLayout);
    }

    /**
     * 更新全局页指示点
     */
    public void updateGlobalCuePoint() {
        while (true) {
            int tmp = gLayout.getChildCount() - DataPageValues.globalDataPageCount;
            if (tmp > 0) {
                gLayout.removeViewAt(gLayout.getChildCount() - 1);
            } else if (tmp < 0) {
                addImageView(true);
            } else {
                break;
            }
        }
        refreshView(true);
    }

    /**
     * 更新上下文页面指示点
     */
    public void updateContextCuePoint() {
        while (true) {
            int tmp = cLayout.getChildCount() - DataPageValues.contextDataPageCount;
            if (tmp > 0) {
                cLayout.removeViewAt(cLayout.getChildCount() - 1);
            } else if (tmp < 0) {
                addImageView(false);
            } else {
                break;
            }
        }
        refreshView(false);
    }

    /**
     * 更新指示点的ui
     */
    private void refreshView(boolean isGlobal) {
        LinearLayout view = isGlobal ? gLayout : cLayout;
        int count = isGlobal ? DataPageValues.globalDataPageCount : DataPageValues.contextDataPageCount;
        int index = isGlobal ? DataPageValues.currentGlobalPageIndex : DataPageValues.currentContextPageIndex;
        try {
            for (int i = 0; i < count; i++) {
                if (i == index) {
                    view.getChildAt(i).setBackgroundResource(R.drawable.ic_point_light);
                } else {
                    view.getChildAt(i).setBackgroundResource(R.drawable.ic_point_dark);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成一个imageView并添加进布局
     */
    public void addImageView(boolean isGlobal) {
        ImageView image = new ImageView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.d_5),
                getResources().getDimensionPixelSize(R.dimen.d_5)
        );
        layoutParams.setMargins(10, 10, 10, 10);
        image.setLayoutParams(layoutParams);
        if (isGlobal)
            gLayout.addView(image);
        else
            cLayout.addView(image);
    }

}
