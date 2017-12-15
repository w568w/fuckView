package ml.qingsu.fuckview.first_run_library;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * w568w on 2017-6-30.
 * It's the most useless.It's the most useful.
 */

public class FirstRun {
    public static boolean isFirstRun(Context appContext, String tag) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        boolean firstRun = preferences.getBoolean(tag, true);
        preferences.edit().putBoolean(tag, false).apply();
        return firstRun;
    }
}
