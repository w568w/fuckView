package ml.qingsu.fuckview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static ml.qingsu.fuckview.Hook.ViewBlocker.getAllText;
import static ml.qingsu.fuckview.Hook.ViewBlocker.getText;
import static ml.qingsu.fuckview.Hook.ViewBlocker.getViewPath;

/**
 * w568w on 2017-6-30.
 */

public class Hook implements de.robv.android.xposed.IXposedHookLoadPackage {
    private static ArrayList<BlockModel> mBlockList = new ArrayList<>();
    private ArrayList<String> jsFiles = new ArrayList<>();
    //由于目标APP不一定有读写文件权限，所以想到了这么个奇巧淫技，自己维护个缓存区
    private static String writeFileCache = "";
    private static final String DIR_NAME = "fuckView/";
    private static final String JS_FILE_NAME = DIR_NAME + "js";
    private static final String SUPER_MODE_NAME = DIR_NAME + "super_mode";
    private static final String ONLY_ONCE_NAME = DIR_NAME + "only_once";
    private static final String PACKAGE_NAME_FILENAME = DIR_NAME + "package_name";
    private static final String LIST_FILENAME = DIR_NAME + "block_list";
    public static final String DIALOG_VIRTUAL_CLASSNAME = "Dialog";
    private static final String LAUNCHER_VIRTUAL_CLASSNAME = "launcher";

    private static final String ALL_SPLIT = "~~";
    private static final int NOTIFICATION_ID = 0x123;

    private boolean super_mode;
    private static boolean only_once;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //读文件会有多余的换行
        String pkg = Read_File(PACKAGE_NAME_FILENAME).replace("\n", "");

