package ml.qingsu.fuckview.ui.popups;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.utils.ShellUtils;
import ml.qingsu.fuckview.utils.FirstRun;
import ml.qingsu.fuckview.utils.dumper.ViewDumper;
import ml.qingsu.fuckview.utils.dumper.ViewDumperProxy;

/**
 * Created by w568w on 2017-7-12.
 */

public class DumpViewerPopupView extends GlobalPopupWindow {
    private static final String BROADCAST_ACTION = "tooYoungtooSimple";

    private ArrayList<ViewDumper.ViewItem> mList;
    private final String mPackageName;
    private int mGravity = Gravity.TOP;
    private GlobalPopupWindow mFullScreenPopupWindow;
    private boolean isNotList;
    private Button mRefresh;
    private TextView mInfo;
    private HookBrocastReceiver mReceiver;


    public DumpViewerPopupView(Activity activity, String pkg) {
        super(activity);
        mPackageName = pkg;
        isNotList = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("no_list", true);
        mReceiver = new HookBrocastReceiver();


    }

    @Override
    protected View onCreateView(final Context context) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dump_viewer_view, null);
        mRefresh = (Button) layout.findViewById(R.id.dump_refresh);
        mInfo = (TextView) layout.findViewById(R.id.dump_info);
        final Button close = (Button) layout.findViewById(R.id.dump_close);
        final Button top = (Button) layout.findViewById(R.id.dump_top);

        setFocusable(false);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //一个null会被误认为Runnable
                new RefreshTask().execute(null, null);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFullScreenPopupWindow != null)
                    mFullScreenPopupWindow.hide();
                hide();
            }
        });
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGravity = (mGravity == Gravity.TOP ? Gravity.BOTTOM : Gravity.TOP);
                updateLayout();
            }
        });
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.ViewModel model = (MainActivity.ViewModel) mInfo.getTag();
                    model.save();
                    Toast.makeText(appContext, R.string.rule_saved, Toast.LENGTH_SHORT).show();
                    mInfo.setTag(null);
                    mInfo.setText("");
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        return layout;
    }

    @Override
    protected void onShow() {
        super.onShow();
        appContext.registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION));
    }

    @Override
    protected void onHide() {
        super.onHide();
        try {
            ShellUtils.killProcess(mPackageName);
            MainActivity.Write_Preferences("", MainActivity.PACKAGE_NAME_NAME);
            appContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getGravity() {
        return mGravity;
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mRefresh.getText().toString().equals(appContext.getString(R.string.parsing_view))) {
                cancel(true);
            } else
                mRefresh.setText(R.string.parsing_view);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            boolean force_root = PreferenceManager.getDefaultSharedPreferences(appContext).getBoolean("force_root", false);
            mList = force_root ? ViewDumper.parseCurrentView() : ViewDumperProxy.parseCurrentView(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void vo) {
            super.onPostExecute(vo);
            mRefresh.setText(R.string.parse_view);

            if (mList == null || mList.isEmpty()) {
                Toast.makeText(appContext, R.string.parse_failed, Toast.LENGTH_LONG).show();
                return;
            }


            if (isNotList)
                mFullScreenPopupWindow = new FullScreenPopupWindow(getActivity(), mList, mPackageName, DumpViewerPopupView.this);
            else {
                if (FirstRun.isFirstRun(appContext, "list_popup"))
                    Toast.makeText(appContext, R.string.mark_tip, Toast.LENGTH_LONG).show();
                mFullScreenPopupWindow = new FullScreenListPopupWindow(getActivity(), mList, mPackageName, DumpViewerPopupView.this);
            }
            mFullScreenPopupWindow.show();
            hide();
        }
    }

    private class HookBrocastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int height = intent.getIntExtra("height", 0);
                int width = intent.getIntExtra("width", 0);
                MainActivity.ViewModel viewModel = MainActivity.ViewModel.fromString(intent.getStringExtra("record"));
                mInfo.setTag(viewModel);
                mInfo.setText(context.getString(R.string.click_to_save) + " " + viewModel.getPath());
            }
        }
    }
}
