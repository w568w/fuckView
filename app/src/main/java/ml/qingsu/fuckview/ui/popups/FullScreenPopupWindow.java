package ml.qingsu.fuckview.ui.popups;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.base.BasePopupWindow;
import ml.qingsu.fuckview.models.ViewModel;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.utils.dumper.ViewDumper;

/**
 * Created by w568w on 2017-7-29.
 */

class FullScreenPopupWindow extends BasePopupWindow {
    private ArrayList<ViewDumper.ViewItem> list;
    private AbsoluteLayout absoluteLayout;
    private String pkg;
    private DumpViewerPopupView popupView;

    FullScreenPopupWindow(Activity activity, ArrayList<ViewDumper.ViewItem> list, String pkg, DumpViewerPopupView popupView) {
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
        absoluteLayout = new AbsoluteLayout(context);
        absoluteLayout.setBackgroundColor(Color.argb(120, 0, 0, 0));
        absoluteLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                //获得点击的View
                View v = getTouchView((int) motionEvent.getX(), (int) motionEvent.getY());
                if (v != null) {
                    //调用点击处理
                    onClick(v);
                }
                return true;
            }

            private void onClick(final View view) {
                //确定为红框标识
                if (view.getTag() instanceof ViewDumper.ViewItem) {
                    System.out.println(getPath((ViewDumper.ViewItem) view.getTag()));
                    //先储存它原有的样式
                    final Drawable drawable = view.getBackground();
                    //弹出菜单
                    android.support.v7.widget.PopupMenu popupMenu = new android.support.v7.widget.PopupMenu(view.getContext(), view);
                    popupMenu.getMenu().add(R.string.popup_mark_it);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals(context.getString(R.string.popup_mark_it))) {
                                ViewDumper.ViewItem item1 = (ViewDumper.ViewItem) view.getTag();
                                Point p = item1.bounds;
                                MainActivity.appendPreferences("\n" + new ViewModel(pkg, " "+MainActivity.ALL_SPLIT+" "+MainActivity.ALL_SPLIT+p.x + "," + p.y + "$$", "", "*").toString(), MainActivity.LIST_NAME);
                                Toast.makeText(getActivity(), R.string.rule_saved, Toast.LENGTH_SHORT).show();

                            }
                            return true;
                        }
                    });
                    popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                        @Override
                        public void onDismiss(PopupMenu menu) {
                            view.setBackgroundDrawable(drawable);
                            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                            updateLayout();
                        }
                    });
                    popupMenu.setGravity(Gravity.CENTER);
                    view.setBackgroundColor(Color.argb(120, 255, 0, 0));
                    updateLayout();
                    popupMenu.show();
                }
            }
        });
        absoluteLayout.setFocusableInTouchMode(true);
        absoluteLayout.setOnKeyListener(new View.OnKeyListener() {
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
        return absoluteLayout;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = appContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = appContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 上层View点击后会使下层接收不到事件，这里用了一种极其愚蠢的方法...
     * @param x x axis
     * @param y y axis
     * @return
     */

    private View getTouchView(int x, int y) {
        View v = null;
        AbsoluteLayout.LayoutParams minParam = null;
        for (int i = 0; i < absoluteLayout.getChildCount(); i++) {
            View view = absoluteLayout.getChildAt(i);

            AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) view.getLayoutParams();
            if (x >= param.x && x <= (param.x + param.width)) {
                if (y >= param.y && y <= (param.y + param.height)) {
                    if (v == null || (minParam.height * minParam.width > param.width * param.height)) {
                        v = view;
                        minParam = param;
                    }
                }
            }
        }
        return v;
    }

    private void init(final Context context) {


        for (ViewDumper.ViewItem item : list) {
            TextView tv = new TextView(context);
            tv.setTag(item);
            //红框
            GradientDrawable redBounds = new GradientDrawable();
            redBounds.setStroke(2, Color.RED);
            redBounds.setColor(Color.TRANSPARENT);
            tv.setBackgroundDrawable(redBounds);
            AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(item.wh.x, item.wh.y, item.bounds.x, item.bounds.y - getStatusBarHeight());
            absoluteLayout.addView(tv, layoutParams);
        }
    }

    public String getPath(ViewDumper.ViewItem viewItem) {
        return getPathRecur(viewItem) + "#";
    }

    private String getPathRecur(ViewDumper.ViewItem viewItem) {
        String path = "";
        Position p = getPostionInTop(viewItem);
        path += p.postion + "|" + viewItem.simpleClassName + "/";
        if (p.parent != null) {
            path += getPathRecur(p.parent);
        }
        return path;
    }

    /**
     * @param viewItem 控件对象
     * @return 控件在父布局中的位置和父布局本身控件
     */
    private Position getPostionInTop(ViewDumper.ViewItem viewItem) {
        int position = 0;
        for (int index = list.indexOf(viewItem) - 1; index >= 0; index--) {
            ViewDumper.ViewItem node = list.get(index);
            if (node.level == viewItem.level) {
                position++;
            }
            if (node.level == viewItem.level - 1) {
                return new Position(position, node);
            }
        }
        return new Position(position, null);
    }

    private class Position {
        int postion;
        ViewDumper.ViewItem parent;

        public Position(int postion, ViewDumper.ViewItem parent) {
            this.postion = postion;
            this.parent = parent;
        }
    }

    @Override
    protected int getGravity() {
        return Gravity.TOP | Gravity.LEFT;
    }
}
