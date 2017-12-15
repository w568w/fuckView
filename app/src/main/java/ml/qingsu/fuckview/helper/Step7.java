package ml.qingsu.fuckview.helper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ml.qingsu.fuckview.wizard_library.WizardStep;


/**
 * Created by Administrator on 2017-7-6.
 */

public class Step7 extends WizardStep {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Helper.getTextView(inflater.getContext(), "重要提示", "悬浮窗对很多控件支持并没有预期的那么好，建议使用长按或者通知栏来标记。");
    }
}
