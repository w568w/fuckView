package ml.qingsu.fuckview.utils.dumper;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;

/**
 * Created by w568w on 2017-8-1.
 */

public class ViewDumperProxy {
    public static synchronized ArrayList<ViewDumper.ViewItem> parseCurrentView(Context context) {
        if (Build.VERSION.SDK_INT < 16) {
            return ViewDumper.parseCurrentView();
        } else {
            return ViewDumper.parseCurrentViewAbove16(context);
        }
    }
}
