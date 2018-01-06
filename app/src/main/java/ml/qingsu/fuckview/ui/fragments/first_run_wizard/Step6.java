package ml.qingsu.fuckview.ui.fragments.first_run_wizard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ml.qingsu.fuckview.utils.wizard.WizardStep;


/**
 * Created by Administrator on 2017-7-8.
 */

public class Step6 extends WizardStep {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Helper.getTextView(inflater.getContext(), "快速指南", "1. 强制停止要标记的应用\n2. 选择应用，开始标记\n3. 长按或从悬浮窗中选择，屏蔽要标记的控件\n4. 再次强制停止要标记的应用，使屏蔽生效");
    }
}