package ml.qingsu.fuckview;

import android.app.Application;
import android.content.Context;


import com.tencent.bugly.crashreport.CrashReport;


/**
 * Created by w568w on 2017-6-30.
 */

public class MyApplication extends Application {
    public static Context con;

    @Override
    public void onCreate() {
        super.onCreate();
        con = this;
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        // Normal app init code...
        CrashReport.initCrashReport(getApplicationContext(), "cfccfa2c50", false);
    }
}
