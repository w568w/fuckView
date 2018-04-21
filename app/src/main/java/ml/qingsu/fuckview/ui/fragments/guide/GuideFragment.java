package ml.qingsu.fuckview.ui.fragments.guide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import ml.qingsu.fuckview.R;
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
    private static LinkedHashMap<String, String> sList;

    static {
        sList.put("a", "b");
        sList.put("c", "d");
    }

    @Override
    protected Settings getSettings() {
        return new Settings(getResources().getString(R.string.prev_step), getResources().getString(R.string.next_step), getString(R.string.finish_guide),
                initSteps());
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
