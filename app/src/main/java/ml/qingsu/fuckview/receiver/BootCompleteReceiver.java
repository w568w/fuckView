package ml.qingsu.fuckview.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ml.qingsu.fuckview.ui.activities.OnePixelActivity;


/**
 * Created by w568w on 18-5-13.
 *
 * @author w568w
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            OnePixelActivity.startFuck(context.getApplicationContext());
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            OnePixelActivity.killFuck();
        }
    }
}
