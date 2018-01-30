package ml.qingsu.fuckview.ui.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.ui.fragments.select_app.SelectAppWizard;

/**
 * Created by w568w on 18-1-6.
 */

public class WelcomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.welcome_fragment, null);
        layout.findViewById(R.id.welcome_guide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://w568.wodemo.net/entry/467891")));
                } catch (ActivityNotFoundException a) {
                    a.printStackTrace();
                    Toast.makeText(getActivity(), getString(R.string.unsupport_of_package), Toast.LENGTH_SHORT).show();
                }
            }
        });
        layout.findViewById(R.id.welcome_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).setFragmentWithoutBack(new SelectAppWizard());
            }
        });
        return layout;
    }
}
