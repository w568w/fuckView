package ml.qingsu.fuckview.hook.blocker;

import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import ml.qingsu.fuckview.hook.Hook;
import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.models.CoolApkHeadlineModel;
import ml.qingsu.fuckview.utils.ViewUtils;

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
    private static int sFromWhereViewId = -1;
    private static int sTextViewId = -1;
    private static ArrayList<BlockModel> mData;

    public static CoolapkBlocker getInstance() {
        if (instance == null) {

            instance = new CoolapkBlocker();
        }
        return instance;
    }

    public void setList(ArrayList<BlockModel> data) {
        mData = data;
    }

    @Override
    public boolean shouldBlock(View view, String id, String className) {
        if (sFromWhereViewId == -1) {
            sFromWhereViewId = ViewUtils.getId("from_where_view", view.getContext());

        }
        if (sTextViewId == -1) {
            sTextViewId = ViewUtils.getId("text_view", view.getContext());

        }
        if (!COOLAPK_MARKET_PKG_NAME.equals(view.getContext().getPackageName())) {
            return false;
        }
        if (COOLAPK_AD_LAYOUT_TYPE.equals(className) &&
                id.contains(COOLAPK_AD)) {
            return true;
        }
        if ("LinearLayout".equals(className) && id.contains("card_view")) {
            try {
                //获取动态来源
                TextView from = (TextView) view.findViewById(sFromWhereViewId);
                TextView content = (TextView) view.findViewById(sTextViewId);
                String froms = null;
                String contents = null;
                if (from != null) {
                    froms = from.getText().toString();
                }
                if (content != null) {
                    contents = content.getText().toString();
                }
                final int len = mData.size();
                for (int i = 0; i < len; i++) {
                    BlockModel model = mData.get(i);
                    if (model instanceof CoolApkHeadlineModel) {
                        switch (((CoolApkHeadlineModel) model).getType()) {
                            case FROM:
                                if (froms != null && froms.contains(model.text)) {
                                    return true;
                                }
                                break;
                            case CONTENT:
                                if (contents != null && contents.contains(model.text)) {
                                    return true;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

        }
        //todo more rules
        return false;
    }
}