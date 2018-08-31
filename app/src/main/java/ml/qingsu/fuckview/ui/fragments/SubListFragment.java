package ml.qingsu.fuckview.ui.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.ui.fragments.select_app.SelectAppWizard;

import static ml.qingsu.fuckview.ui.fragments.MainFragment.getAppIcon;
import static ml.qingsu.fuckview.ui.fragments.MainFragment.getAppTitle;

/**
 * Created by w568w on 18-6-6.
 *
 * @author w568w
 */

public class SubListFragment extends Fragment implements Searchable {
    ListView listView;
    private Activity context;
    ArrayList<BlockModel> models;
    private AppAdapter adapter;
    PackageManager packageManager;
    private ArrayList<Integer> deleteList = new ArrayList<>();
    String searchText = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        packageManager = context.getPackageManager();
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.main_fragment, null);
        listView = (ListView) layout.findViewById(R.id.listView);
        TextView noItem = (TextView) layout.findViewById(R.id.no_item);
        noItem.setVisibility(View.GONE);
        models = MainFragment.models.get(getArguments().getInt("index"));
        adapter = new AppAdapter();
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        final FloatingActionButton button = (FloatingActionButton) layout.findViewById(R.id.fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectAppWizard wizard = new SelectAppWizard();
                Bundle bundle = new Bundle();
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("system_app", false)) {
                    bundle.putBoolean("sys", true);
                }
                wizard.setArguments(bundle);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setFragment(wizard);
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BlockModel model = models.get(i);
                Bundle bundle = new Bundle();
                bundle.putString("pkg", model.packageName);
                bundle.putString("record", model.record);
                bundle.putString("className", model.className);
                InfoFragment infoFragment = new InfoFragment();
                infoFragment.setArguments(bundle);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setFragment(infoFragment);
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
        //If there's no rule...
        if (models.isEmpty()) {
            listView.setVisibility(View.GONE);
            noItem.setVisibility(View.VISIBLE);
        }

        return layout;
    }

    @Override
    public void setSearchText(String text) {
        searchText = text;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }

    private void saveAll() {
        MainActivity.writePreferences("", Constant.LIST_NAME);
        MainFragment.models.set(getArguments().getInt("index"), models);
        for (ArrayList<BlockModel> model : MainFragment.models) {
            for (BlockModel bm : model) {
                bm.save();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BlockModel model = models.get(info.position);
        if (deleteList.size() > 0) {
            menu.add(0, 4, Menu.NONE, R.string.delete_selections);
            menu.add(0, 5, Menu.NONE, R.string.share_selections);
            return;
        }
        menu.add(0, 1, Menu.NONE, R.string.delete_item);
        menu.add(0, 3, Menu.NONE, R.string.share);
        menu.add(0, 6, Menu.NONE, model.enable ? R.string.disable_item : R.string.enable_item);
        menu.add(0, 7, Menu.NONE, R.string.start_app);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final BlockModel model = models.get(menuInfo.position);
        switch (item.getItemId()) {
            case 1:
                models.remove(menuInfo.position);
                adapter.notifyDataSetChanged();
                saveAll();
                break;
            case 2:
                new AlertDialog.Builder(context)
                        .setTitle(R.string.confirm)
                        .setMessage("是否要将此项设为不按类名定位？\n这样可能会解决一些无法屏蔽的问题，但是也会增加一些误伤的几率。\n\n注意:此操作不可逆!")
                        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                model.className = "*";
                                models.set(menuInfo.position, model);
                                adapter.notifyDataSetChanged();
                                saveAll();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;
            case 3:
                share(model.toString());
                break;
            case 4:
                ArrayList<BlockModel> arrayList = new ArrayList<>();
                for (Integer postion : deleteList) {
                    arrayList.add(models.get(postion));
                }
                models.removeAll(arrayList);
                deleteList.clear();
                adapter.notifyDataSetChanged();

                saveAll();
                break;
            case 5:
                ArrayList<BlockModel> shares = new ArrayList<>();
                for (Integer postion : deleteList) {
                    shares.add(models.get(postion));
                }
                deleteList.clear();
                StringBuilder stringBuilder = new StringBuilder();
                for (BlockModel model1 : shares) {
                    stringBuilder.append(model1.toString()).append("\n");
                }
                share(stringBuilder.toString());
                adapter.notifyDataSetChanged();
                break;
            case 6:
                model.enable = !model.enable;
                models.set(menuInfo.position, model);
                adapter.notifyDataSetChanged();
                saveAll();
                break;
            case 7:
                try {
                    startActivity(packageManager.getLaunchIntentForPackage(model.packageName));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.cant_start_app, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        refreshTitle();
        return super.onContextItemSelected(item);
    }

    private void share(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setType("text/plain");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.no_share_app, Toast.LENGTH_SHORT).show();
        }
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
            ViewCompat.setTransitionName(icon, String.valueOf(i) + "_image");
            final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            checkbox.setChecked(contains(deleteList, i));
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isChecked = contains(deleteList, i);
                    if (isChecked) {
                        deleteList.remove(Integer.valueOf(i));
                    } else {
                        deleteList.add(i);
                    }
                    refreshTitle();
                }
            });
            BlockModel bm = models.get(i);
            try {
                icon.setImageDrawable(getAppIcon(packageManager, bm.packageName));
                title.setText(getAppTitle(packageManager, bm.packageName));
            } catch (Exception e) {
                //Application not found
                icon.setImageResource(R.drawable.ic_launcher);
                title.setText(bm.packageName);
            }
            //是否是经典模式
            if (bm.text.isEmpty()) {
                type.setText(bm.className);
            } else {
                type.setText(String.format("%s ---> %s", bm.className, bm.text));
            }
            if (!bm.enable) {
                view.setBackgroundColor(Color.GRAY);
            }
            if (!searchText.isEmpty() && !title.getText().toString().toLowerCase().contains(searchText.toLowerCase())) {
                view = new View(context);
                view.setVisibility(View.GONE);
            }
            return view;
        }

    }

    private void refreshTitle() {
        getActivity().setTitle(deleteList.isEmpty() ? R.string.app_name : R.string.multi_select);
    }
}
