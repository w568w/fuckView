package ml.qingsu.fuckview.hook;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Keep;

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
                // FIXME: 18-4-28 this error is often reported for unknown reason.
                /*
                Reporter's log:
                 W/System  ( 9297): ClassLoader referenced unknown path: /data/app/ml.qingsu.fuckview-1/lib/arm
                 E/Xposed  ( 9297): java.lang.NoSuchMethodError: ml.qingsu.fuckview.ui.activities.MainActivity#isModuleActive()#exact
                 E/Xposed  ( 9297): 	at de.robv.android.xposed.XposedHelpers.findMethodExact(XposedHelpers.java:344)
                 E/Xposed  ( 9297): 	at de.robv.android.xposed.XposedHelpers.findAndHookMethod(XposedHelpers.java:185)
                 E/Xposed  ( 9297): 	at ml.qingsu.fuckview.hook.InitInjector.handleLoadPackage(Unknown Source)
                * */
            }catch (NoSuchMethodError error){
                error.printStackTrace();
            }
        }
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Context context = (Context) param.args[0];
                if (context != null) {
                    List<PackageInfo> list = context.getPackageManager().getInstalledPackages(0);
                    final int size = list.size();
                    String pkgPath = null;
                    for (int i = 0; i < size; i++) {
                        PackageInfo info = list.get(i);
                        if (PKG_NAME.equals(info.packageName)) {
                            pkgPath = info.applicationInfo.sourceDir;
                            break;
                        }
                    }
                    if (pkgPath != null) {
                        PathClassLoader loader = new PathClassLoader(pkgPath, ClassLoader.getSystemClassLoader());
                        Class<?> hookerClz = Class.forName(HOOK_CLASS, true, loader);
                        Object hooker = hookerClz.newInstance();
                        Method handleLoadPackage = hookerClz.getDeclaredMethod("handleLoadPackage", XC_LoadPackage.LoadPackageParam.class);
                        handleLoadPackage.invoke(hooker, loadPackageParam);
                    }
                }
            }
        });
    }
}
