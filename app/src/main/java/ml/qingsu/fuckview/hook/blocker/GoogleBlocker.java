package ml.qingsu.fuckview.hook.blocker;

import android.view.View;

import ml.qingsu.fuckview.Constant;

import static ml.qingsu.fuckview.Constant.Ad.GOOGLE_AD_LAYOUT_TYPE;

/**
 * Created by w568w on 18-5-6.
 *
 * @author w568w
 */
public class GoogleBlocker extends AbstractViewBlocker {
    private static GoogleBlocker instance;

    public static GoogleBlocker getInstance() {
        if (instance == null) {
            instance = new GoogleBlocker();
        }

        return instance;
    }

    @Override
    public boolean shouldBlock(View view, String id, String className) {
        if (GOOGLE_AD_LAYOUT_TYPE.equals(className) && id.contains(Constant.Ad.GOOGLE_AD)) {
            return true;
        }
        return false;
    }
}
