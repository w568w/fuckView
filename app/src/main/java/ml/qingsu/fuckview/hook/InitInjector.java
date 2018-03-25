package ml.qingsu.fuckview.hook;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.lang.reflect.Method;
import java.util.List;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ml.qingsu.fuckview.Constant;

/**
 * Created by w568w on 18-3-2.
 * <p>
 * So we do not need to reboot,but it'll run slowlier.
 * Just for debug cycle period.
 *
 * @author w568w
 */

public class InitInjector implements IXposedHookLoadPackage {
    public InitInjector() {
        super();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Context context = (Context) param.args[0];
                if (context != null) {
                    PackageManager manager = context.getPackageManager();
                    List<PackageInfo> list = manager.getInstalledPackages(0);
                    int size = list.size();
                    String pkgPath = null;
                    for (int i = 0; i < size; i++) {
                        PackageInfo info = list.get(i);
                        if (Constant.PKG_NAME.equals(info.packageName)) {
                            pkgPath = info.applicationInfo.sourceDir;
                            break;
                        }
                    }
                    if (pkgPath != null) {
                        PathClassLoader loader = new PathClassLoader(pkgPath, ClassLoader.getSystemClassLoader());
                        Class<?> hookerClz = Class.forName(Constant.PKG_NAME + ".hook.Hook", true, loader);
                        Object hooker =  hookerClz.newInstance();
                        Method handleLoadPackage=hookerClz.getDeclaredMethod("handleLoadPackage",XC_LoadPackage.LoadPackageParam.class);
                        handleLoadPackage.invoke(hooker,loadPackageParam);
                    }
                }
            }
        });
    }
}
