package ml.qingsu.fuckview;

import android.app.Application;


import com.tencent.bugly.crashreport.CrashReport;


/**
 * Created by w568w on 2017-6-30.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
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
