package ml.qingsu.fuckview.ui.fragments;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.utils.OnlineRulesUtils;

/**
 * @author w568w
 * @date 18-1-26
 */

public class OnlineRulesFragment extends Fragment {
    TextView mInfo;
    Button mDownload;
    ArrayList<BlockModel> mRules;
    SwipeRefreshLayout mLayout;
    ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.online_rules_fragment, null);

        mInfo = (TextView) layout.findViewById(R.id.online_rules_info);
        mDownload = (Button) layout.findViewById(R.id.online_rules_download);
        mProgressBar = (ProgressBar) layout.findViewById(R.id.online_rules_progressBar);
        mLayout = layout;
        mDownload.setEnabled(false);

        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRules == null) {
                    return;
                }
                mProgressBar.setMax(mRules.size());
                //noinspection unchecked
                new FliterTask().execute(mRules);
            }
        });

        mLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshTask().execute();
            }
        });

        mLayout.findViewById(R.id.online_rules_rl).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_DOWN;
            }
        });
        return layout;
    }

    /**
     * 启动时刷新列表
     */
    @Override
    public void onResume() {
        super.onResume();
        mLayout.setRefreshing(true);
        new RefreshTask().execute();
    }

    /**
     * 执行下载规则的任务
     */
    private class RefreshTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mInfo.setText("");
            mDownload.setEnabled(false);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object blockModels) {
            super.onPostExecute(blockModels);
            //Fragment  OnlineRulesFragment not attached to Activity
            if (!isVisible()) {
                return;
            }
            mLayout.setRefreshing(false);
            if (blockModels instanceof Exception) {
                mInfo.setText(Log.getStackTraceString((Throwable) blockModels));
            } else if (blockModels instanceof ArrayList) {
                mDownload.setEnabled(true);
                mRules = (ArrayList<BlockModel>) blockModels;
                mInfo.setText(String.format(Locale.getDefault(), getString(R.string.online_rules_info), mRules.size(), BlockModel.readModel().size()));
            }
        }

        @Override
        protected Object doInBackground(Void... params) {
            try {
                return OnlineRulesUtils.getOnlineRules();
            } catch (Exception e) {
                return e;
            }
        }
    }

    private class FliterTask extends AsyncTask<ArrayList<BlockModel>, Integer, ArrayList<BlockModel>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<BlockModel> blockModels) {
            super.onPostExecute(blockModels);
            mProgressBar.setVisibility(View.INVISIBLE);

            for (BlockModel bm : blockModels) {
                bm.save();
            }
            //Refresh Rules...
            mLayout.setRefreshing(true);
            new RefreshTask().execute();
        }

        @SafeVarargs
        @Override
        protected final ArrayList<BlockModel> doInBackground(ArrayList<BlockModel>... params) {
            ArrayList<BlockModel> models = params[0];
            ArrayList<BlockModel> flitered = new ArrayList<>();
            ArrayList<BlockModel> originModels = BlockModel.readModel();
            PackageManager packageManager = getActivity().getPackageManager();
            for (int len = models.size(), i = 0; i < len; i++) {
                publishProgress(i);
                BlockModel blockModel = models.get(i);
                if (blockModel == null) {
                    continue;
                }
                try {
                    //去重&检查包名是否存在
                    packageManager.getApplicationInfo(blockModel.packageName, 0);
                    if (!originModels.contains(blockModel)) {
                        flitered.add(blockModel);
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }

            return flitered;
        }
    }

}
