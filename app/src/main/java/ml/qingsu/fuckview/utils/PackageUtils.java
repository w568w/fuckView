package ml.qingsu.fuckview.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import java.util.List;

/**
 * Created by w568w on 18-6-8.
 *
 * @author w568w
 */

public class PackageUtils {
    private static List<RunningServiceInfo> serviceInfoList = null;

    public static void initLists(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        serviceInfoList = am.getRunningServices(1000);
    }

    public static boolean isRunning(String packag) {
        int len = serviceInfoList.size();
        for (int i = 0; i < len; ++i) {
            if (serviceInfoList.get(i).service.getPackageName().equals(packag)) {
                return true;
            }
        }
        return false;
    }
}
