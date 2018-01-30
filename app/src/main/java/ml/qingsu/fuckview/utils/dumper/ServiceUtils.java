package ml.qingsu.fuckview.utils.dumper;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/**
 * Created by w568w on 2017-8-1.
 */

public class ServiceUtils {

    public static void openSetting(Context context) {
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }


}
