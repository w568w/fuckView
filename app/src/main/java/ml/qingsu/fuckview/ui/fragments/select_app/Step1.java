package ml.qingsu.fuckview.ui.fragments.select_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ml.qingsu.fuckview.implement.ListFilter;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.utils.Lists;
import ml.qingsu.fuckview.utils.ShellUtils;
import ml.qingsu.fuckview.utils.wizard.WizardStep;

/**
 * Created by w568w on 17-6-18.
 */
public class Step1 extends WizardStep implements Searchable {
    TextView mText;
    ListView mList;
    int mSelectPosition;
    public static AppInfo sSelected;
    String searchText = "";
    ArrayList<AppInfo> mAppList;
    ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.select_app, null);
        mText = (TextView) ll.findViewById(R.id.step1_textView);
        mList = (ListView) ll.findViewById(R.id.step1_listView);
        ll.setPadding(5, 5, 5, 5);
        registerForContextMenu(mList);
        return ll;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, Menu.NONE, R.string.goto_app_detail);
        menu.add(0, 2, Menu.NONE, R.string.force_stop);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!(mList.getAdapter() instanceof AppAdapter)){
            return super.onContextItemSelected(item);
        }
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        AppInfo info = ((AppAdapter) mList.getAdapter()).getList().get(menuInfo.position);
        switch (item.getItemId()) {
            case 1:
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + info.packageName));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mCon, R.string.unsupport_of_package, Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                try {
                    ShellUtils.killProcess(info.packageName);
                    Toast.makeText(mCon, R.string.finish, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCon instanceof MainActivity) {
            ((MainActivity) mCon).shouldShowFAQ = true;
            ((MainActivity) mCon).currentFragment = this;
            //AppCompatActivity类提供了一个可以在API9上使用的invalidateOptionsMenu方法
            //本来这个方法在Activity类上需要API11的
            //所以需要转型
            //(PS:呆萌的AS告诉我转型是多余的2333)
            ((AppCompatActivity) mCon).invalidateOptionsMenu();

        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            singleThreadPool.execute(new LoadTask());
        }
    }

    @Override
    public void setSearchText(String text) {
        searchText = text;
        if (mList == null) {
            return;
        }
        if (mList.getAdapter() instanceof AppAdapter) {
            AppAdapter adapter = (AppAdapter) mList.getAdapter();
            if ("".equals(text)) {
                adapter.setList(mAppList);
            } else {
                adapter.setList(Lists.filter(mAppList, new ListFilter<AppInfo>() {
                    @Override
                    public boolean filter(AppInfo object) {
                        return object.appName.toLowerCase().contains(searchText.toLowerCase());
                    }
                }));
            }
        }
    }

    private class AppAdapter extends BaseAdapter {
        private List<AppInfo> mAppList;

        AppAdapter(List<AppInfo> a) {
            mAppList = a;
        }

        public void setList(List<AppInfo> a) {
            mAppList = a;
            notifyDataSetChanged();
        }
        public List<AppInfo> getList(){
            return mAppList;
        }
        @Override
        public int getCount() {
            return mAppList.size();
        }

        @Override
        public Object getItem(int i) {
            return mAppList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null || !(view instanceof ViewGroup)) {
                view = getActivity().getLayoutInflater().inflate(R.layout.radiobutton, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) view.findViewById(R.id.textView);
                viewHolder.select = (RadioButton) view.findViewById(R.id.radioButton);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.name.setText("    " + mAppList.get(i).appName);
            mAppList.get(i).appIcon.setBounds(0, 0, 64, 64);
            viewHolder.name.setCompoundDrawables(mAppList.get(i).appIcon, null, null, null);
            if (mSelectPosition == i) {
                viewHolder.select.setChecked(true);

            } else {
                viewHolder.select.setChecked(false);
            }
            return view;
        }

        public class ViewHolder {
            TextView name;
            RadioButton select;
        }
    }

    public class AppInfo {
        public String appName = "";
        public String packageName = "";
        public String versionName = "";
        public int versionCode = 0;
        public Drawable appIcon = null;
        public PackageInfo packageInfo;
    }

    public class LoadTask extends Thread{
        @Override
        public void run() {
            final Context act = mCon;
            mAppList = new ArrayList<>();
            PackageManager pm = act.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            if (packages == null || packages.size() == 0) {
                mCon.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(act, getString(R.string.no_app_list_permission), Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                //TODO somethings Terrible
                //无法在MIUI上读取系统应用
                if (getArguments() != null && !getArguments().containsKey("sys")) {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                        continue;
                    }
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) {
                        continue;
                    }
                }
                AppInfo tmpInfo = new AppInfo();
                tmpInfo.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                tmpInfo.packageName = packageInfo.packageName;
                tmpInfo.versionName = packageInfo.versionName;
                tmpInfo.versionCode = packageInfo.versionCode;
                tmpInfo.packageInfo = packageInfo;
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(pm);
                mAppList.add(tmpInfo);
            }
            AppInfo[] s = mAppList.toArray(new AppInfo[0]);
            try {
                Arrays.sort(s, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo appInfo, AppInfo t1) {
                        return appInfo.appName.compareTo(t1.appName);
                    }
                });
            } catch (IllegalArgumentException e) {
                //So we will not sort them,OK?
                e.printStackTrace();
            }
            mAppList = new ArrayList<>(Arrays.asList(s));
            while (mList == null) {

            }
            mList.post(new Runnable() {
                @Override
                public void run() {
                    final AppAdapter aa = new AppAdapter(mAppList);
                    mList.setAdapter(aa);
                    mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if(!(parent.getAdapter() instanceof AppAdapter)){
                                return;
                            }
                            mSelectPosition = position;
                            sSelected = ((AppAdapter) parent.getAdapter()).getList().get(position);
                            //自残？
                            if (sSelected.packageName.equals(mList.getContext().getPackageName())) {
                                mSelectPosition = 0;
                                sSelected = ((AppAdapter) parent.getAdapter()).getList().get(0);
                                Toast.makeText(act, getString(R.string.dont_mark_myself), Toast.LENGTH_SHORT).show();
                            }
                            aa.notifyDataSetChanged();
                        }
                    });
                    mSelectPosition = 0;
                    aa.notifyDataSetChanged();
                    sSelected = mAppList.get(0);
                }
            });
        }
    }
}