        //读取设置
        try {
            super_mode = Boolean.valueOf(Read_File(SUPER_MODE_NAME).replace("\n", ""));
            only_once = Boolean.valueOf(Read_File(ONLY_ONCE_NAME).replace("\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
            super_mode = false;
            only_once = false;
        }
        XposedBridge.log("净眼:开始HOOK --> " + loadPackageParam.packageName);
        //@see MainActivity
        if (loadPackageParam.packageName.equals("ml.qingsu.fuckview")) {
            XposedHelpers.findAndHookMethod("ml.qingsu.fuckview.MainActivity", loadPackageParam.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
            return;
        }

        XposedBridge.log("净眼:检测模块正常 -->" + pkg);
        if ((pkg != null && loadPackageParam.packageName.equals(pkg))) {
//            XposedHelpers.findAndHookMethod(View.class, "setOnClickListener", View.OnClickListener.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                    final View.OnClickListener ocl = (View.OnClickListener) param.args[0];
//                    param.args[0] = new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            //处理，显示参数
//                            HandleClick(view);
//                            if (ocl != null) {
//                                ocl.onClick(view);
//                            }
//                            //如果是ListView中的项目，手动CALL一次监听器
//                            ViewModel model = getListView(view);
//                            if (model != null) {
//                                //Google哪个傻逼写这个JB参数，非要我提供ID和位置，你TM自己不是有
//                                //getItemIdAtPosition嘛，你TM敢跟我说你没有getPositionForView？？
//                                //我敢肯定你们没审查代码
//                                // (╯‵□′)╯︵┻━┻！！！
//                                //我预祝写这个类的程序员下AV全是葫芦娃和喜羊羊
//
//                                //腾讯哪个傻逼，还真TM会装逼啊，自己写了个com.tencent.widget.AdapterView!!!
//                                //我TM还得用反射！不然就ClassCast异常！
//                                //想写累死我？
//                                //我问候您全家
//                                // fuck duck type!
//                                try {
//                                    int position = (int) XposedHelpers.callMethod(model.adapterView, "getPositionForView", new Class[]{View.class}, model.subView);
//                                    long id = (long) XposedHelpers.callMethod(model.adapterView, "getItemIdAtPosition", new Class[]{int.class}, position);
//                                    XposedHelpers.callMethod(model.adapterView, "performItemClick", new Class[]{View.class, int.class, int.class}, model.subView, position, id);
//                                } catch (Throwable e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    };
//                }
//            });
            XposedBridge.log("净眼:hook -->setOnTouchListener");
            XposedHelpers.findAndHookMethod(View.class, "setOnTouchListener", View.OnTouchListener.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    final View.OnTouchListener touchListener = (View.OnTouchListener) param.args[0];
                    param.args[0] = new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                                //处理，显示参数
                                HandleClick(view);
                            //如果是ListView中的项目，手动CALL一次监听器
                            ViewModel model = getListView(view);
                            if (model != null && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                try {
                                    int position = (int) XposedHelpers.callMethod(model.adapterView, "getPositionForView", new Class[]{View.class}, model.subView);
                                    long id = (long) XposedHelpers.callMethod(model.adapterView, "getItemIdAtPosition", new Class[]{int.class}, position);
                                    XposedHelpers.callMethod(model.adapterView, "performItemClick", new Class[]{View.class, int.class, int.class}, model.subView, position, id);
                                } catch (Throwable e) {
                                    //e.printStackTrace();
                                }
                            }

                            return touchListener != null && touchListener.onTouch(view, motionEvent);
                        }
                    };
                }
            });
            XposedBridge.log("净眼:hook -->setOnLongClickListener");
            //代码同上
            XposedHelpers.findAndHookMethod(View.class, "setOnLongClickListener", View.OnLongClickListener.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    final View.OnLongClickListener listener = (View.OnLongClickListener) param.args[0];
                    param.args[0] = new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            //处理，显示参数
                            HandleLongClick(view);
                            if (listener != null) {
                                listener.onLongClick(view);
                            }
                            ViewModel model = getListView(view);
                            if (model != null) {
                                try {
                                    int position = (int) XposedHelpers.callMethod(model.adapterView, "getPositionForView", new Class[]{View.class}, model.subView);
                                    long id = (long) XposedHelpers.callMethod(model.adapterView, "getItemIdAtPosition", new Class[]{int.class}, position);
                                    XposedHelpers.callMethod(model.adapterView, "performItemLongClick", new Class[]{View.class, int.class, int.class}, model.subView, position, id);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                            return false;
                        }
                    };
                }


            });
            XposedBridge.log("净眼:hook -->setClickable");
            XposedHelpers.findAndHookMethod(View.class, "setClickable", boolean.class, new booleanSetterHooker(true));
            XposedBridge.log("净眼:hook -->setLongClickable");
            XposedHelpers.findAndHookMethod(View.class, "setLongClickable", boolean.class, new booleanSetterHooker(true));
            XposedBridge.log("净眼:hook -->View<init>");
            XposedBridge.hookAllConstructors(View.class, new constructorHooker());
            XposedBridge.log("净眼:hook -->setCancelable");
            //对话框部分
            XposedHelpers.findAndHookMethod(Dialog.class, "setCancelable", boolean.class, new booleanSetterHooker(true));
