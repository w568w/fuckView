package ml.qingsu.fuckview.hook.blocker;

import android.view.View;

import ml.qingsu.fuckview.Constant;

import static ml.qingsu.fuckview.Constant.Ad.GOOGLE_AD_LAYOUT_TYPE;
import static ml.qingsu.fuckview.Constant.Ad.SHARE_THROUGH_AD_LAYOUT_TYPE;

/**
 * Created by w568w on 18-5-6.
 *
 * @author w568w
 */

public class ShareThroughBlocker extends AbstractViewBlocker {
    private static ShareThroughBlocker instance;

    public static ShareThroughBlocker getInstance() {
        if (instance == null) {
            instance = new ShareThroughBlocker();
        }

        return instance;
    }
    @Override
    public boolean shouldBlock(View view, String id, String className) {
        if (SHARE_THROUGH_AD_LAYOUT_TYPE.equals(className) && id.contains(Constant.Ad.SHARE_THROUGH_AD)) {
            return true;
        }
        return false;
    }
}
