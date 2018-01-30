package ml.qingsu.fuckview.ui.fragments;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.utils.OnlineRulesUtils;

/**
 * Created by w568w on 18-1-26.
 */

public class OnlineRulesFragment extends Fragment {
    TextView mInfo;
    Button mDownload;
    ArrayList<MainActivity.BlockModel> mRules;
    SwipeRefreshLayout mLayout;
    ProgressBar mProgressBar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SwipeRefreshLayout layout = (SwipeRefreshLayout) inflater.inflate(R.layout.online_rules_fragment, null);

        mInfo = (TextView) layout.findViewById(R.id.online_rules_info);
        mDownload = (Button) layout.findViewById(R.id.online_rules_download);
        mProgressBar=(ProgressBar)layout.findViewById(R.id.online_rules_progressBar);
        mLayout = layout;
        mDownload.setEnabled(false);

        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRules==null)return;
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
        return layout;
    }

    /**
     * 启动时刷新列表
     */
    @Override
    public void onResume() {
        super.onResume();
        mLayout.setRefreshing(true);
    }

    /**
     * 执行下载规则的任务
     */
    private class RefreshTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDownload.setEnabled(false);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object blockModels) {
            super.onPostExecute(blockModels);
            mLayout.setRefreshing(false);
            if (blockModels instanceof Exception) {

                mInfo.setText(Log.getStackTraceString((Throwable) blockModels));
            } else if (blockModels instanceof ArrayList) {
                mDownload.setEnabled(true);
                mRules = (ArrayList<MainActivity.BlockModel>) blockModels;
                mInfo.setText(String.format(Locale.getDefault(), getString(R.string.online_rules_info), mRules.size(), MainActivity.read().size()));
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

    private class FliterTask extends AsyncTask<ArrayList<MainActivity.BlockModel>,Integer,ArrayList<MainActivity.BlockModel>>{
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
        protected void onPostExecute(ArrayList<MainActivity.BlockModel> blockModels) {
            super.onPostExecute(blockModels);
            mProgressBar.setVisibility(View.INVISIBLE);

            //MainActivity.Write_Preferences("", MainActivity.LIST_NAME);
            //TODO 可能会有重复规则，需要去重
            for (MainActivity.BlockModel bm : blockModels)
                bm.save();
            mLayout.setRefreshing(true);
        }

        @SafeVarargs
        @Override
        protected final ArrayList<MainActivity.BlockModel> doInBackground(ArrayList<MainActivity.BlockModel>... params) {
            ArrayList<MainActivity.BlockModel> models=params[0];
            ArrayList<MainActivity.BlockModel> flitered=new ArrayList<>();
            PackageManager packageManager=getActivity().getPackageManager();
            for (int len = models.size(), i = 0; i < len; i++) {
                publishProgress(i);
                MainActivity.BlockModel blockModel = models.get(i);
                try {
                    packageManager.getApplicationInfo(blockModel.packageName, 0);
                    flitered.add(blockModel);
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }

            return flitered;
        }
    }

}
