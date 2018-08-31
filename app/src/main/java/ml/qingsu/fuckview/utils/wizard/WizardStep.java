package ml.qingsu.fuckview.utils.wizard;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * @author w568w
 * @date 2017-7-9
 */

public class WizardStep extends Fragment {
    protected Activity mCon;


    public void setContext(Activity mCon) {
        this.mCon = mCon;
    }
}
