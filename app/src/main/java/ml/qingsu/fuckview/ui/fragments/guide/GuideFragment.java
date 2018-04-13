package ml.qingsu.fuckview.ui.fragments.guide;

import android.app.AppOpsManager;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.utils.wizard.BaseWizard;
import ml.qingsu.fuckview.utils.wizard.WizardStep;

/**
 * Created by w568w on 18-4-9.
 *
 * @author w568w
 */

public class GuideFragment extends BaseWizard {
    @Override
    protected Settings getSettings() {

        return new Settings(getResources().getString(R.string.prev_step), getResources().getString(R.string.next_step),getString(R.string.finish_guide),
                new WizardStep[]{new Step1()});
    }
}
