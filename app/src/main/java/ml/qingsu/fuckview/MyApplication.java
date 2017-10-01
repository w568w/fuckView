package ml.qingsu.fuckview;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


/**
 * Created by w568w on 2017-6-30.
 */

public class MyApplication extends Application {
    public static Context con;

    @Override
    public void onCreate() {
        super.onCreate();
        //FIR.init(this);
        con = this;
        Fabric.with(this, new Crashlytics());
    }
}
