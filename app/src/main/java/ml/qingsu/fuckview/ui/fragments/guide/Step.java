package ml.qingsu.fuckview.ui.fragments.guide;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.utils.wizard.WizardStep;

/**
 * Created by w568w on 18-4-12.
 *
 * @author w568w
 */

public class Step extends WizardStep {
    private String mTitle, mInfo;

    public void setText(@StringRes int title, @StringRes int info) {
        mTitle = getString(title);
        mInfo = getString(info);
    }

    public void setText(String title, String info) {
        mTitle = title;
        mInfo = info;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.guide_steps, null);
        TextView title = (TextView) layout.findViewById(R.id.guide_title);
        TextView info = (TextView) layout.findViewById(R.id.guide_info);
        title.setText(mTitle);
        info.setText(mInfo);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
