package ml.qingsu.fuckview.ui.fragments.guide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.models.PageEvent;
import ml.qingsu.fuckview.utils.wizard.BaseWizard;
import ml.qingsu.fuckview.utils.wizard.WizardStep;

/**
 * Created by w568w on 18-4-9.
 *
 * @author w568w
 */

public class GuideFragment extends BaseWizard {

    /**
     * Use LinkedHashMap to keep the order
     */
    private static LinkedHashMap<String, String> sList = new LinkedHashMap<>();

    static {
        sList.put("开始旅程!", "欢迎使用!\n本教程会带您一步步使用净眼，现在您可以点击\"下一步\"");
        sList.put("第一步", "选择您的App并启动。");
        sList.put("第二步", "很好！接下来，您应该能看到下方有一个悬浮窗口。\n点击您要屏蔽的控件。");
    }

    @Override
    protected Settings getSettings() {
        return new Settings(getResources().getString(R.string.prev_step), getResources().getString(R.string.next_step), getString(R.string.finish_guide),
                initSteps());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void pageTo(PageEvent event) {
        scrollTo(event.page);
    }

    private WizardStep[] initSteps() {
        ArrayList<WizardStep> steps = new ArrayList<>();
        Iterator<String> iter = sList.keySet().iterator();
        while (iter.hasNext()) {
            String title = iter.next();
            Step step = new Step();
            step.setText(title, sList.get(title));
            steps.add(step);
        }
        return steps.toArray(new WizardStep[0]);
    }

}
