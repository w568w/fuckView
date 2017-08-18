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

public class Step4 extends WizardStep {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return Helper.getTextView(inflater.getContext(), "实验性", "净眼 仍处于实验性阶段，可能不太稳定。如果遇到崩溃，希望您能将尽可能多的信息发送给我们，以帮助我们尽快地增强其稳定性。\n注意：请最好不要直接在评论区发布崩溃信息，我们不能及时收到并处理它。如有必要，请私信开发者，这样我们能更快地解决问题。");
    }
}
