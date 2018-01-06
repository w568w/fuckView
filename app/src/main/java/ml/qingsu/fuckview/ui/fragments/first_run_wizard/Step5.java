package ml.qingsu.fuckview.ui.fragments.first_run_wizard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ml.qingsu.fuckview.utils.wizard.WizardStep;

/**
 * Created by Administrator on 2017-7-6.
 */

public class Step5 extends WizardStep {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Helper.getTextView(inflater.getContext(), "权限", "净眼 需要储存、悬浮窗以及Root权限。另外，它所劫持的应用需要通知栏权限和悬浮窗权限，因此请允许权限请求，否则屏蔽功能不能正常工作。");
    }
}
