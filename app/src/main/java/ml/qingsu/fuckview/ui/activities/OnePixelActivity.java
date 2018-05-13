package ml.qingsu.fuckview.ui.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import ml.qingsu.fuckview.base.BaseAppCompatActivity;

/**
 * Created by w568w on 18-5-13.
 * 一像素Activity,竭力保持应用存活
 * <p>
 * <p>
 * 技术无罪。
 * ——王欣
 *
 * @author w568w
 */

public class OnePixelActivity extends BaseAppCompatActivity {
    private static OnePixelActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }

    /**
     * 开启保活页面
     */
    public static void startFuck(Context application) {
        Intent intent = new Intent(application.getApplicationContext(), OnePixelActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.getApplicationContext().startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    /**
     * 关闭保活页面
     */
    public static void killFuck() {
        if (instance != null) {
            instance.finish();
        }
    }
}
