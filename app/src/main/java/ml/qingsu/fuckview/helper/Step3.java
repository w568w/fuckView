package ml.qingsu.fuckview.helper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ml.qingsu.fuckview.wizard_library.WizardStep;

/**
 * Created by w568w on 2017-7-6.
 */

public class Step3 extends WizardStep {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Helper.getTextView(inflater.getContext(), "原理", "净眼 通过劫持其他应用的界面组件的构造器方法，并插入自身代码使其不能显示，以达到屏蔽某特定控件的效果。");
    }
}
