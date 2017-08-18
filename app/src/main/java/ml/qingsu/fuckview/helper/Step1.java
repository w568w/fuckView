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

public class Step1 extends WizardStep {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Helper.getTextView(inflater.getContext(), "欢迎", "非常感谢你选择了净眼，这是一款可以让你屏蔽任何应用UI控件的工具。我们希望能帮助您清理那些眼不见为净的东西，如广告、热门推荐等。下一步来获取更多信息。");
    }
}
