package ml.qingsu.fuckview.view_dumper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by w568w on 2017-8-1.
 */
public class DumperService extends AccessibilityService {
    private static DumperService instance;
    private static String activityName=null;

    public static DumperService getInstance() {
        return instance;
    }

    public String getActivityName() {
        return activityName;
    }

    @Override
    protected void onServiceConnected() {
        instance = this;
        super.onServiceConnected();
    }

    @Override
    public AccessibilityNodeInfo getRootInActiveWindow() {
        try {
            return super.getRootInActiveWindow();
        } catch (Throwable ignored) {
        }
        return null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //NOTHING
        int type = accessibilityEvent.getEventType();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:

                String pkg = accessibilityEvent.getPackageName() == null ? "" : accessibilityEvent.getPackageName().toString();
                if(!pkg.equals("ml.qingsu.fuckview"))
                    activityName = accessibilityEvent.getClassName().toString();
                break;
        }

    }

    @Override
    public void onInterrupt() {
        //NOTHING
    }
}
