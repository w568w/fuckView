package ml.qingsu.fuckview.wizard;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ml.qingsu.fuckview.MainActivity;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.Searchable;
import ml.qingsu.fuckview.wizard_library.WizardStep;

import static ml.qingsu.fuckview.MyApplication.con;

/**
 * Created by w568w on 17-6-18.
 * 这代码没啥可看的，走吧走吧
 */
public class Step1 extends WizardStep implements Searchable {
    TextView tv;
    ListView lv;
    int selectPosition;
    public static AppInfo selected;
    String searchText = "";
    ArrayList<AppInfo> appList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.step1, null);
        tv = (TextView) ll.findViewById(R.id.step1_textView);
        lv = (ListView) ll.findViewById(R.id.step1_listView);
        ll.setPadding(5, 5, 5, 5);
        registerForContextMenu(lv);
        return ll;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, Menu.NONE, "转到应用信息页面");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        AppInfo info = appList.get(menuInfo.position);
        switch (item.getItemId()) {
            case 1:
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + info.packageName));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mCon, "抱歉,您的设备不支持此选项", Toast.LENGTH_SHORT).show();
                }
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Context act = con;
                    appList = new ArrayList<>();
                    PackageManager pm = act.getPackageManager();
                    List<PackageInfo> packages = pm.getInstalledPackages(0);
                    if (packages == null || packages.size() == 0) {
                        mCon.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(act, "未获得读取应用权限", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }
                    for (int i = 0; i < packages.size(); i++) {
                        PackageInfo packageInfo = packages.get(i);
                        if (getArguments() != null && !getArguments().containsKey("sys")) {
                            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
                                continue;
                            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
                                continue;
                        }
                        AppInfo tmpInfo = new AppInfo();
                        tmpInfo.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                        tmpInfo.packageName = packageInfo.packageName;
                        tmpInfo.versionName = packageInfo.versionName;
                        tmpInfo.versionCode = packageInfo.versionCode;
                        tmpInfo.packageInfo = packageInfo;
                        tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(pm);
                        appList.add(tmpInfo);
                    }
                    AppInfo[] s = appList.toArray(new AppInfo[0]);
                    Arrays.sort(s, new Comparator<AppInfo>() {
                        @Override
                        public int compare(AppInfo appInfo, AppInfo t1) {
                            return appInfo.appName.compareTo(t1.appName);
                        }
                    });
                    appList = new ArrayList<>(Arrays.asList(s));
                    final ArrayList<AppInfo> finalAppList = appList;
                    while (lv == null) ;
                    lv.post(new Runnable() {
                        @Override
                        public void run() {
                            final AppAdapter aa = new AppAdapter(finalAppList);
                            lv.setAdapter(aa);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    selectPosition = position;
                                    selected = finalAppList.get(position);
                                    //自残？
                                    if (selected.packageName.equals(lv.getContext().getPackageName())) {
                                        selectPosition = 0;
                                        selected = finalAppList.get(0);
                                        Toast.makeText(con, "大坏淫!我不会自残的~(╯‵□′)╯︵┻━┻", Toast.LENGTH_SHORT).show();
                                    }
                                    aa.notifyDataSetChanged();
                                }
                            });
                            selectPosition = 0;
                            aa.notifyDataSetChanged();
                            selected = finalAppList.get(0);
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void setSearchText(String text) {
        searchText = text;
        if (lv == null) return;
        if (lv.getAdapter() instanceof BaseAdapter) {
            ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
        }
    }

    private class AppAdapter extends BaseAdapter {
        private ArrayList<AppInfo> al;

        AppAdapter(ArrayList<AppInfo> a) {
            al = a;

        }

        @Override
        public int getCount() {
            return al.size();
        }

        @Override
        public Object getItem(int i) {
            return al.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

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
            viewHolder.name.setText(al.get(i).appName);
            al.get(i).appIcon.setBounds(0, 0, 40, 40);
            viewHolder.name.setCompoundDrawables(al.get(i).appIcon, null, null, null);
            if (selectPosition == i) {
                viewHolder.select.setChecked(true);

            } else {
                viewHolder.select.setChecked(false);
            }
            if (!searchText.equals("") && !al.get(i).appName.toLowerCase().contains(searchText.toLowerCase())) {
                view.setVisibility(View.GONE);
                view = new View(con);
            } else {
                view.setVisibility(View.VISIBLE);
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
}