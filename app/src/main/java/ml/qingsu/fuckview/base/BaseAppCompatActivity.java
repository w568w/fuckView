package ml.qingsu.fuckview.base;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by w568w on 18-4-2.
 *
 * @author w568w
 */

public abstract class BaseAppCompatActivity extends AppCompatActivity {
    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        try {
            super.startActivity(intent, options);
        } catch (ActivityNotFoundException e) {
            CrashReport.postCatchedException(e);
        }
    }
}