//            XposedBridge.log("净眼:hook -->show");
//            XposedHelpers.findAndHookMethod(Dialog.class, "show", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                    final Dialog dialog = (Dialog) param.thisObject;
//                    dialog.setCancelable(true);
//                    if (dialog instanceof AlertDialog) {
//                        final AlertDialog alertDialog = (AlertDialog) dialog;
//                        final String id = getDialogID(alertDialog);
//                        final Context context = alertDialog.getContext();
//                        System.out.println("DialogID = " + id);
//
//                        if (id != null && !id.contains("你确定要屏蔽这一项吗?") && !id.contains("继续标记"))
//                            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface dialogInterface) {
//                                    new AlertDialog.Builder(context)
//                                            .setTitle("你确定要屏蔽这一项吗?")
//                                            .setMessage("按\"屏蔽\"来屏蔽这个对话框。")
//                                            .setPositiveButton("屏蔽", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    DialogBlocker.getInstance().log(dialog).save();
//                                                    getContinueAskDialog(context).show();
//                                                }
//                                            })
//                                            .setNegativeButton("好像不对", null)
//                                            .show();
//                                }
//                            });
//                    } else if (dialog instanceof android.support.v7.app.AlertDialog) {
//                        final android.support.v7.app.AlertDialog alertDialog = (android.support.v7.app.AlertDialog) dialog;
//                        final String id = getDialogID(alertDialog);
//                        final Context context = alertDialog.getContext();
//                        System.out.println("DialogID = " + id);
//
//                        if (id != null && !id.contains("你确定要屏蔽这一项吗?") && !id.contains("继续标记"))
//                            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface dialogInterface) {
//                                    new AlertDialog.Builder(context)
//                                            .setTitle("你确定要屏蔽这一项吗?")
//                                            .setMessage("按\"屏蔽\"来屏蔽这个对话框。")
//                                            .setPositiveButton("屏蔽", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    DialogBlocker.getInstance().log(dialog).save();
//                                                    getContinueAskDialog(context).show();
//                                                }
//                                            })
//                                            .setNegativeButton("好像不对", null)
//                                            .show();
//                                }
//                            });
//                    }
//                }
//            })
            try {
                XposedHelpers.findAndHookMethod("android.view.WindowManagerGlobal", Dialog.class.getClassLoader(), "removeView", View.class, boolean.class, new windowHook());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            XposedHelpers.findAndHookMethod("android.view.WindowManagerImpl", Dialog.class.getClassLoader(), "removeView", View.class, new windowHook());
            XposedHelpers.findAndHookMethod("android.view.WindowManagerImpl", Dialog.class.getClassLoader(), "removeViewImmediate", View.class, new windowHook());
        }

        //------------------------------------标记部分结束，以下为拦截部分------------------------
        //------------------------------------华丽的分割线----------------------------------------
        //读取屏蔽列表

        //坑：若要屏蔽的是一个“请使用Google Play服务”之类的对话框，标记时View.getContext().getPackageName()记录的是APK本身的包名，
        //屏蔽时调用同一方法却会读到com.google.android.gms这一包名，导致对不上记录，不屏蔽，很迷。
        //虽说可以HardCode，但是像这样使用XXX服务的APP不在少数...
        //这种方案肯定不行， TODO 一下，以后慢慢想解决办法。
