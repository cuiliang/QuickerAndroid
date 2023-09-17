package cuiliang.quicker;

import android.app.Application;
import android.os.Build;
import android.util.DisplayMetrics;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cuiliang.quicker.util.SPUtils;

public class QuickerApplication extends Application {
    public static DisplayMetrics displayMetrics = null;

    @Override
    public void onCreate() {
        super.onCreate();
        displayMetrics = getResources().getDisplayMetrics();
        SPUtils.init(this);
//        closeHideApiDialog();
    }

    /**
     * 解决androidP 第一次打开程序出现莫名弹窗
     * 弹窗内容“detected problems with api ”
     */
    //这段代码会导致闪退，先注释，后续尝试复现这段注释出现的问题，然后用其他方法解决
//    private void closeHideApiDialog() {
//        try {
//            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
//            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
//            declaredConstructor.setAccessible(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            try {
//                Class cls = Class.forName("android.app.ActivityThread");
//                Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
//                declaredMethod.setAccessible(true);
//                Object activityThread = declaredMethod.invoke(null);
//                Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
//                mHiddenApiWarningShown.setAccessible(true);
//                mHiddenApiWarningShown.setBoolean(activityThread, true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
