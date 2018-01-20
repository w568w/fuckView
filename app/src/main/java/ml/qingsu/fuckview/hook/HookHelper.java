package ml.qingsu.fuckview.hook;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * Created by w568w on 18-1-10.
 */

public class HookHelper {
    //For debug.
    public static void HookEveryMethods(Class<?> clz, XC_MethodHook xc_methodHook) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method m : methods) {
            XposedBridge.hookMethod(m, xc_methodHook);
        }
    }
}
