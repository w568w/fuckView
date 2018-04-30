package ml.qingsu.fuckview.ui.popups;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.base.BasePopupWindow;

/**
 * Created by w568w on 18-4-29.
 *
 * @author w568w
 */

public class GuidePopupWindow extends BasePopupWindow {

    public GuidePopupWindow(Activity activity) {
        super(activity);
    }

    @Override
    protected View onCreateView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.guide_fragment,null);
    }

    @Override
    protected int getGravity() {
        return Gravity.TOP;
    }
}
