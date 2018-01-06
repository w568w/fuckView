package ml.qingsu.fuckview.hook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
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

import java.util.ArrayList;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.ui.activities.MainActivity;

import static ml.qingsu.fuckview.hook.Hook.ViewBlocker.getAllText;
import static ml.qingsu.fuckview.hook.Hook.ViewBlocker.getText;
import static ml.qingsu.fuckview.hook.Hook.ViewBlocker.getViewPath;

/**
 * w568w on 2017-6-30.
 */

public class Hook implements IXposedHookLoadPackage {

    public static final String DIALOG_VIRTUAL_CLASSNAME = "Dialog";
    private static final String SUPER_MODE_NAME = "superMode";
    private static final String ONLY_ONCE_NAME = "onlyOnce";
    private static final String STANDARD_MODE_NAME = "standardMode";
    private static final String PACKAGE_NAME_NAME = "package_name";
    private static final String LIST_FILENAME = "block_list";
    private static final String BROADCAST_ACTION = "tooYoungtooSimple";
    private static final String ALL_SPLIT = "~~";
    private static final int NOTIFICATION_ID = 0x123;
    //由于目标APP不一定有读写文件权限，所以想到了这么个
    // 奇巧淫技，自己维护个缓存区
    private static String writeFileCache = "";
    private static boolean onlyOnce;
    private static boolean standardMode;
    private boolean superMode;

    private static XSharedPreferences xSP = new XSharedPreferences("ml.qingsu.fuckview", "data");

    private static int mNotificationId = NOTIFICATION_ID + 1;

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

    private static String getString(int resId, Context context) throws PackageManager.NameNotFoundException {
        try {
            Context appContext = context.createPackageContext("ml.qingsu.fuckview", Context.CONTEXT_IGNORE_SECURITY);
            return appContext.getResources().getString(resId);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private static void HandleLongClick(final View view) throws PackageManager.NameNotFoundException {
        final Context con = view.getContext();
        final AlertDialog adb = getContinueAskDialog(con);
        DialogInterface.OnClickListener
                onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ViewBlocker.getInstance().log(view).save();
                OnlySingleToast.cancel();
                adb.show();
            }
        };
        //假如没啥信息...
        new AlertDialog.Builder(con)
                .setTitle(getString(R.string.captured, con))
                .setMessage(String.format("确认以下信息：\n种类:%s\nID:%s\n路径:%s\n大小:%d×%d\n文本:%s"
                        , view.getClass().getSimpleName(), view.getId(), getViewPath(view), view.getWidth(), view.getHeight(), getText(view)))
                .setPositiveButton("屏蔽", onClickListener)
                .setNegativeButton("好像不对", null).show();
    }

