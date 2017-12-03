package ml.qingsu.fuckview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ml.qingsu.fuckview.view_dumper.ViewDumper;

/**
 * Created by w568w on 2017-7-29.
 */

class FullScreenPopupWindow extends GlobalPopupWindow {
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
    protected View onCreateView(Context context) {
        absoluteLayout = new AbsoluteLayout(context);
        absoluteLayout.setBackgroundColor(Color.argb(120, 0, 0, 0));
        absoluteLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_UP) return false;
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
                    //先储存它原有的样式
                    final Drawable drawable = view.getBackground();
                    //弹出菜单
                    android.support.v7.widget.PopupMenu popupMenu = new android.support.v7.widget.PopupMenu(view.getContext(), view);
                    popupMenu.getMenu().add("就它了");
                    popupMenu.getMenu().add("结束标记");
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals("就它了")) {
                                Point p = ((ViewDumper.ViewItem) view.getTag()).bounds;
                                MainActivity.Append_Preferences("\n" + new MainActivity.BlockModel(pkg, p.x + "," + p.y + "$$", "", "*").toString(), MainActivity.LIST_NAME);
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

    //上层View点击后会使下层接收不到事件，这里用了一种极其愚蠢的方法...
    private View getTouchView(int x, int y) {
        View v = null;
        AbsoluteLayout.LayoutParams MinParam = null;
        for (int i = 0; i < absoluteLayout.getChildCount(); i++) {
            View view = absoluteLayout.getChildAt(i);

            AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) view.getLayoutParams();
            if (x >= param.x && x <= (param.x + param.width))
                if (y >= param.y && y <= (param.y + param.height)) {
                    if (v == null || (MinParam.height * MinParam.width > param.width * param.height)) {
                        v = view;
                        MinParam = param;
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

    @Override
    protected int getGravity() {
        return Gravity.TOP | Gravity.LEFT;
    }
}
