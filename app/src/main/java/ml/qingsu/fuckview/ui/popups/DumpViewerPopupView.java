package ml.qingsu.fuckview.ui.popups;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.utils.first_run.FirstRun;
import ml.qingsu.fuckview.utils.view_dumper.ViewDumper;
import ml.qingsu.fuckview.utils.view_dumper.ViewDumperProxy;

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
        final Button close = (Button) layout.findViewById(R.id.dump_close);
        final Button top = (Button) layout.findViewById(R.id.dump_top);


        setFocusable(false);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //一个null会被误认为Runnable
                new refreshTask().execute(null, null);
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
            appContext.unregisterReceiver(mReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    @Override
    protected int getGravity() {
        return mGravity;
    }

    private class refreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mRefresh.getText().toString().equals("解析中...")) {
                cancel(true);
                return;
            }
            mRefresh.setText("解析中...");
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
            mRefresh.setText("刷新解析");

            if (mList == null || mList.isEmpty()) {
                Toast.makeText(appContext, "解析失败!您启用 净眼 的辅助服务了吗?\n\n启用后请强制停止应用重来！\n启用后请强制停止应用重来！\n启用后请强制停止应用重来！\n重要的事说三遍", Toast.LENGTH_LONG).show();
                return;
            }


            if (isNotList)
                mFullScreenPopupWindow = new FullScreenPopupWindow(getActivity(), mList, mPackageName, DumpViewerPopupView.this);
            else {
                if (FirstRun.isFirstRun(appContext, "list_popup"))
                    Toast.makeText(appContext, "长按显示菜单,短按定位控件", Toast.LENGTH_LONG).show();
                mFullScreenPopupWindow = new FullScreenListPopupWindow(getActivity(), mList, mPackageName, DumpViewerPopupView.this);
            }
            mFullScreenPopupWindow.show();
            hide();
        }
    }

    private class HookBrocastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                int height=intent.getIntExtra("height",0);
                int width=intent.getIntExtra("width",0);
                Log.d("jy","Get a cast!");
                Toast.makeText(context, "Get it!-->"+intent.getStringExtra("className"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
