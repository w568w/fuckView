package ml.qingsu.fuckview.hook;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Keep;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.List;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ml.qingsu.fuckview.Constant;

import static ml.qingsu.fuckview.Constant.ACTIVITY_NAME;
import static ml.qingsu.fuckview.Constant.HOOK_CLASS;
import static ml.qingsu.fuckview.Constant.PKG_NAME;
import static ml.qingsu.fuckview.Constant.VAILD_METHOD;

/**
 * Created by w568w on 18-3-2.
 * <p>
 * So we do not need to reboot,but it'll run slowlier.
 *
 * @author w568w
 * @author shuihuadx
 */

public class InitInjector implements IXposedHookLoadPackage {
    public InitInjector() {
        super();
    }

    @Override
    @Keep
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //Moved (see Issue #3)
        if (PKG_NAME.equals(loadPackageParam.packageName)) {
            try {
                XposedHelpers.findAndHookMethod(ACTIVITY_NAME, loadPackageParam.classLoader,
                        VAILD_METHOD, XC_MethodReplacement.returnConstant(true));
                return;
                // FIXME: 18-4-28 this error is often reported for some unknown reasons.
                /*
                Reporter's log:
                 W/System  ( 9297): ClassLoader referenced unknown path: /data/app/ml.qingsu.fuckview-1/lib/arm
                 E/Xposed  ( 9297): java.lang.NoSuchMethodError: ml.qingsu.fuckview.ui.activities.MainActivity#isModuleActive()#exact
                 E/Xposed  ( 9297): 	at de.robv.android.xposed.XposedHelpers.findMethodExact(XposedHelpers.java:344)
                 E/Xposed  ( 9297): 	at de.robv.android.xposed.XposedHelpers.findAndHookMethod(XposedHelpers.java:185)
                 E/Xposed  ( 9297): 	at ml.qingsu.fuckview.hook.InitInjector.handleLoadPackage(Unknown Source)
                * */
            } catch (NoSuchMethodError error) {
                error.printStackTrace();
            }
        }
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Context context = (Context) param.args[0];
                if (context != null) {
                    loadPackageParam.classLoader = context.getClassLoader();
                    try {
                        invokeHandleHookMethod(context, Constant.PKG_NAME, Constant.HOOK_CLASS, "handleLoadPackage", loadPackageParam);
                    } catch (Throwable error) {
                        error.printStackTrace();
                    }
                }
            }
        });
    }

    private void invokeHandleHookMethod(Context context, String modulePackageName, String handleHookClass, String handleHookMethod, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //原来的两种方式不是很好,改用这种新的方式
        File apkFile = findApkFile(context, modulePackageName);
        if (apkFile == null) {
            new RuntimeException("寻找模块apk失败").printStackTrace();
        }
        //加载指定的hook逻辑处理类，并调用它的handleHook方法
        PathClassLoader pathClassLoader = new PathClassLoader(apkFile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
        Class<?> cls = Class.forName(handleHookClass, true, pathClassLoader);
        Object instance = cls.newInstance();
        Method method = cls.getDeclaredMethod(handleHookMethod, XC_LoadPackage.LoadPackageParam.class);
        method.invoke(instance, loadPackageParam);
    }

    /**
     * 根据包名构建目标Context,并调用getPackageCodePath()来定位apk
     *
     * @param context           context参数
     * @param modulePackageName 当前模块包名
     * @return return apk file
     */
    private File findApkFile(Context context, String modulePackageName) {
        if (context == null) {
            return null;
        }
        try {
            Context moudleContext = context.createPackageContext(modulePackageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            String apkPath = moudleContext.getPackageCodePath();
            return new File(apkPath);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
