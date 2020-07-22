package ml.qingsu.fuckview.ui.popups;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.base.BasePopupWindow;
import ml.qingsu.fuckview.hook.ViewReceiver;
import ml.qingsu.fuckview.models.PageEvent;
import ml.qingsu.fuckview.models.ViewCaptureEvent;
import ml.qingsu.fuckview.models.ViewModel;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.ui.popups.guide.GuidePopupToast;
import ml.qingsu.fuckview.utils.FirstRun;
import ml.qingsu.fuckview.utils.PackageUtils;
import ml.qingsu.fuckview.utils.dumper.ViewDumper;
import ml.qingsu.fuckview.utils.dumper.ViewDumperProxy;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by w568w on 2017-7-12.
 *
 * @author w568w
 */

public class FloatingPopupView extends BasePopupWindow {


    private ArrayList<ViewDumper.ViewItem> mList;
    private final String mPackageName;
    private int mGravity = Gravity.TOP;
    private BasePopupWindow mFullScreenPopupWindow;
    private boolean isNotList;
    private Button mRefresh;
    private ProgressBar mClosingProgress;
    private TextView mInfo;
    private Button mDetail;

    public FloatingPopupView(Activity activity, String pkg) {
        super(activity);
        mPackageName = pkg;
        isNotList = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("no_list", true);
        boolean isLayoutParsingEnabled = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("enable_layout_parse", false);
        if (isLayoutParsingEnabled) {
            mRefresh.setVisibility(View.VISIBLE);
        } else {
            mRefresh.setVisibility(View.GONE);
        }
    }

    @Override
    protected View onCreateView(final Context context) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dump_viewer_view, null);
        mRefresh = (Button) layout.findViewById(R.id.dump_refresh);
        mInfo = (TextView) layout.findViewById(R.id.dump_info);
        mClosingProgress = (ProgressBar) layout.findViewById(R.id.dump_progress);
        mDetail = (Button) layout.findViewById(R.id.dump_detail);
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
                if (mFullScreenPopupWindow != null) {
                    mFullScreenPopupWindow.hide();
                }
                mInfo.setText(R.string.force_closing);
                mClosingProgress.setVisibility(View.VISIBLE);
                try {
                    PackageUtils.asyncStopProcess(mPackageName, new Runnable() {
                        @Override
                        public void run() {
                            hide();
                            mClosingProgress.setVisibility(View.GONE);
                            MainActivity.writePreferences("", Constant.PACKAGE_NAME_NAME);
                            //Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag.
                            appContext.startActivity(new Intent(appContext, MainActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK));

                            EventBus.getDefault().post(new PageEvent(GuidePopupToast.Page.HIDE.ordinal()));
                        }
                    }, mInfo);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGravity = (mGravity == Gravity.TOP ? Gravity.BOTTOM : Gravity.TOP);
                updateLayout();
            }
        });
        mDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO :Show a dialog with detail
                try {
                    ViewModel model = (ViewModel) mInfo.getTag();
                    model.save();
                    appContext.sendBroadcast(new Intent(ViewReceiver.ACTION).putExtra("path", model.getPath()));
                    Toast.makeText(appContext, R.string.rule_saved, Toast.LENGTH_SHORT).show();
                    mInfo.setTag(null);
                    mInfo.setText("");
                    mInfo.setBackgroundDrawable(null);
                    mDetail.setVisibility(View.GONE);
                    EventBus.getDefault().post(new PageEvent(GuidePopupToast.Page.MARKED.ordinal()));
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
        EventBus.getDefault().register(this);
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
            } else {
                mRefresh.setText(R.string.parsing_view);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            boolean forceRoot = PreferenceManager.getDefaultSharedPreferences(appContext).getBoolean("force_root", false);
            mList = forceRoot ? ViewDumper.parseCurrentView() : ViewDumperProxy.parseCurrentView(getActivity());
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


            if (isNotList) {
                mFullScreenPopupWindow = new FullScreenPopupWindow(getActivity(), mList, mPackageName, FloatingPopupView.this);
            } else {
                if (FirstRun.isFirstRun(appContext, "list_popup")) {
                    Toast.makeText(appContext, R.string.mark_tip, Toast.LENGTH_LONG).show();
                }
                mFullScreenPopupWindow = new FullScreenListPopupWindow(getActivity(), mList, mPackageName, FloatingPopupView.this);
            }
            mFullScreenPopupWindow.show();
            hide();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCaptureView(ViewCaptureEvent event) {
        Bundle intent = event.bundle;
        ViewModel viewModel = ViewModel.fromString(intent.getString("record"));

        if (viewModel != null) {
            if (event.bitmap != null) {
                mInfo.setBackgroundDrawable(new BitmapDrawable(event.bitmap));
            }
            mInfo.setTag(viewModel);
            mInfo.setText(viewModel.className);
            mDetail.setVisibility(View.VISIBLE);
            EventBus.getDefault().post(new PageEvent(GuidePopupToast.Page.CLICKED.ordinal()));
        }
    }

}
