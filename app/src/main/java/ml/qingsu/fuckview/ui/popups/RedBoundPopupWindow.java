package ml.qingsu.fuckview.ui.popups;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsoluteLayout;

import ml.qingsu.fuckview.base.BasePopupWindow;

/**
 * Created by w568w on 2017-7-29.
 */

public class RedBoundPopupWindow extends BasePopupWindow {


    RedBoundPopupWindow(Activity activity, Point point, Point wh) {
        super(activity);
        params.x = point.x;
        params.y = point.y - getStatusBarHeight();
        params.width = wh.x;
        params.height = wh.y;
        setFocusable(false);
    }

    @Override
    protected View onCreateView(Context context) {
        AbsoluteLayout absoluteLayout = new AbsoluteLayout(context);
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(4, Color.RED);
        absoluteLayout.setBackgroundDrawable(gd);
        return absoluteLayout;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = appContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = appContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected int getGravity() {
        return Gravity.TOP | Gravity.LEFT;
    }
}
