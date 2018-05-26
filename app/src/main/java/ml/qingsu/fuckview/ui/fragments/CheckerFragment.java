package ml.qingsu.fuckview.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dalvik.system.PathClassLoader;
import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.ui.popups.FloatingPopupView;
import ml.qingsu.fuckview.utils.ShellUtils;
import ml.qingsu.fuckview.utils.dumper.DumperService;

/**
 * @author w568w
 * @date 18-2-7
 */

public class CheckerFragment extends Fragment {
    ListView mCheckerList;
    ListView mErrorList;
    FrameLayout mStatusContainer;
    ImageView mStatusIcon;
    TextView mStatusText;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.checker_fragment, null);
        mCheckerList = (ListView) layout.findViewById(R.id.checker_list);
        mErrorList = (ListView) layout.findViewById(R.id.checker_error_list);
        mStatusContainer = (FrameLayout) layout.findViewById(R.id.checker_status_container);
        mStatusIcon = (ImageView) layout.findViewById(R.id.checker_status_icon);
        mStatusText = (TextView) layout.findViewById(R.id.checker_module_status);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Show a progress dialog to make users happy.
        ProgressDialog.Builder dialogB = new ProgressDialog.Builder(getActivity());
        AlertDialog dialog = dialogB.setCancelable(false)
                .setMessage(R.string.checking).show();
        fillCheckerList();
        fillErrorList();

        dialog.dismiss();
    }

    private void fillCheckerList() {
        ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
        HashMap<String, String> row = new HashMap<>();
        row.put("item", getString(R.string.check_item_module_enabled));
        row.put("status", bool2Str(MainActivity.isModuleActive()));
        dataList.add(row);

        row = new HashMap<>(2);
        row.put("item", getString(R.string.check_item_floating_permission));
        row.put("status", bool2Str(canShowFloatingWindow()));
        dataList.add(row);


        row = new HashMap<>(2);
        row.put("item", getString(R.string.check_item_service_running));
        row.put("status", bool2Str(DumperService.getInstance() != null && DumperService.getInstance().getRootInActiveWindow() != null));
        dataList.add(row);


        row = new HashMap<>(2);
        row.put("item", getString(R.string.check_item_root));
        row.put("status", bool2Str(ShellUtils.checkRootPermission()));
        dataList.add(row);

        mCheckerList.setAdapter(new SimpleAdapter(getContext(), dataList, R.layout.pairs, new String[]{"item", "status"}, new int[]{R.id.pairs_textView1, R.id.pairs_textView2}));

        int colorResId = MainActivity.isModuleActive() ? R.color.darker_green : R.color.warning;
        int iconResId = MainActivity.isModuleActive() ? R.drawable.ic_check_circle : R.drawable.ic_error;
        int textResId = MainActivity.isModuleActive() ? R.string.module_active : R.string.module_not_active;
        mStatusText.setText(textResId);
        mStatusText.setTextColor(getResources().getColor(colorResId));
        mStatusIcon.setImageResource(iconResId);
        mStatusContainer.setBackgroundColor(getResources().getColor(colorResId));

    }

    private void fillErrorList() {
        ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
        HashMap<String, String> row = new HashMap<>();
        row.put("item", getString(R.string.check_item_method_invoked));
        row.put("status", bool2Str(canInvokeMethod().first));
        dataList.add(row);

        mErrorList.setAdapter(new SimpleAdapter(getContext(), dataList, R.layout.pairs, new String[]{"item", "status"}, new int[]{R.id.pairs_textView1, R.id.pairs_textView2}));
        mErrorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Pair<Boolean, Throwable> booleanThrowablePair = canInvokeMethod();
                        if (!booleanThrowablePair.first) {
                            Toast.makeText(getActivity(), Log.getStackTraceString(booleanThrowablePair.second), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "It's totally okay.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private static String bool2Str(boolean b) {
        return b ? "OK" : "Failed";
    }

    private boolean canShowFloatingWindow() {
        try {
            FloatingPopupView floatingPopupView = new FloatingPopupView(getActivity(), "");
            floatingPopupView.show();
            floatingPopupView.hide();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private Pair<Boolean, Throwable> canInvokeMethod() {
        try {
            Context context = getActivity();
            PackageManager manager = context.getPackageManager();
            List<PackageInfo> list = manager.getInstalledPackages(0);
            int size = list.size();
            String pkgPath = null;
            for (int i = 0; i < size; i++) {
                PackageInfo info = list.get(i);
                if (Constant.PKG_NAME.equals(info.packageName)) {
                    pkgPath = info.applicationInfo.sourceDir;
                    break;
                }
            }
            if (pkgPath != null) {
                PathClassLoader loader = new PathClassLoader(pkgPath, ClassLoader.getSystemClassLoader());
                Class<?> hookerClz = Class.forName(Constant.PKG_NAME + ".hook.Hook", true, loader);
                Object hooker = hookerClz.newInstance();
                Method[] method = hookerClz.getDeclaredMethods();
                for (Method method1 : method) {
                    if ("handleLoadPackage".equals(method1.getName())) {
                        return new Pair<>(true, null);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return new Pair<>(false, e);
        }
        final Throwable t = new NoSuchMethodException("Can't find handleLoadPackage() method.");
        return new Pair<>(false, t);
    }
}
