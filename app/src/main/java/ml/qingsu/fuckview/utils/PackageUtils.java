package ml.qingsu.fuckview.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.view.View;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by w568w on 18-6-8.
 *
 * @author w568w
 */

public class PackageUtils {
    private static List<RunningServiceInfo> serviceInfoList = null;
    private static ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

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

    public static void killProcess(String pkg) throws IOException {
        java.lang.Process process = Runtime.getRuntime().exec("su");
        OutputStream out = process.getOutputStream();
        String cmd = "am force-stop " + pkg + " \n";
        out.write(cmd.getBytes());

        out.flush();
        out.close();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void asyncStopProcess(final String pkgName, final Runnable callback, final View runner) {
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    killProcess(pkgName);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (runner != null) {
                        runner.post(callback);
                    } else {
                        callback.run();
                    }
                }

            }
        });
    }
}
