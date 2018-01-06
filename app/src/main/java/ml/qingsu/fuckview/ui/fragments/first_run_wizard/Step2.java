package ml.qingsu.fuckview.ui.fragments.first_run_wizard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ml.qingsu.fuckview.utils.wizard.WizardStep;

/**
 * Created by w568w on 2017-7-6.
 */

public class Step2 extends WizardStep {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Helper.getTextView(inflater.getContext(), "框架", "净眼 需要Xposed框架支持。请确定您的框架是否正确安装，并在Xposed Installer中启用本模块，然后选择 软重启 或 重启 以激活。");
    }
}
