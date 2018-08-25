package ml.qingsu.fuckview.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.tencent.bugly.crashreport.CrashReport;

import ml.qingsu.fuckview.Constant;

import static ml.qingsu.fuckview.Constant.ACTION_LAZY_LOAD_SERVICE;


/**
 * @author w568w
 */
public class LazyLoadService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public LazyLoadService() {
        super("LazyLoadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LAZY_LOAD_SERVICE.equals(action)) {
                CrashReport.initCrashReport(getApplicationContext(), Constant.BUGLY_KEY, false);
            }
        }
    }
    public static void start(Context c){
        c.startService(new Intent(c,LazyLoadService.class));
    }
}
