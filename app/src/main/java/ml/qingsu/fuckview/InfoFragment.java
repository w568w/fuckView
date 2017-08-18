package ml.qingsu.fuckview;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by w568w on 2017-7-7.
 */

public class InfoFragment extends Fragment {
    MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        PackageManager pm = activity.getPackageManager();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.info_fragment, null);
        TextView appName = (TextView) layout.findViewById(R.id.info_app_name);
        TextView packageName = (TextView) layout.findViewById(R.id.info_package_name);
        TextView className = (TextView) layout.findViewById(R.id.info_class_name);
        ImageView icon = (ImageView) layout.findViewById(R.id.info_icon);
        TextView id = (TextView) layout.findViewById(R.id.info_id);

        String pkg = getArguments().getString("pkg");

        String ids = getArguments().getString("id");
        String classNames = getArguments().getString("className");


        appName.setText(MainFragment.getAppTitle(pm, pkg));
        packageName.setText(pkg);
        className.setText(classNames);
        icon.setImageDrawable(MainFragment.getAppIcon(pm, pkg));
        id.setText(ids);

        return layout;
    }
}
