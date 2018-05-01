package ml.qingsu.fuckview.utils;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

/**
 * Created by w568w on 18-3-2.
 *
 * @author w568w
 */

public final class ViewUtils {
    public static String getClassName(Class<?> clz) {
        final String name = clz.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
    public static String getViewId(View view){
        try {
            return view.getResources().getResourceName(view.getId());
        }catch (Exception e){
            return "";
        }
    }
    public static String getViewPath(View v) {
        ViewParent viewParent = v.getParent();
        Object object = v;
        final StringBuilder path = new StringBuilder();
        while (viewParent != null) {
            if (viewParent instanceof ViewGroup) {
                final int len = ((ViewGroup) viewParent).getChildCount();
                for (int i = 0; i < len; i++) {
                    final View child = ((ViewGroup) viewParent).getChildAt(i);
                    if (child.equals(object)) {
                        path.append(i).append("|").append(getClassName(child.getClass())).append("/");
                    }
                }
            }
            object = viewParent;
            viewParent = viewParent.getParent();
        }
        path.append("#");
        return path.toString();
    }

    public static String getViewPosition(View view) {
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        return new StringBuilder().append(loc[0]).append(',').append(loc[1]).append("$$").toString();
    }

    public static String getText(View view) {
        if (view == null) {
            return "";
        }
        if (view instanceof TextView) {
            return ((TextView) view).getText().toString();
        }
        if (view instanceof ViewGroup) {
            final int len = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < len; i++) {
                final View child = ((ViewGroup) view).getChildAt(i);
                if (child instanceof TextView) {
                    return ((TextView) child).getText().toString().replace("\n", "");
                }
            }
        }
        return "";
    }

    /**
     * Also @see ml.qingsu.fuckview.ui.popups.DumpViewerPopupView#getAllText(View)
     */
    public static String getAllText(View view) {
        final StringBuilder allText = new StringBuilder();
        if (view == null) {
            return "";
        }
        if (view instanceof TextView) {
            return ((TextView) view).getText().toString();
        }
        if (view instanceof ViewGroup) {
            final int len = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < len; i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                if (child instanceof TextView) {
                    if (allText.length() != 0) {
                        allText.append(((TextView) child).getText().toString().replace("\n", "")).append("|");
                    }
                }
                if (child instanceof ViewGroup) {
                    allText.append(getAllText(child));
                }
            }
        }
        return allText.toString();
    }

}
