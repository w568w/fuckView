package ml.qingsu.fuckview.ui.popups.guide;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.base.BasePopupWindow;
import ml.qingsu.fuckview.models.PageEvent;
import ml.qingsu.fuckview.utils.ViewUtils;

/**
 * Created by w568w on 18-5-13.
 *
 * @author w568w
 */

public class GuidePopupToast extends BasePopupWindow {
    private TextView mText;
    private String[] mSteps;

    public enum Page {
        WELCOME, FORCE_STOP, STARTING_APP, APP_STARTED, CLICKED, MARKED, HIDE
    }

    public GuidePopupToast(Activity activity) {
        super(activity);
        EventBus.getDefault().register(this);
        setFocusable(false);
    }

    @Override
    protected void onHide() {
        super.onHide();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected View onCreateView(Context context) {
        final int screenWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
        View layout = LayoutInflater.from(context).inflate(R.layout.guide_toasts, null);
        mText = (TextView) layout.findViewById(R.id.guide_info);
        mSteps = context.getResources().getStringArray(R.array.guide);
        //Set animation
        ViewUtils.setOnFinishLoadListener(layout, new ViewUtils.OnFinishLoadListener() {
            @Override
            public void onFinishedLoad(View view) {
                ValueAnimatorCompat animator = AnimatorCompatHelper.emptyValueAnimator();
                animator.setDuration(1000);
                animator.addUpdateListener(new AnimatorUpdateListenerCompat() {
                    @Override
                    public void onAnimationUpdate(ValueAnimatorCompat animation) {
                        setX((int) ((1 - animation.getAnimatedFraction()) * screenWidth));
                    }
                });
                animator.start();
            }
        });
        goTo(Page.WELCOME.ordinal());
        return layout;
    }

    private void goTo(int step) {
        if (step < mSteps.length) {
            mText.setText(mSteps[step]);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void brocastGoto(PageEvent pageEvent) {
        if (pageEvent.page == Page.HIDE.ordinal()) {
            hide();
        } else {
            goTo(pageEvent.page);
        }
    }

    @Override
    protected int getGravity() {
        return Gravity.TOP;
    }
}
