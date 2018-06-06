package ml.qingsu.fuckview.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.AutoTransition;
import android.transition.Explode;
import android.transition.Fade;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.Executors;

import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.ui.fragments.select_app.SelectAppWizard;

/**
 * By w568w on 2017-7-6.
 * @author w568w
 */

public class MainFragment extends Fragment implements Searchable {
    private AppAdapter adapter;
    private Activity context;
    PackageManager pm;
    public static ArrayList<ArrayList<BlockModel>> models = new ArrayList<>();
    ListView listView;
    String searchText = "";

    public static String getAppTitle(PackageManager pm, String packageName) {
        try {
            return pm.getPackageInfo(packageName, 0).applicationInfo.loadLabel(pm).toString();

        } catch (Exception ignored) {
        }
        return packageName;
    }

    public static Drawable getAppIcon(PackageManager pm, String packageName) {
        try {
            Drawable drawable = pm.getPackageInfo(packageName, 0).applicationInfo.loadIcon(pm);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;

        } catch (Exception ignored) {
        }
        return new BitmapDrawable();
    }

    private ArrayList<ArrayList<BlockModel>> fold(ArrayList<BlockModel> flatList) {
        String pkgname = "";
        ArrayList<BlockModel> currentList = null;
        ArrayList<ArrayList<BlockModel>> list = new ArrayList<>();
        for (int i = 0, len = flatList.size(); i < len; i++) {
            BlockModel model = flatList.get(i);
            if (pkgname.equals(model.packageName)) {
                currentList.add(model);
            } else {
                if (currentList == null) {
                    currentList = new ArrayList<>();
                } else {
                    list.add((ArrayList<BlockModel>) currentList.clone());
                    currentList.clear();
                }
                currentList.add(model);
            }
            pkgname = model.packageName;
        }
        return list;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        pm = context.getPackageManager();
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.main_fragment, null);
        listView = (ListView) layout.findViewById(R.id.listView);
        final TextView noItem = (TextView) layout.findViewById(R.id.no_item);
        noItem.setVisibility(View.GONE);

        final android.app.AlertDialog progressDialog = new ProgressDialog.Builder(context)
                .setMessage(R.string.loading).setCancelable(false).show();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<BlockModel> arrayList = BlockModel.readModel();
                try {
                    Collections.sort(arrayList, new Comparator<BlockModel>() {
                        @Override
                        public int compare(BlockModel blockModel, BlockModel t1) {
                            String s1 = blockModel.packageName;
                            String s2 = t1.packageName;
                            //JDK7: RFE: 6804124
                            //Synopsis: Updated sort behavior for Arrays and Collections may throw an IllegalArgumentException
                            if (s1.equals(s2)) {
                                return 0;
                            }
                            return Collator.getInstance(Locale.CHINA).compare(s1, s2);
                        }
                    });
                    models = fold(arrayList);
                    Collections.sort(models, new Comparator<ArrayList<BlockModel>>() {
                        @Override
                        public int compare(ArrayList<BlockModel> lhs, ArrayList<BlockModel> rhs) {
                            String s1 = getAppTitle(pm, lhs.get(0).packageName);
                            String s2 = getAppTitle(pm, rhs.get(0).packageName);
                            //JDK7: RFE: 6804124
                            //Synopsis: Updated sort behavior for Arrays and Collections may throw an IllegalArgumentException
                            if (s1.equals(s2)) {
                                return 0;
                            }
                            return Collator.getInstance(Locale.CHINA).compare(s1, s2);
                        }
                    });
                } catch (IllegalArgumentException e) {
                    //So we do not sort them,OK?
                    e.printStackTrace();
                }
                adapter = new AppAdapter();
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        //If there's no rule...
                        if (models.isEmpty()) {
                            listView.setVisibility(View.GONE);
                            noItem.setVisibility(View.VISIBLE);
                        } else {
                            listView.setAdapter(adapter);
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
        final FloatingActionButton button = (FloatingActionButton) layout.findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectAppWizard tw = new SelectAppWizard();
                Bundle bundle = new Bundle();
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("system_app", false)) {
                    bundle.putBoolean("sys", true);
                }
                tw.setArguments(bundle);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setFragment(tw);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", i);
                SubListFragment subListFragment = new SubListFragment();
                subListFragment.setArguments(bundle);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setFragment(subListFragment);
                }
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean scrollFlag = false;
            boolean isFirst = true;

            int lastVisibleItemPosition;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                scrollFlag = scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!isFirst) {

                    if (firstVisibleItem < lastVisibleItemPosition) {
                        button.show();
                        //执行向上滑动时要做的逻辑
                    } else if (firstVisibleItem > lastVisibleItemPosition) {
                        button.hide();
                    }
                    //记录当前条目
                    lastVisibleItemPosition = firstVisibleItem;

                }
                isFirst = false;

            }
        });

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (context instanceof MainActivity) {
            ((MainActivity) context).shouldShowFAQ = true;
            ((MainActivity) context).currentFragment = this;
            //AppCompatActivity类提供了一个可以在API9上使用的invalidateOptionsMenu方法
            //本来这个方法在Activity类上需要API11的
            //所以需要转型
            //(PS:呆萌的AS告诉我转型是多余的2333)
            ((AppCompatActivity) context).invalidateOptionsMenu();
        }
    }


    @Override
    public void setSearchText(String text) {
        searchText = text;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }


    private class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return models.size();
        }

        @Override
        public Object getItem(int i) {
            return models.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private boolean contains(ArrayList<Integer> arrayList, int i) {
            for (Integer integer : arrayList) {
                if (integer == i) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null || !(view instanceof ViewGroup)) {
                view = context.getLayoutInflater().inflate(R.layout.main_fragment_list_item, null);
            }
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView title = (TextView) view.findViewById(R.id.app_name);
            TextView type = (TextView) view.findViewById(R.id.class_name);
            view.findViewById(R.id.checkbox).setVisibility(View.GONE);
            BlockModel bm = models.get(i).get(0);
            try {
                icon.setImageDrawable(getAppIcon(pm, bm.packageName));
                title.setText(getAppTitle(pm, bm.packageName));
                type.setText(String.format(getString(R.string.rules),models.get(i).size()));
            } catch (Exception e) {
                //Application not found
                title.setText(bm.packageName);
            }
            if (!searchText.isEmpty() && !title.getText().toString().toLowerCase().contains(searchText.toLowerCase())) {
                view = new View(context);
                view.setVisibility(View.GONE);
            }
            return view;
        }

    }
}
