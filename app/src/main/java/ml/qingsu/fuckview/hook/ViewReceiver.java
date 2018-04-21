package ml.qingsu.fuckview.hook;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import ml.qingsu.fuckview.base.BaseActionBroadcastReceiver;
import ml.qingsu.fuckview.utils.ViewUtils;

import static android.view.View.GONE;

/**
 * Created by w568w on 18-3-1.
 *
 * @author w568w
 */

public class ViewReceiver extends BaseActionBroadcastReceiver {
    public static String ACTION = "ilovefuckingtheleftboytilldeath";
    private View mView;

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public void onReceiving(Context context, Intent intent) {
        if (mView != null && ViewUtils.getViewPath(mView).equals(intent.getStringExtra("path"))) {
            mView.setVisibility(GONE);
        }
    }

    public void setView(View mView) {
        this.mView = mView;
    }
}
