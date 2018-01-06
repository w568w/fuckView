package ml.qingsu.fuckview.utils.view_dumper;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by w568w on 2017-8-1.
 */
public class DumperService extends AccessibilityService {
    private static DumperService instance;

    public static DumperService getInstance() {
        return instance;
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
            return null;
        }

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //NOTHING
    }

    @Override
    public void onInterrupt() {
        //NOTHING
    }
}
