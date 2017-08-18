package ml.qingsu.fuckview;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

/**
 * Created by w568w on 2017-7-12.
 */


abstract class GlobalPopupWindow {
    private Activity activity;
    Context appContext;
    private WindowManager mWindowManager = null;
    WindowManager.LayoutParams params;
    private final View view;
    private boolean isShown = false;

    protected abstract View onCreateView(Context context);

    protected abstract int getGravity();

    GlobalPopupWindow(Activity activity) {
        this.activity = activity;
        this.appContext = activity.getApplicationContext();
        mWindowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = getGravity();
        view = onCreateView(appContext);
    }

    public void setFocusable(boolean focusable) {
        params.flags = focusable ? FLAG_NOT_TOUCH_MODAL : FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL;
        try {
            if (isShown)
                mWindowManager.updateViewLayout(view, params);
        } catch (Exception ignored) {
        }
    }

    public void updateLayout() {
        params.gravity = getGravity();
        try {
            mWindowManager.updateViewLayout(view, params);
        } catch (Exception ignored) {
        }
    }

    public final void show() {
        if (isShown) return;
        isShown = true;
        mWindowManager.addView(view, params);
    }

    public final void hide() {
        if (!isShown) return;
        isShown = false;
        mWindowManager.removeView(view);
    }

    protected final Activity getActivity() {
        return activity;
    }
}
