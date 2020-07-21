package ml.qingsu.fuckview.provider;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

import static ml.qingsu.fuckview.Constant.ACTIVITY_NAME;

public class PreferenceProvider extends RemotePreferenceProvider {
    public PreferenceProvider() {
        super(ACTIVITY_NAME, new String[]{"data"});
    }
}
