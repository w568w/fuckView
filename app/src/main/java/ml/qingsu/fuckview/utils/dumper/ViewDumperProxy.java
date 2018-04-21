package ml.qingsu.fuckview.utils.dumper;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;

/**
 * @author w568w
 * @date 2017-8-1
 */

public class ViewDumperProxy {
    public static synchronized ArrayList<ViewDumper.ViewItem> parseCurrentView(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return ViewDumper.parseCurrentView();
        } else {
            return ViewDumper.parseCurrentViewAbove16(context);
        }
    }
}