    private void HandleClick(final View view) throws PackageManager.NameNotFoundException {
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
//        TextView infomation = new TextView(context);
//        infomation.setText("长按以标记");
        //增加红框
        addViewShape(view);
        //OnlySingleToast.showToast(context, infomation, Toast.LENGTH_SHORT);
        //显示通知栏
        //设置Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("ml.qingsu.fuckview");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //看！奇巧淫技x2！
        intent.putExtra("cache", "\n" + BlockModel.getInstanceByAll(view));
        intent.putExtra("Dialog", true);
        Notification n = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setTicker(getString(R.string.captured, context))
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.notification_title, context))
                .setContentText(view.getClass().getSimpleName())
                .setOngoing(false)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, n);

        //BroadcastReceiver
        Intent broadcastIntent = new Intent(BROADCAST_ACTION)
                .putExtra("height", view.getHeight())
                .putExtra("width", view.getWidth())
                .putExtra("className", view.getClass().getSimpleName())
                .putExtra("path", ViewBlocker.getViewPath(view));
        context.sendBroadcast(broadcastIntent);
        XposedBridge.log("净眼:Send a broadcast!");
    }

    //与上面唯一的区别是通知方式
    private void HandleTouch(final View view) throws PackageManager.NameNotFoundException {
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
//        TextView infomation = new TextView(context);
//        infomation.setText("长按以标记");
        //增加红框
        addViewShape(view);
        //OnlySingleToast.showToast(context, infomation, Toast.LENGTH_SHORT);
        //显示通知栏
        //设置Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("ml.qingsu.fuckview");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //看！奇巧淫技x2！
        XposedBridge.log(BlockModel.getInstanceByAll(view).toString());
        intent.putExtra("cache", "\n" + BlockModel.getInstanceByAll(view));
        intent.putExtra("Dialog", true);
        Notification n = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setTicker(getString(R.string.captured, context))
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.notification_title, context))
                .setContentText(view.getClass().getSimpleName())
                .setOngoing(false)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, n);

        //BroadcastReceiver
        Intent broadcastIntent = new Intent(BROADCAST_ACTION)
                .putExtra("height", view.getHeight())
                .putExtra("width", view.getWidth())
                .putExtra("className", view.getClass().getSimpleName())
                .putExtra("path", ViewBlocker.getViewPath(view));
        XposedBridge.log("净眼:Send a broadcast!");
        context.sendBroadcast(broadcastIntent);
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

    private static ArrayList<BlockModel> readBlockList(String pkgFilter) {
        ArrayList<BlockModel> list = new ArrayList<>();
        ArrayList<String> lines = readPreferenceByLine(LIST_FILENAME);
        for (String line : lines) {

            BlockModel model = BlockModel.fromString(line);
            if (model.record.contains(ALL_SPLIT)) {
                model = ViewModel.fromString(line);
            }
            if (model != null && model.packageName.equals(pkgFilter))
                list.add(model);
        }
        return list;
    }

    private static void Write_Preference(String data, String filename) {
        xSP.makeWorldReadable();
        xSP.getFile().setWritable(true);
        xSP.edit().putString(filename, data).apply();
    }

    private static String Read_Preference(String filename) {
        if (filename.equals(LIST_FILENAME)) {
            //直接返回cache
            return writeFileCache;
        }
        return xSP.getString(filename, "");
    }

    //正常的readfile,不做任何缓存代理
    private static ArrayList<String> readPreferenceByLine(String filename) {
        String data = xSP.getString(filename, "");
        ArrayList<String> arrayList = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (!line.equals("")) arrayList.add(line);
        }
        return arrayList;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.log("Read File -->" + xSP.getFile().getAbsolutePath());

        final ArrayList<BlockModel> mBlockList;
        xSP.reload();
        xSP.makeWorldReadable();
        xSP.getFile().setWritable(true);
        String pkg = xSP.getString(PACKAGE_NAME_NAME, "");

        //读取设置
        try {
            superMode = Boolean.valueOf(xSP.getString(SUPER_MODE_NAME, String.valueOf(false)));
            onlyOnce = Boolean.valueOf(xSP.getString(ONLY_ONCE_NAME, String.valueOf(false)));
            standardMode = Boolean.valueOf(xSP.getString(STANDARD_MODE_NAME, String.valueOf(true)));
        } catch (Exception e) {
            e.printStackTrace();
            superMode = false;
            onlyOnce = false;
            standardMode = true;
        }
        XposedBridge.log("净眼:开始HOOK --> " + loadPackageParam.packageName);
        //@see MainActivity
        if (loadPackageParam.packageName.equals("ml.qingsu.fuckview")) {
            XposedHelpers.findAndHookMethod("ml.qingsu.fuckview.ui.activities.MainActivity", loadPackageParam.classLoader,
                    "isModuleActive", XC_MethodReplacement.returnConstant(true));
            return;
        }

        XposedBridge.log("净眼:检测模块正常 -->" + pkg);
        if ((pkg != null && loadPackageParam.packageName.equals(pkg))) {
            if (standardMode) {
                XposedBridge.log("净眼:hook -->setOnClickListener");
                XposedHelpers.findAndHookMethod(View.class, "setOnClickListener", View.OnClickListener.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        final View.OnClickListener clickListener = (View.OnClickListener) param.args[0];
                        param.args[0] = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //处理，显示参数
                                try {
                                    HandleClick(view);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                //如果是ListView中的项目，手动CALL一次监听器
                                ViewArg model = getListView(view);
                                if (model != null) {
                                    XposedBridge.log("in ListView!");
                                    try {
                                        int position = (int) XposedHelpers.callMethod(model.adapterView, "getPositionForView", new Class[]{View.class}, model.subView);
                                        long id = (long) XposedHelpers.callMethod(model.adapterView, "getItemIdAtPosition", new Class[]{int.class}, position);
                                        XposedHelpers.callMethod(model.adapterView, "performItemClick", new Class[]{View.class, int.class, int.class}, model.subView, position, id);
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (clickListener != null) clickListener.onClick(view);
                            }
                        };
                    }
                });
            } else {
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
                                    try {
                                        HandleTouch(view);
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                //Needn't the following.
//                                //如果是ListView中的项目，手动CALL一次监听器
//                                ViewArg model = getListView(view);
//                                if (model != null && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                                    try {
//                                        int position = (int) XposedHelpers.callMethod(model.adapterView, "getPositionForView", new Class[]{View.class}, model.subView);
//                                        long id = (long) XposedHelpers.callMethod(model.adapterView, "getItemIdAtPosition", new Class[]{int.class}, position);
//                                        XposedHelpers.callMethod(model.adapterView, "performItemClick", new Class[]{View.class, int.class, int.class}, model.subView, position, id);
//                                    } catch (Throwable e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                                return touchListener != null && touchListener.onTouch(view, motionEvent);
                            }
                        };
                    }
                });
            }
            XposedBridge.log("净眼:hook -->setOnLongClickListener");
            //代码同上
            //长按已废除
