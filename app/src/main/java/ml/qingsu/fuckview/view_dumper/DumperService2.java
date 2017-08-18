package ml.qingsu.fuckview.view_dumper;


import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by w568w on 2017-8-1.
 */

public class DumperService2 extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            Class<?> clazz = Class.forName("android.view.accessibility.AccessibilityInteractionClient");
            Method instance = clazz.getMethod("getInstance");
            Method find = clazz.getMethod("findAccessibilityNodeInfoByAccessibilityId", int.class, int.class, int.class);
            Object client = instance.invoke(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {

    }
}
