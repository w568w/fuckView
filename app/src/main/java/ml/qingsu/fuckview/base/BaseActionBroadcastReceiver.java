package ml.qingsu.fuckview.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Choreographer;

/**
 * Created by w568w on 18-3-1.
 *
 * @author w568w
 */

public abstract class BaseActionBroadcastReceiver extends BroadcastReceiver {
    public abstract String getAction();

    public abstract void onReceiving(Context context, Intent intent);

    public final void registerReceiver(Context context) {
        context.registerReceiver(this, new IntentFilter(getAction()));
    }

    public static BaseActionBroadcastReceiver createAndRegisterReceiver(Class<? extends BaseActionBroadcastReceiver> clz, Context context) throws IllegalAccessException, InstantiationException {
        BaseActionBroadcastReceiver broadcastReceiver = clz.newInstance();
        broadcastReceiver.registerReceiver(context);

        return broadcastReceiver;
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (getAction().equals(intent.getAction())) {
            onReceiving(context, intent);

        }
    }
}