//        if (!loadPackageParam.packageName.equals("com.google.android.gms"))
//           mBlockList = readBlockList(loadPackageParam.packageName);
        mBlockList = readBlockList(loadPackageParam.packageName);

        //以下为Hook
        //对话框取消那个APP，其实核心就这一行代码...
        XposedBridge.log("净眼:hook -->setCancelable");
        XposedHelpers.findAndHookMethod(Dialog.class, "setCancelable", boolean.class, new booleanSetterHooker(true));
        if (isBlockPackage(loadPackageParam.packageName) && !loadPackageParam.packageName.equals(pkg)) {
            XposedBridge.log("净眼:hook -->setVisibility");
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    View v = (View) param.thisObject;
                    if ((int) param.args[0] == View.GONE) {
                        return;
                    }
                    if (ViewBlocker.getInstance().isBlocked(v)) {
                        param.args[0] = View.GONE;
                        ViewBlocker.getInstance().block(v);
                    }
                }
            });
            XposedBridge.log("净眼:hook -->setText");
            XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    TextView v = (TextView) param.thisObject;
                    if (ViewBlocker.getInstance().isBlocked(v)) {
                        ViewBlocker.getInstance().block(v);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(View.class, "setLayoutParams", ViewGroup.LayoutParams.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) param.args[0];
                    if (layoutParams != null) {
                        if (layoutParams.height == 0 && layoutParams.width == 0)
                            return;
                        if (ViewBlocker.getInstance().isBlocked(param.thisObject))
                            ViewBlocker.getInstance().block(param.thisObject);
                    }
                }
            });
            final boolean finalSuper_mode = super_mode;
            XposedBridge.log("净眼:hook -->View<init>");
            final XC_MethodHook viewHooker=new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    final View v = (View) param.thisObject;
                    v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            if (finalSuper_mode) {
                                if (ViewBlocker.getInstance().isBlocked(v)) {
                                    v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    ViewBlocker.getInstance().block(v);
                                }
                            } else {
                                v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                if (ViewBlocker.getInstance().isBlocked(v)) {
                                    ViewBlocker.getInstance().block(v);
                                }
                            }
                        }
                    });
                }
            };

            XposedBridge.hookAllConstructors(View.class,viewHooker );
            XposedBridge.hookAllConstructors(ViewGroup.class,viewHooker );
            XposedBridge.log("净眼:hook -->addView");

            //Dialog blocking
            XposedHelpers.findAndHookMethod("android.view.WindowManagerImpl", loadPackageParam.classLoader, "addView",
                    View.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            View view = (View) param.args[0];
                            WindowManager windowManager = (WindowManager) param.thisObject;
                            if (view == null) return;
                            if (DialogBlocker.getInstance().isBlocked(view))
                                windowManager.removeViewImmediate(view);
                        }
                    });


            XposedHelpers.findAndHookMethod(Activity.class, "setContentView", View.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (LauncherBlocker.getInstance().isBlocked(param.thisObject)) {
                        View view = (View) param.args[0];
                        ViewBlocker.fuckView(view, true);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Activity.class, "setContentView", View.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (LauncherBlocker.getInstance().isBlocked(param.thisObject)) {
                        View view = (View) param.args[0];
                        ViewBlocker.fuckView(view, true);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(Activity.class, "setContentView", int.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (LauncherBlocker.getInstance().isBlocked(param.thisObject)) {

                        ViewBlocker.fuckView(getContentView((Activity) param.thisObject), true);
                    }

                }

                private View getContentView(Activity ac) {
                    //FrameLayout content = (FrameLayout) view.findViewById(android.R.id.content);
                    return (ViewGroup) ac.getWindow().getDecorView();
                }
            });
        }
    }

    private class windowHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            if (param.args.length == 0 || param.args[0] == null)
                return;
            View view = (View) param.args[0];
            final Context context = view.getContext();
            XposedBridge.log("净眼:Message --> RemoveView");
            final BlockModel model = DialogBlocker.getInstance().log(view);
            XposedBridge.log("净眼:Removed View -->" + model.id);
            //防止自残
            if (!model.id.equals("") &&
                    !model.id.contains("长按以标记")
                    && !model.id.contains("强制停止应用即可")
                    && !model.id.contains("你确定要屏蔽这一项吗?")
                    && !model.id.contains("继续标记")
                    && !model.id.contains("模式选择")
                    && !model.id.contains("已捕获")) {
                new AlertDialog.Builder(context)
                        .setTitle("你确定要屏蔽这一项吗?")
                        .setMessage("按\"屏蔽\"来屏蔽这个对话框。")
                        .setPositiveButton("屏蔽", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                model.save();
                                getContinueAskDialog(context).show();
                            }
                        })
                        .setNegativeButton("好像不对", null)
                        .show();
            }
        }
    }

    //--------------------------------------------------------------
    //--------------------------------------------------------------
    //Block包判断
    private boolean isBlockPackage(String pkg) {
//        jsFiles = JavaScriptRunner.readJS(pkg);
//        if (jsFiles.size() > 0)
//            return true;
        for (BlockModel model : mBlockList) {
            if (model.packageName.equals(pkg))
                return true;
        }

        return false;
    }


    private static AlertDialog getContinueAskDialog(final Context con) {
        return new AlertDialog.Builder(con)
                .setMessage("继续标记?\n是:暂时不保存结果，等会儿再说\n否:保存刚刚标记的所有结果，并返回净眼")
                .setPositiveButton("是", null)
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        OnlySingleToast.cancel();
                        Toast.makeText(con, "强制停止应用即可", Toast.LENGTH_LONG).show();
                        Intent intent = con.getPackageManager().getLaunchIntentForPackage("ml.qingsu.fuckview");
                        //看！奇巧淫技！
                        intent.putExtra("cache", writeFileCache);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        con.startActivity(intent);
                    }
                }).create();
    }

    private static void HandleLongClick(final View view) {
        final Context con = view.getContext();
        final AlertDialog adb = getContinueAskDialog(con);
        DialogInterface.OnClickListener
                onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new AlertDialog.Builder(con)
                        .setTitle("模式选择")
                        .setMessage("经典模式:使用ID和文本进行定位,适合于一般情况，误杀率高。\n\n路径模式:使用类XPath的形式定位，适合于位置固定不动，但ID为-1或文本会不断变化的情况，误杀率低。\n\n坐标模式:前两种无效时试试它。")
                        .setPositiveButton("经典", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ViewBlocker.getInstance().log(view).save();
                                OnlySingleToast.cancel();
                                adb.show();
                            }
                        })
                        .setNegativeButton("路径", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ViewBlocker.getInstance().logPath(view).save();
                                OnlySingleToast.cancel();
                                adb.show();
                            }
                        })
                        .setNeutralButton("坐标", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ViewBlocker.getInstance().logLocation(view).save();
                                OnlySingleToast.cancel();
                                adb.show();
                            }
                        })
                        .show();
            }
        };
        //假如没啥信息...

        new AlertDialog.Builder(con)
                .setTitle("已捕获")
                .setMessage(String.format(Locale.CHINA, "确认以下信息：\n种类:%s\nID:%s\n路径:%s\n大小:%d×%d\n文本:%s"
                        , view.getClass().getSimpleName(), view.getId(), getViewPath(view), view.getWidth(), view.getHeight(), getText(view)))
                .setPositiveButton("屏蔽", onClickListener)
                .setNegativeButton("好像不对", null).show();
    }

    private static void HandleClick(final View view) {
        //屏蔽几个按钮，防止调用多次Handle()
        if (view instanceof Button
                && ((Button) view).getText().equals("就它了")
                )
            return;
        if (view instanceof Button
                && ((Button) view).getText().equals("放弃标记"))
            return;
        if (view instanceof Button
                && ((Button) view).getText().equals("屏蔽"))
            return;
        if (view instanceof Button
                && ((Button) view).getText().equals("坚持屏蔽"))
            return;
        if (view instanceof Button
                && ((Button) view).getText().equals("好像不对"))
            return;
        if (view instanceof Button
                && ((Button) view).getText().equals("好吧"))
            return;

        final Context context = view.getContext();
        //设置View
        TextView infomation = new TextView(context);
        infomation.setText("长按以标记");
        //增加红框
        addViewShape(view);
        OnlySingleToast.showToast(context, infomation, Toast.LENGTH_SHORT);
        //显示通知栏
        //设置Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("ml.qingsu.fuckview");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //看！奇巧淫技x2！
        intent.putExtra("cache", "\n" + BlockModel.getInstanceByAll(view));
        intent.putExtra("Dialog", true);
        Notification n = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setTicker("控件捕获")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("净眼(点击以屏蔽)")
                .setContentText(view.getClass().getSimpleName())
                .setOngoing(false)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, n);
    }

    //给View加上bling bling的红边~~
    private static void addViewShape(final View view) {
        try {
            GradientDrawable gd = new GradientDrawable();
            gd.setStroke(4, Color.RED);
            final Drawable background = view.getBackground();
            view.setBackgroundDrawable(gd);
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setBackgroundDrawable(background);
                }
            }, 800);
        } catch (Throwable ignored) {

        }
    }


    //只显示最后一个Toast，避免依次显示
    private static class OnlySingleToast {
        private static Toast lastToast = null;

        private static void showToast(Context con, View v, int duartion) {
            if (lastToast == null)
                lastToast = new Toast(con);
            lastToast.setView(v);
            lastToast.setDuration(duartion);
            lastToast.show();
        }

        private static void cancel() {
            if (lastToast != null)
                lastToast.cancel();
        }
    }


    private class booleanSetterHooker extends XC_MethodHook {
        private boolean setValue;

        booleanSetterHooker(boolean s) {
            super();
            setValue = s;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            param.args[0] = setValue;
        }
    }

    private class constructorHooker extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            View view = (View) param.thisObject;

            // java.lang.RuntimeException:
            // Don't call setOnClickListener for an AdapterView.
            // You probably want setOnItemClickListener() instead.
            if (isAdapterView(view) || view == null) return;
            try {
                view.setClickable(true);
                view.setLongClickable(true);
                view.setOnTouchListener(null);
                view.setOnClickListener(null);
                view.setOnLongClickListener(null);
            } catch (Throwable ignored) {

            }
        }
    }

    private boolean isAdapterView(Object v) {
        return v instanceof AdapterView;
    }

    private ViewModel getListView(View v) {
        ViewParent vp = v.getParent();
        //如果它的父布局就是ListView
        if (isAdapterView(v))
            return new ViewModel(vp, v);
        //向上遍历
        while (vp != null) {
            if (isAdapterView(vp))
                return new ViewModel(vp.getParent(), (View) vp);
            vp = vp.getParent();
        }
        return null;
    }

    private class ViewModel {
        private Object adapterView;
        private View subView;

        private ViewModel(Object adapterView, View subView) {
            this.adapterView = adapterView;
            this.subView = subView;
        }
    }


    private static ArrayList<BlockModel> readBlockList(String pkgFilter) {
        ArrayList<BlockModel> list = new ArrayList<>();
        ArrayList<String> lines = readFileByLine(LIST_FILENAME);
        for (String line : lines) {
            BlockModel model = BlockModel.fromString(line);
            if (model != null && model.packageName.equals(pkgFilter))
                list.add(model);
        }
        return list;
    }

    private static ArrayList<BlockModel> readBlockList() {
        ArrayList<BlockModel> list = new ArrayList<>();
        ArrayList<String> lines = readFileByLine(LIST_FILENAME);
        for (String line : lines) {
            BlockModel model = BlockModel.fromString(line);
            if (model != null)
                list.add(model);
        }
        return list;
    }

    private static class BlockModel {
        private String packageName;
        public String id;
        private String text;
        private String className;
        private boolean enable;

        private static BlockModel fromString(String text) {
            String[] var = text.split("@@@");
            if (var.length == 4) {
                return new BlockModel(var[0], var[1], var[2], var[3]);
            }
            if (var.length == 5) {
                return new BlockModel(var[0], var[1], var[2], var[3], Boolean.valueOf(var[4]));
            }
            return null;
        }

        private BlockModel(String packageName, String id, String text, String className) {
            this.packageName = packageName;
            this.id = id;
            this.text = text;
            this.className = className;
            enable=true;
        }
        private BlockModel(String packageName, String id, String text, String className,boolean enable) {
            this.packageName = packageName;
            this.id = id;
            this.text = text;
            this.className = className;
            this.enable=enable;
        }
        private static BlockModel getInstanceByAll(View view) {
            return ViewBlocker.getInstance().logAll(view);
        }

        private void save() {
            Write_File(Read_File(LIST_FILENAME) + "\n" + toString(), LIST_FILENAME);
        }

        @Override
        public String toString() {
            return String.format(Locale.CHINA, "%s@@@%s@@@%s@@@%s@@@%s", packageName, id, text, className,enable+"");
        }
    }

    private static void Write_File(String data, String filename) {
        try {
            if (filename.equals(LIST_FILENAME)) {
                //直接写进cache
                writeFileCache = data;
                return;
            }
            File file = new File(File_Get_SD_Path() + "/" + filename);
            if (file.exists())
                file.delete();
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fops = new FileOutputStream(file);
            fops.write(data.getBytes());
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String File_Get_SD_Path() {
        String[] partpaths = {
                "/emulated/0",
                "/extSdCard",
                "/sdcard0",
                "/sdcard1",
                "/sdcard2",
                "/sdcard3",
                "/sdcard4",
                "/emulated/0",
                "/external_sd",
                "/extsdcard",
                "/sdcard",
                "/sdcard/sdcard",

        };
        File file = new File("/sdcard/");
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        for (String partpath : partpaths) {
            file = new File(partpath + "/");
            if (file.exists() && file.canWrite()) {
                return file.getAbsolutePath();
            }
            file = new File("/storage" + partpath + "/");
            if (file.exists() && file.canWrite()) {
                return file.getAbsolutePath();
            }
            file = new File("/mnt" + partpath + "/");

            if (file.exists() && file.canWrite()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    private static String Read_File(String filename) {
        if (filename.equals(LIST_FILENAME)) {
            //直接把cache还给它
            return writeFileCache;
        }

        File f = new File(File_Get_SD_Path() + "/" + filename);
        if (!f.exists())
            return "";
        String result = "";
        try {
            FileInputStream is = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufReader = new BufferedReader(isr);
            String line;
            while ((line = bufReader.readLine()) != null)
                result += ("\n" + line);
            bufReader.close();
            isr.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //正常的readfile,不做任何缓存代理
    private static ArrayList<String> readFileByLine(String filename) {
        File f = new File(File_Get_SD_Path() + "/" + filename);
        ArrayList<String> result = new ArrayList<>();
        if (!f.exists())
            return result;
        try {
            FileInputStream is = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufReader = new BufferedReader(isr);
            String line;
            while ((line = bufReader.readLine()) != null)
                result.add(line);
            bufReader.close();
            isr.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static abstract class Blocker {


        @NonNull
        public abstract BlockModel log(Object o);

        protected abstract Pair<Boolean, BlockModel> isBlock(Object o);

        public abstract void block(Object o);

        final boolean isBlocked(Object o) {
            Pair<Boolean, BlockModel> pair = isBlock(o);
            if (only_once && pair.second != null) {
                mBlockList.remove(pair.second);
            }
            return pair.first;
        }
    }
    static class ViewBlocker extends Blocker {
        private static ViewBlocker instance;

        public static ViewBlocker getInstance() {
            if (instance == null)
                instance = new ViewBlocker();
            return instance;
        }

        @NonNull
        @Override
        public BlockModel log(Object o) {
            View view = (View) o;

            return new BlockModel(view.getContext().getPackageName(), view.getId() + "", getText(view), view.getClass().getSimpleName());
        }

        public BlockModel logPath(Object o) {
            View view = (View) o;
            return new BlockModel(view.getContext().getPackageName(), getViewPath(view), getText(view), view.getClass().getSimpleName());
        }

        public BlockModel logLocation(Object o) {
            View view = (View) o;
            return new BlockModel(view.getContext().getPackageName(), getViewPosition(view), getText(view), view.getClass().getSimpleName());
        }

        public BlockModel logAll(Object o) {
            View view = (View) o;
            return new BlockModel(view.getContext().getPackageName(), view.getId() + ALL_SPLIT + getViewPath(view) + ALL_SPLIT + getViewPosition(view), getText(view), view.getClass().getSimpleName());
        }


        @Override
        protected Pair<Boolean, BlockModel> isBlock(Object o) {
            return isBlockView((View) o);
        }

        @Override
        public void block(Object o) {
            fuckView((View) o, true);
            XposedBridge.log("净眼:屏蔽一个控件 -->" + getAllText((View) o) + "," + o);
        }


        static String getViewPath(View v) {
            ViewParent viewParent = v.getParent();
            Object object = v;
            String path = "";
            while (viewParent != null) {
                if (viewParent instanceof ViewGroup) {
                    for (int i = 0; i < ((ViewGroup) viewParent).getChildCount(); i++) {
                        View child = ((ViewGroup) viewParent).getChildAt(i);
                        if (child.equals(object)) {
                            path += (i + "|" + child.getClass().getSimpleName() + "/");
                        }
                    }
                }
                object = viewParent;
                viewParent = viewParent.getParent();
            }
            path += "#";
            return path;
        }

        static String getViewPosition(View view) {
            int[] loc = new int[2];
            view.getLocationInWindow(loc);
            return loc[0] + "," + loc[1] + "$$";
        }

        static String getText(View view) {
            if (view == null)
                return "";
            if (view instanceof TextView)
                return ((TextView) view).getText().toString();
            if (view instanceof ViewGroup)
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View child = ((ViewGroup) view).getChildAt(i);
                    if (child instanceof TextView)
                        return ((TextView) child).getText().toString().replace("\n", "");
                }
            return "";
        }
        //Also @see ml.qingsu.fuckview.DumpViewerPopupView#getAllText(View)
        static String getAllText(View view) {
            String allText = "";
            if (view == null)
                return allText;
            if (view instanceof TextView)
                return ((TextView) view).getText().toString();
            if (view instanceof ViewGroup)
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View child = ((ViewGroup) view).getChildAt(i);
                    if (child instanceof TextView)
                        if(!allText.equals(""))
                            allText += (((TextView) child).getText().toString().replace("\n", "") + "|");
                    if (child instanceof ViewGroup)
                        allText += getAllText(child);
                }
            return allText;
        }

        private static Pair<Boolean, BlockModel> isBlockView(View view) {

            //测试代码，为某个功能做铺垫
//            ViewParent parent = view.getParent();
//            if (parent != null && (parent instanceof AdapterView || parent instanceof RecyclerView)) {
//                return new Pair<>(getAllText(view).contains("广告"), null);
//            }
            final String className = view.getClass().getSimpleName();
            final int id = view.getId();
            for (BlockModel model : mBlockList) {
//                final String pkg = view.getContext().getPackageName();
//
//                if (!model.packageName.equals(pkg))
//                    continue;

                //className都不对，免谈了，直接跳过
                if (!model.className.equals("*") && !model.className.equals(className))
                    continue;
                //如果ID/路径/坐标能用
                if (model.id.contains("#")) {
                    if (getViewPath(view).equals(model.id))
                        return new Pair<>(model.enable, model);
                } else if (model.id.contains("$$")) {
                    if (getViewPosition(view).equals(model.id))
                        return new Pair<>(model.enable, model);
                } else if ((!model.id.equals("-1") && (id != -1))) {
                    if (model.id.equals(id + ""))
                        //以防万一，再判断一次文本
                        if ((!model.text.equals("")) && (model.text.equals(getText(view))))
                            return new Pair<>(model.enable, model);
                }
                //ID也不能用？判断文本
                else if ((!model.text.equals("")) && (model.text.equals(getText(view)))) {
                    return new Pair<>(model.enable, model);
                }
            }
            return new Pair<>(false, null);
        }

        private static void fuckView(View v, boolean shouldVisibility) {
            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
            layoutParams.height = 0;
            layoutParams.width = 0;
            v.setPadding(0, 0, 0, 0);
            v.setMinimumHeight(0);
            v.setMinimumWidth(0);
            v.setBackgroundColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                v.setAlpha(0f);
            }

            v.setLayoutParams(layoutParams);
            v.clearAnimation();
            if (shouldVisibility)
                v.setVisibility(View.GONE);
            v.setWillNotDraw(true);
        }

    }

    static class DialogBlocker extends Blocker {
        private static DialogBlocker instance;

        public static DialogBlocker getInstance() {
            if (instance == null)
                instance = new DialogBlocker();
            return instance;
        }

        @NonNull
        @Override
        public BlockModel log(Object o) {
            return new BlockModel(((View) o).getContext().getPackageName(), getAllText((View) o), "", DIALOG_VIRTUAL_CLASSNAME);
        }

        @Override
        protected Pair<Boolean, BlockModel> isBlock(Object o) {
            XposedBridge.log("add View-->" + getAllText((View) o));
            return isBlockDialog((View) o);
        }

        @Override
        public void block(Object o) {
        }

        //Block对话框判断
        private static Pair<Boolean, BlockModel> isBlockDialog(View view) {
            final String pkg = view.getContext().getPackageName();
            for (BlockModel model : mBlockList) {
                if (!model.packageName.equals(pkg) || !model.className.equals(DIALOG_VIRTUAL_CLASSNAME)) {
                    continue;
                }
                if (model.id.equals(getAllText(view))) {
                    return new Pair<>(model.enable, model);
                }
            }
            return new Pair<>(false, null);
        }

    }

    private static class LauncherBlocker extends Blocker {
        private static LauncherBlocker instance;

        public static LauncherBlocker getInstance() {
            if (instance == null)
                instance = new LauncherBlocker();
            return instance;
        }

        @NonNull
        @Override
        public BlockModel log(Object o) {
            return null;
        }

        @Override
        protected Pair<Boolean, BlockModel> isBlock(Object o) {

            Activity activity = (Activity) o;

            final String pkg = activity.getPackageName();
            final String ClazzName = activity.getClass().getName();
            XposedBridge.log("净眼:新的Acitivity-->" + pkg + " " + ClazzName);
            if (activity == null)
                return new Pair<>(false, null);
            else
                for (BlockModel model : mBlockList) {
                    if (!model.packageName.equals(pkg) || !model.className.equals(LAUNCHER_VIRTUAL_CLASSNAME))
                        continue;
                    if (model.id.equals(ClazzName))
                        return new Pair<>(model.enable, model);
                }

            return new Pair<>(false, null);
        }

        @Override
        public void block(Object o) {

        }
    }

}
