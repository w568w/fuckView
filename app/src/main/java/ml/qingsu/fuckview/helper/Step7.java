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
        return Helper.getTextView(inflater.getContext(), ":(", "净眼 暂时不能屏蔽以下控件，这是由于它们动态生成，且非常难以捕捉:\n\n1.列表项\n2.Tab选项夹\n3.菜单项\n4.一些由应用恶意逃避监管而建立的奇葩控件\n\n注意:这并不意味着它们无法被净眼屏蔽，请关注应用商店评论区，可能会有屏蔽规则分享");
    }
}
