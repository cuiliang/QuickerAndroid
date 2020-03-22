package cuiliang.quicker.util;

import com.bumptech.glide.util.Synthetic;

/**
 * Created by Void on 2020/3/12 16:33
 * 用于存放通用page数据。
 * 如：全局页面总数、上下文页面总数、当前显示的上下文页面在总页面的索引等
 */
public class DataPageValues {
    //上下文page名称
    public static String contextPageName = "";
    //全局页面总数
    public static int globalDataPageCount = 0;
    //当前页面在全局总页数中的索引
    public static int currentGlobalPageIndex = 0;
    //上下文页面总数
    public static int contextDataPageCount = 0;
    //当前页面在上下文总页数中的索引
    public static int currentContextPageIndex = 0;
    // 上下文面板切换是否锁定
    public static boolean IsContextPanelLocked = false;
}
