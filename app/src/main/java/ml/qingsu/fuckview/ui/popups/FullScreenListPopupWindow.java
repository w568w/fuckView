package ml.qingsu.fuckview.ui.popups;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.PopupMenu;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.utils.view_dumper.ViewDumper;

/**
 * Created by w568w on 2017-7-29.
 */

class FullScreenListPopupWindow extends GlobalPopupWindow {
    private ArrayList<ViewDumper.ViewItem> list;
    private ListViewCompat listView;
    private String pkg;
    private DumpViewerPopupView popupView;

    FullScreenListPopupWindow(Activity activity, ArrayList<ViewDumper.ViewItem> list, String pkg, DumpViewerPopupView popupView) {
        super(activity);
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        this.list = list;
        this.pkg = pkg;
        this.popupView = popupView;
        init(activity);
    }

    @Override
    protected View onCreateView(final Context context) {
        listView = new ListViewCompat(context);
        listView.setBackgroundColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            listView.setAlpha(0.8f);
        }
        return listView;
    }


    private void init(final Context context) {
        listView.setFocusableInTouchMode(true);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return list.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(final int position, View convertView, final ViewGroup parent) {
                TextView textView;
                if (convertView == null || convertView instanceof TextView)
                    textView = new TextView(context);
                else
                    textView = (TextView) convertView;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setPadding(0, 12, 0, 12);
                final ViewDumper.ViewItem item = (ViewDumper.ViewItem) getItem(position);
                //文字前空格
                textView.setText(tab(item.level) + item.simpleClassName);
                textView.setClickable(true);
                textView.setLongClickable(true);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hide();
                        //红框

                        final RedBoundPopupWindow redBound = new RedBoundPopupWindow(getActivity(), item.bounds, item.wh);
                        redBound.show();
                        v.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                redBound.hide();
                                show();
                            }
                        }, 1000);
                    }
                });
                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(context, v);
                        popupMenu.getMenu().add("标记该项");
                        popupMenu.getMenu().add("退出选择");
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().equals("标记该项")) {
                                    ViewDumper.ViewItem item1 = list.get(position);
                                    Point p = item1.bounds;
                                    MainActivity.Append_Preferences("\n" + new MainActivity.ViewModel(pkg, " "+MainActivity.ALL_SPLIT+" "+MainActivity.ALL_SPLIT+p.x + "," + p.y + "$$", "", "*").toString(), MainActivity.LIST_NAME);
                                    Toast.makeText(getActivity(), "已保存标记", Toast.LENGTH_SHORT).show();
                                } else {
                                    //选择“结束标记”，先隐藏自身，
                                    hide();
                                    //再显示悬浮窗
                                    popupView.show();
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                        return true;
                    }
                });
                return textView;
            }

            private String tab(int num) {
                String tab = "";
                for (int i = 0; i < num; i++) {
                    tab += " ";
                }
                return tab;
            }
        });
        listView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_BACK){
                    hide();
                    popupView.show();
                    return true;
                }
                return false;
            }
        });
    }

    protected int getGravity() {
        return Gravity.TOP | Gravity.LEFT;
    }
}
