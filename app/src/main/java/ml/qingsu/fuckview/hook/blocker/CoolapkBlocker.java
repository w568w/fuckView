package ml.qingsu.fuckview.hook.blocker;

import android.view.View;

import static ml.qingsu.fuckview.Constant.Ad.COOLAPK_AD;
import static ml.qingsu.fuckview.Constant.Ad.COOLAPK_AD_LAYOUT_TYPE;
import static ml.qingsu.fuckview.Constant.COOLAPK_MARKET_PKG_NAME;

/**
 * Created by w568w on 18-5-6.
 *
 * @author w568w
 */

public class CoolapkBlocker extends AbstractViewBlocker {
    private static CoolapkBlocker instance;

    public static CoolapkBlocker getInstance() {
        if (instance == null) {
            instance = new CoolapkBlocker();
        }
        return instance;
    }

    @Override
    public boolean shouldBlock(View view, String id, String className) {
        if (!COOLAPK_MARKET_PKG_NAME.equals(view.getContext().getPackageName())) {
            return false;
        }
        if (COOLAPK_AD_LAYOUT_TYPE.equals(className) &&
                id.contains(COOLAPK_AD)) {
            return true;
        }
        //todo more rules
        return false;
    }
}