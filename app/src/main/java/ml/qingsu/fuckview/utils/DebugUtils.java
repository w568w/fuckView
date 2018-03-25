package ml.qingsu.fuckview.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * Created by w568w on 18-1-21.
 */

public class DebugUtils {
    /**
     * 判断当前应用是否是debug状态
     */

    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *Hook某个类的所有方法
     */
    public static void hookEveryMethods(Class<?> clz, XC_MethodHook methodHook) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method m : methods) {
            XposedBridge.hookMethod(m, methodHook);
        }
    }
}