//            XposedHelpers.findAndHookMethod(View.class, "setOnLongClickListener", View.OnLongClickListener.class, new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                    final View.OnLongClickListener listener = (View.OnLongClickListener) param.args[0];
//                    param.args[0] = new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(View view) {
//                            //处理，显示参数
//                            HandleLongClick(view);
//                            if (listener != null) {
//                                listener.onLongClick(view);
//                            }
//                            ViewArg model = getListView(view);
//                            if (model != null) {
//                                try {
//                                    int position = (int) XposedHelpers.callMethod(model.adapterView, "getPositionForView", new Class[]{View.class}, model.subView);
//                                    long id = (long) XposedHelpers.callMethod(model.adapterView, "getItemIdAtPosition", new Class[]{int.class}, position);
//                                    XposedHelpers.callMethod(model.adapterView, "performItemLongClick", new Class[]{View.class, int.class, int.class}, model.subView, position, id);
//                                } catch (Throwable e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            return false;
//                        }
//                    };
//                }
//
//
//            });
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
            XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                }
            });
        }

        //------------------------------------标记部分结束，以下为拦截部分------------------------
        //------------------------------------华丽的分割线----------------------------------------
        //读取屏蔽列表
        mBlockList = readBlockList(loadPackageParam.packageName);

        //以下为Hook
        //对话框取消那个APP，其实核心就这一行代码...
        XposedBridge.log("净眼:hook -->setCancelable");
        XposedHelpers.findAndHookMethod(Dialog.class, "setCancelable", boolean.class, new booleanSetterHooker(true));
        if (isBlockPackage(mBlockList, loadPackageParam.packageName) && !loadPackageParam.packageName.equals(pkg)) {
            XposedBridge.log("净眼:hook -->setVisibility");
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    View v = (View) param.thisObject;
                    if ((int) param.args[0] == View.GONE) {
                        return;
                    }
                    if (ViewBlocker.getInstance().isBlocked(mBlockList, v)) {
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

                    if (ViewBlocker.getInstance().isBlocked(mBlockList, v))
                        ViewBlocker.getInstance().block(v);
                }
            });
            XposedHelpers.findAndHookMethod(View.class, "setLayoutParams", ViewGroup.LayoutParams.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) param.args[0];
                    if (layoutParams != null) {
                        if (layoutParams.height == 0 && layoutParams.width == 0)
                            return;
                        if (ViewBlocker.getInstance().isBlocked(mBlockList, param.thisObject))
                            ViewBlocker.getInstance().block(param.thisObject);
                    }
                }
            });

            final boolean finalSuper_mode = superMode;
            XposedBridge.log("净眼:hook -->View<init>");
            final XC_MethodHook viewHooker = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    final View v = (View) param.thisObject;
                    v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            if (finalSuper_mode) {
                                if (ViewBlocker.getInstance().isBlocked(mBlockList, v)) {
                                    v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    ViewBlocker.getInstance().block(v);
                                }
                            } else {
                                v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                if (ViewBlocker.getInstance().isBlocked(mBlockList, v)) {
                                    ViewBlocker.getInstance().block(v);
                                }
                            }
                        }
                    });

                }
            };

            XposedBridge.hookAllConstructors(View.class, viewHooker);
            XposedBridge.hookAllConstructors(ViewGroup.class, viewHooker);
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
                            if (DialogBlocker.getInstance().isBlocked(mBlockList, view))
                                windowManager.removeViewImmediate(view);
                        }
                    });


        }
    }

    //--------------------------------------------------------------
    //--------------------------------------------------------------
    //Block包判断
    private boolean isBlockPackage(ArrayList<BlockModel> arrayList, String pkg) {
        for (BlockModel model : arrayList) {
            if (model.packageName.equals(pkg))
                return true;
        }

        return false;
    }

    private boolean isAdapterView(Object v) {
        return v instanceof AdapterView;
    }

    private ViewArg getListView(View v) {
        ViewParent vp = v.getParent();
        //如果它的父布局就是ListView
        if (isAdapterView(v))
            return new ViewArg(vp, v);
        //向上遍历
        while (vp != null) {
            if (isAdapterView(vp.getParent()))
                return new ViewArg(vp.getParent(), (View) vp);
            vp = vp.getParent();
        }
        return null;
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

    private static class BlockModel {
        public String record;
        private String packageName;

        public String getText() {
            return text;
        }

        private String text;
        private String className;
        private boolean enable;


        private BlockModel(String packageName, String record, String text, String className) {
            this.packageName = packageName;
            this.record = record;
            this.text = text;
            this.className = className;
            enable = true;
        }

        private BlockModel(String packageName, String record, String text, String className, boolean enable) {
            this.packageName = packageName;
            this.record = record;
            this.text = text;
            this.className = className;
            this.enable = enable;
        }

        protected static BlockModel fromString(String text) {
            String[] var = text.split("@@@");
            if (var.length == 4) {
                return new BlockModel(var[0], var[1], var[2], var[3]);
            }
            if (var.length == 5) {
                return new BlockModel(var[0], var[1], var[2], var[3], Boolean.valueOf(var[4]));
            }
            return null;
        }

        private static BlockModel getInstanceByAll(View view) {
            return ViewBlocker.getInstance().log(view);
        }

        private void save() {
            Write_Preference(Read_Preference(LIST_FILENAME) + "\n" + toString(), LIST_FILENAME);
        }

        @Override
        public String toString() {
            return String.format(Locale.CHINA, "%s@@@%s@@@%s@@@%s@@@%s", packageName, record, text, className, enable + "");
        }

    }

    private static class ViewModel extends BlockModel {
        private String id;
        private String path;
        private String position;

        private ViewModel(String packageName, String record, String text, String className) {
            super(packageName, record, text, className);
            prepare();
        }

        private ViewModel(String packageName, String record, String text, String className, boolean enable) {
            super(packageName, record, text, className, enable);
            prepare();
        }

        private void prepare() {
            String[] spilted = record.split(ALL_SPLIT);
            id = spilted[0];
            path = spilted[1];
            position = spilted[2];
        }

        public String getId() {
            return id;
        }

        public String getPath() {
            return path;
        }

        public String getPosition() {
            return position;
        }
        protected static BlockModel fromString(String text) {
            String[] var = text.split("@@@");
            if (var.length == 4) {
                return new ViewModel(var[0], var[1], var[2], var[3]);
            }
            if (var.length == 5) {
                return new ViewModel(var[0], var[1], var[2], var[3], Boolean.valueOf(var[4]));
            }
            return null;
        }
    }

    static abstract class Blocker {


        @NonNull
        public abstract BlockModel log(Object o);

        protected abstract Pair<Boolean, Integer> isBlock(ArrayList<BlockModel> arrayList, Object o);

        public abstract void block(Object o);

        final boolean isBlocked(ArrayList<BlockModel> arrayList, Object o) {
            Pair<Boolean, Integer> pair = isBlock(arrayList, o);
            if (onlyOnce && pair.second != null && pair.second >= 0) {
                arrayList.remove((int) pair.second);
            }
            return pair.first;
        }
    }

    static class ViewBlocker extends Blocker {
        private static ViewBlocker instance;

        static ViewBlocker getInstance() {
            if (instance == null)
                instance = new ViewBlocker();
            return instance;
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

        //Also @see ml.qingsu.fuckview.ui.popups.DumpViewerPopupView#getAllText(View)
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
                        if (!allText.equals(""))
                            allText += (((TextView) child).getText().toString().replace("\n", "") + "|");
                    if (child instanceof ViewGroup)
                        allText += getAllText(child);
                }
            return allText;
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

        @NonNull
        @Override
        public BlockModel log(Object o) {
            View view = (View) o;
            return new BlockModel(view.getContext().getPackageName(), view.getId() + ALL_SPLIT + getViewPath(view) + ALL_SPLIT + getViewPosition(view), getText(view), view.getClass().getSimpleName());
        }

        @Override
        protected Pair<Boolean, Integer> isBlock(ArrayList<BlockModel> arrayList, Object o) {
            XposedBridge.log("new View-->" + getAllText((View) o) + "|" + getViewPath((View) o));
//            XposedBridge.log("list  --> "+arrayList);
            return isBlockView(arrayList, (View) o);
        }

        @Override
        public void block(Object o) {
            fuckView((View) o, true);
            XposedBridge.log("净眼:屏蔽一个控件 -->" + getAllText((View) o) + "," + o);
        }

        private Pair<Boolean, Integer> isBlockView(ArrayList<BlockModel> mBlockList, View view) {

            //测试代码，为某个功能做铺垫
//            String path=getViewPath(view);
//            if (path.contains("RecyclerView")||path.contains("ListView")) {
//                return new Pair<>(getAllText(view).contains("广告"), null);
//            }
            final String className = view.getClass().getSimpleName();
            final int id = view.getId();
            final int len = mBlockList.size();

            for (int i = 0; i < len; i++) {
                final BlockModel model = mBlockList.get(i);
                //className都不对，免谈了，直接跳过
                if (!model.className.equals("*") && !model.className.equals(className))
                    continue;
                if (model instanceof ViewModel) {
                    final String path = getViewPath(view);
                    final String postion = getViewPosition(view);
                    if (path.equals(((ViewModel) model).getPath()))
                        return new Pair<>(true, i);
                    if (id != 0 && ((ViewModel) model).getId().equals(id + ""))
                        return new Pair<>(true, i);
                    if (postion.equals(((ViewModel) model).getPosition()))
                        return new Pair<>(true, i);
                    if (!model.getText().equals("")&&model.getText().equals(getText(view)))
                        return new Pair<>(true, i);
                }
            }
            return new Pair<>(false, -1);
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
        protected Pair<Boolean, Integer> isBlock(ArrayList<BlockModel> arrayList, Object o) {
            XposedBridge.log("add View-->" + getAllText((View) o));
            return isBlockDialog(arrayList, (View) o);
        }

        @Override
        public void block(Object o) {
        }

        //Block对话框判断
        private Pair<Boolean, Integer> isBlockDialog(ArrayList<BlockModel> mBlockList, View view) {
            final String pkg = view.getContext().getPackageName();
            final int len = mBlockList.size();
            for (int i = 0; i < len; i++) {
                BlockModel model = mBlockList.get(i);
                if (!model.packageName.equals(pkg) || !model.className.equals(DIALOG_VIRTUAL_CLASSNAME)) {
                    continue;
                }
                if (model.record.equals(getAllText(view))) {
                    return new Pair<>(model.enable, i);
                }
            }
            return new Pair<>(false, -1);
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
            XposedBridge.log("净眼:Removed View -->" + model.record);
            //防止自残
            if (!model.record.equals("") &&
                    !model.record.contains("长按以标记")
                    && !model.record.contains("强制停止应用即可")
                    && !model.record.contains("你确定要屏蔽这一项吗?")
                    && !model.record.contains("继续标记")
                    && !model.record.contains("模式选择")
                    && !model.record.contains("已捕获")) {
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
                view.setFocusable(true);
                view.setClickable(true);
                view.setEnabled(true);
                view.setLongClickable(true);
                view.setOnTouchListener(null);
                view.setOnClickListener(null);
                view.setOnLongClickListener(null);
            } catch (Throwable ignored) {

            }
        }
    }

    private class ViewArg {
        private Object adapterView;
        private View subView;

        private ViewArg(Object adapterView, View subView) {
            this.adapterView = adapterView;
            this.subView = subView;
        }
    }


}
