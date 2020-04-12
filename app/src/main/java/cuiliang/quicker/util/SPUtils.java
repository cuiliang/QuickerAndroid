package cuiliang.quicker.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

/**
 * Created by Void on 2020/4/12 13:39
 * SharedPreferences工具类
 */
public class SPUtils {
    private static SharedPreferences sp;

    public static void init(Application application) {
        sp = PreferenceManager.getDefaultSharedPreferences(application);

    }

    private SPUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean contains(@NonNull String key) {
        return sp.contains(key);
    }

    public static void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public static void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public static void putLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public static String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    public static Boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    public static void remove(String key) {
        sp.edit().remove(key).apply();
    }

    public static void clear() {
        sp.edit().clear().apply();
    }
}
