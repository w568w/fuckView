package ml.qingsu.fuckview.hook;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.Keep;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.hook.blocker.CoolapkBlocker;
import ml.qingsu.fuckview.hook.blocker.GoogleBlocker;
import ml.qingsu.fuckview.hook.blocker.ShareThroughBlocker;
import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.models.CoolApkHeadlineModel;
import ml.qingsu.fuckview.models.ViewModel;
import ml.qingsu.fuckview.utils.ViewUtils;

import static ml.qingsu.fuckview.Constant.PKG_NAME;
import static ml.qingsu.fuckview.utils.ViewUtils.getAllText;
import static ml.qingsu.fuckview.utils.ViewUtils.getText;
import static ml.qingsu.fuckview.utils.ViewUtils.getViewId;
import static ml.qingsu.fuckview.utils.ViewUtils.getViewPath;
import static ml.qingsu.fuckview.utils.ViewUtils.getViewPosition;

/**
 * w568w on 2017-6-30.
 *
 * @author w568w
 */

public class Hook {


    private static final String DIALOG_VIRTUAL_CLASSNAME = "Dialog";
    private static final String SUPER_MODE_NAME = "super_mode";
    private static final String ONLY_ONCE_NAME = "only_once";
    private static final String STANDARD_MODE_NAME = "standard_mode";
    private static final String ENABLE_LOG_NAME = "enable_log";
    private static final String PACKAGE_NAME_NAME = "package_name";
    private static final String LIST_FILENAME = "block_list";
    private static final String BROADCAST_ACTION = "tooYoungtooSimple";
    private static final String ALL_SPLIT = "~~~";
    private static final String RECEIVER_KEY = "motherfuckerreceiver";
    private static final int NOTIFICATION_ID = 0x123;

    private static boolean onlyOnce;
    private static boolean standardMode;
    private static boolean superMode;
    private static boolean enableLog;

    private static XSharedPreferences xSP = new XSharedPreferences("ml.qingsu.fuckview", "data");

    /**
     * @removed Nowhere to use.
     */
    private static int mNotificationId = NOTIFICATION_ID + 1;

    private static String getString(int resId, Context context) throws PackageManager.NameNotFoundException {
        try {
            Context appContext = context.createPackageContext("ml.qingsu.fuckview", Context.CONTEXT_IGNORE_SECURITY);
            return appContext.getResources().getString(resId);
        } catch (NullPointerException e) {
            return null;
        }
    }

    // FIXME: 18-4-21 These two methods are the same.

    private void handleClick(final View view) throws PackageManager.NameNotFoundException {
        final Context context = view.getContext();
        //增加红框
        addViewShape(view);
        //增加接收器
        try {
            ViewReceiver receiver;
            if ((receiver = (ViewReceiver) XposedHelpers.getAdditionalInstanceField(view, RECEIVER_KEY)) == null) {
                receiver = (ViewReceiver) ViewReceiver.createAndRegisterReceiver(ViewReceiver.class, context);
                XposedHelpers.setAdditionalInstanceField(view, RECEIVER_KEY,
                        receiver);
            }
            receiver.setView(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //显示通知栏
        //设置Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(PKG_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("cache", "\n" + ViewBlocker.getInstance().log(view).toString());
        intent.putExtra("Dialog", true);
        //Fix: NullPointerE in VAEXposed.
        //VAEXposed throws a exception with chain styles.
        //So you cannot write:
        //nb.xxx().xxx();
        //You should write:
        //nb.xxx();nb.xxx();

        //TODO too much trying.
        try {
            NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
            nb.setAutoCancel(true);
            nb.setTicker(getString(R.string.captured, context));
            nb.setSmallIcon(android.R.drawable.stat_sys_warning);
            nb.setContentText(ViewUtils.getClassName(view.getClass()));
            nb.setOngoing(false);
            nb.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            nb.setContentTitle(getString(R.string.notification_title, context));
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, nb.build());
        } catch (NullPointerException npe) {
            npe.printStackTrace();

        } catch (Throwable t) {
            /*
            Deal with Class Not Found thrown when creating NotificationCompat in [Exposed](https://github.com/android-hacker/exposed) Environment.
            But it's not supported in API 10 and below, todo it.
            */
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Notification.Builder nb = new Notification.Builder(context);
                    nb.setAutoCancel(true);
                    nb.setTicker(getString(R.string.captured, context));
                    nb.setSmallIcon(android.R.drawable.stat_sys_warning);
                    nb.setContentText(ViewUtils.getClassName(view.getClass()));
                    nb.setOngoing(false);
                    nb.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                    nb.setContentTitle(getString(R.string.notification_title, context));
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(NOTIFICATION_ID, nb.getNotification());
                }
                //Must ignored it.On some devices,the codes above still cannot work properly.
            } catch (Throwable ignored) {
            }
        }

        Intent broadcastIntent = new Intent(BROADCAST_ACTION)
                .putExtra("height", view.getHeight())
                .putExtra("width", view.getWidth())
                .putExtra("className", ViewUtils.getClassName(view.getClass()))
                .putExtra("record", ViewBlocker.getInstance().log(view).toString());
        context.sendBroadcast(broadcastIntent);
    }

    /**
     * @param view the view to be handled
     * @throws PackageManager.NameNotFoundException when
     */
    private void handleTouch(final View view) throws PackageManager.NameNotFoundException {
        final Context context = view.getContext();
        //增加红框
        addViewShape(view);
        //增加接收器
        try {
            ViewReceiver receiver;
            if ((receiver = (ViewReceiver) XposedHelpers.getAdditionalInstanceField(view, RECEIVER_KEY)) == null) {
                receiver = (ViewReceiver) ViewReceiver.createAndRegisterReceiver(ViewReceiver.class, context);
                XposedHelpers.setAdditionalInstanceField(view, RECEIVER_KEY,
                        receiver);
            }
            receiver.setView(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //显示通知栏
        //设置Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(PKG_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //看！奇巧淫技x2！
        log(ViewBlocker.getInstance().log(view).toString());
        intent.putExtra("cache", "\n" + ViewBlocker.getInstance().log(view).toString());
        intent.putExtra("Dialog", true);
        //TODO too much trying.
        try {
            NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
            nb.setAutoCancel(true);
            nb.setTicker(getString(R.string.captured, context));
            nb.setSmallIcon(android.R.drawable.stat_sys_warning);
            nb.setContentText(ViewUtils.getClassName(view.getClass()));
            nb.setOngoing(false);
            nb.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            nb.setContentTitle(getString(R.string.notification_title, context));
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, nb.build());
        } catch (NullPointerException npe) {
            npe.printStackTrace();

        } catch (Throwable t) {
            /*
            Deal with Class Not Found thrown when creating NotificationCompat in [Exposed](https://github.com/android-hacker/exposed) Environment.
            But it's not supported in API 10 and below,let's TODO it.
            */
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Notification.Builder nb = new Notification.Builder(context);
                    nb.setAutoCancel(true);
                    nb.setTicker(getString(R.string.captured, context));
                    nb.setSmallIcon(android.R.drawable.stat_sys_warning);
                    nb.setContentText(ViewUtils.getClassName(view.getClass()));
                    nb.setOngoing(false);
                    nb.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                    nb.setContentTitle(getString(R.string.notification_title, context));
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(NOTIFICATION_ID, nb.getNotification());
                }
                //Must ignored it.On some devices,the codes above still cannot work properly.
            } catch (Throwable ignored) {
            }
        }

        //BroadcastReceiver
        Intent broadcastIntent = new Intent(BROADCAST_ACTION)
                .putExtra("height", view.getHeight())
                .putExtra("width", view.getWidth())
                .putExtra("className", ViewUtils.getClassName(view.getClass()))
                .putExtra("record", ViewBlocker.getInstance().log(view).toString());
        context.sendBroadcast(broadcastIntent);
    }


    /**
     * 给View加上红边~~
     *
     * @param view the view to be bounded
     */
    private static void addViewShape(final View view) {
        try {
            GradientDrawable gd = new GradientDrawable();
            gd.setStroke(4, Color.RED);
            final Drawable background = view.getBackground();
            view.setBackgroundDrawable(gd);
            //Then recover the background.
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setBackgroundDrawable(background);
                }
            }, 800);
        } catch (Throwable ignored) {

        }
    }

    /**
     * @param pkgFilter the specified package name.
     * @return a blocking list.
     */
    private static ArrayList<BlockModel> readBlockList(String pkgFilter) {
        ArrayList<BlockModel> list = new ArrayList<>();
        ArrayList<String> lines = readPreferenceByLine(LIST_FILENAME);
        for (String line : lines) {

            BlockModel model = BlockModel.fromString(line);
            if (model.record.contains(ALL_SPLIT)) {
                if (CoolApkHeadlineModel.isInstance(line)) {
                    model = CoolApkHeadlineModel.fromString(line);
                } else if (ViewModel.isInstance(line)) {
                    model = ViewModel.fromString(line);
                }
            }
            if (model != null && model.packageName.equals(pkgFilter)) {
                list.add(model);
            }
        }
        return list;
    }

    /**
     * 正常的readfile,不做任何缓存代理
     */
    private static ArrayList<String> readPreferenceByLine(String filename) {
        String data = xSP.getString(filename, "");
        ArrayList<String> arrayList = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (!"".equals(line)) {
                arrayList.add(line);
            }
        }
        return arrayList;
    }

    /**
     * @param length the array's length
     * @param array  a zero-length array.
     * @param <E>    type
     * @return an array.
     */
    @SafeVarargs
    private static <E> E[] newArray(int length, E... array) {
        return Arrays.copyOf(array, length);
    }

    public static void log(String text) {
        if (enableLog) {
            XposedBridge.log(text);
        }
    }

    /**
     * The main hooker.
     *
     * @param loadPackageParam the param of the package loaded.
     * @throws Throwable Some exceptions.
     */
    @Keep
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam == null) {
            return;
        }
        final ArrayList<BlockModel>[] mBlockList;
        mBlockList = newArray(1);
        xSP.reload();
        xSP.makeWorldReadable();
        xSP.getFile().setWritable(true);
        String pkg = xSP.getString(PACKAGE_NAME_NAME, "");

        //读取设置
        try {
            superMode = Boolean.valueOf(xSP.getString(SUPER_MODE_NAME, String.valueOf(false)));
            onlyOnce = Boolean.valueOf(xSP.getString(ONLY_ONCE_NAME, String.valueOf(false)));
            standardMode = Boolean.valueOf(xSP.getString(STANDARD_MODE_NAME, String.valueOf(true)));
            enableLog = Boolean.valueOf(xSP.getString(ENABLE_LOG_NAME, String.valueOf(true)));
        } catch (Exception e) {
            e.printStackTrace();
            superMode = false;
            onlyOnce = false;
            standardMode = true;
            enableLog = true;
        }
        log("净眼:开始HOOK --> " + loadPackageParam.packageName);


        log("净眼:检测模块正常 -->" + pkg);
        if ((pkg != null && loadPackageParam.packageName.equals(pkg))) {
            if (standardMode) {
                log("净眼:hook -->setOnClickListener");

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
                                    handleClick(view);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                //如果是ListView中的项目，手动CALL一次监听器
                                ViewArg model = getListView(view);
                                if (model != null) {
                                    log("in ListView!");
                                    try {
                                        int position = (int) XposedHelpers.callMethod(model.adapterView, "getPositionForView", new Class[]{View.class}, model.subView);
                                        long id = (long) XposedHelpers.callMethod(model.adapterView, "getItemIdAtPosition", new Class[]{int.class}, position);
                                        XposedHelpers.callMethod(model.adapterView, "performItemClick", new Class[]{View.class, int.class, int.class}, model.subView, position, id);
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (clickListener != null) {
                                    clickListener.onClick(view);
                                }
                            }
                        };
                    }
                });
            } else {
                log("净眼:hook -->setOnTouchListener");
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
                                {
                                    try {
                                        handleTouch(view);
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return touchListener != null && touchListener.onTouch(view, motionEvent);
                            }
                        };
                    }
                });
            }
            log("净眼:hook -->setClickable");
            XposedHelpers.findAndHookMethod(View.class, "setClickable", boolean.class, new BooleanSetterHooker(true));
            //XposedHelpers.findAndHookMethod(View.class, "isClickable", XC_MethodReplacement.returnConstant(true));
            log("净眼:hook -->setLongClickable");
            XposedHelpers.findAndHookMethod(View.class, "setLongClickable", boolean.class, new BooleanSetterHooker(true));
            log("净眼:hook -->View<init>");
            XposedBridge.hookAllConstructors(View.class, new ConstructorHooker());
            try {
                XposedHelpers.findAndHookMethod("android.view.WindowManagerGlobal", Dialog.class.getClassLoader(), "removeView", View.class, boolean.class, new WindowHooker());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            XposedHelpers.findAndHookMethod("android.view.WindowManagerImpl", Dialog.class.getClassLoader(), "removeView", View.class, new WindowHooker());
            XposedHelpers.findAndHookMethod("android.view.WindowManagerImpl", Dialog.class.getClassLoader(), "removeViewImmediate", View.class, new WindowHooker());
        }

        /*
        ------------------------------------标记部分结束，以下为拦截部分------------------------
        ------------------------------------华丽的分割线----------------------------------------
        读取屏蔽列表
        */
        mBlockList[0] = readBlockList(loadPackageParam.packageName);

        //以下为Hook
        //对话框取消那个APP，其实核心就这一行代码...
        log("净眼:hook -->setCancelable");
        XposedHelpers.findAndHookMethod(Dialog.class, "setCancelable", boolean.class, new BooleanSetterHooker(true));
        if (isBlockPackage(mBlockList[0], loadPackageParam.packageName) && !loadPackageParam.packageName.equals(pkg)) {
            XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    //Reload it!
                    mBlockList[0] = readBlockList(loadPackageParam.packageName);
                }

            });
            XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    View v = (View) param.thisObject;
                    if ((int) param.args[0] == View.GONE) {
                        return;
                    }
                    if (ViewBlocker.getInstance().isBlocked(mBlockList[0], v)) {
                        param.args[0] = View.GONE;
                        ViewBlocker.getInstance().block(v);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    TextView v = (TextView) param.thisObject;

                    if (ViewBlocker.getInstance().isBlocked(mBlockList[0], v)) {
                        ViewBlocker.getInstance().block(v);
                    }
                }
            });
            XposedHelpers.findAndHookMethod(View.class, "setLayoutParams", ViewGroup.LayoutParams.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) param.args[0];
                    if (layoutParams != null) {
                        if (layoutParams.height == 0 && layoutParams.width == 0) {
                            return;
                        }
                        if (ViewBlocker.getInstance().isBlocked(mBlockList[0], param.thisObject)) {
                            ViewBlocker.getInstance().block(param.thisObject);
                        }
                    }
                }
            });

            final boolean fsuperMode = superMode;
            final XC_MethodHook viewHooker = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    final View v = (View) param.thisObject;
                    v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (fsuperMode) {
                                if (ViewBlocker.getInstance().isBlocked(mBlockList[0], v)) {
                                    v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    ViewBlocker.getInstance().block(v);
                                }
                            } else {
                                v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                if (ViewBlocker.getInstance().isBlocked(mBlockList[0], v)) {
                                    ViewBlocker.getInstance().block(v);
                                }
                            }
                        }
                    });

                }
            };

            XposedBridge.hookAllConstructors(View.class, viewHooker);
            XposedBridge.hookAllConstructors(ViewGroup.class, viewHooker);
            log("净眼:hook -->addView");

            //Dialog blocking
            XposedHelpers.findAndHookMethod("android.view.WindowManagerImpl", loadPackageParam.classLoader, "addView",
                    View.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            View view = (View) param.args[0];
                            WindowManager windowManager = (WindowManager) param.thisObject;
                            if (view == null) {
                                return;
                            }
                            if (DialogBlocker.getInstance().isBlocked(mBlockList[0], view)) {
                                windowManager.removeViewImmediate(view);
                            }
                        }
                    });
        }
    }


    //--------------------------------------------------------------
    //--------------------------------------------------------------

    /**
     * @param arrayList the blocking list
     * @param pkg       package name
     * @return whether the app has blocking rules
     */
    private boolean isBlockPackage(ArrayList<BlockModel> arrayList, String pkg) {
        final int len = arrayList.size();
        for (int i = 0; i < len; i++) {
            final BlockModel model = arrayList.get(i);
            if (model.packageName.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param v A view.
     * @return whether it's a listview.
     */
    private boolean isAdapterView(Object v) {
        return v instanceof AdapterView;
    }

    private ViewArg getListView(View v) {
        ViewParent vp = v.getParent();
        //如果它的父布局就是ListView
        if (isAdapterView(v)) {
            return new ViewArg(vp, v);
        }
        //向上遍历
        while (vp != null) {
            if (isAdapterView(vp.getParent())) {
                return new ViewArg(vp.getParent(), (View) vp);
            }
            vp = vp.getParent();
        }
        return null;
    }

    static abstract class AbstractBlocker {


        /**
         * @param o 需要被记录的对象
         * @return 记录Model
         */
        @NonNull
        public abstract BlockModel log(Object o);

        /**
         * @param arrayList 记录列表
         * @param o         需要检查的对象
         * @return 是否需要屏蔽
         */
        protected abstract Pair<Boolean, Integer> isBlock(ArrayList<BlockModel> arrayList, Object o);

        /**
         * @param o 需要屏蔽的对象
         */
        public abstract void block(Object o);

        public final boolean isBlocked(ArrayList<BlockModel> arrayList, Object o) {
            Pair<Boolean, Integer> pair = isBlock(arrayList, o);
            if (onlyOnce && pair.second != null && pair.second >= 0) {
                BlockModel blockModel = arrayList.remove((int) pair.second);
                Hook.log("净眼:删除规则 -->" + blockModel);
            }
            return pair.first;
        }
    }

    public static class ViewBlocker extends AbstractBlocker {
        private static ViewBlocker instance;

        public static ViewBlocker getInstance() {
            if (instance == null) {
                instance = new ViewBlocker();
            }
            return instance;
        }

        @NonNull
        @Override
        public BlockModel log(Object o) {
            View view = (View) o;
            return new BlockModel(view.getContext().getPackageName(), view.getId() + ALL_SPLIT + getViewPath(view) + ALL_SPLIT + getViewPosition(view), getText(view), ViewUtils.getClassName(view.getClass()));
        }

        private static boolean singleStr(String str, char a) {
            final int length = str.length();
            boolean appeared = false;
            for (int i = 0; i < length; i++) {
                if (str.charAt(i) == a) {
                    if (appeared) {
                        return false;
                    } else {
                        appeared = true;
                    }
                }
            }
            return appeared;
        }

        /**
         * Be serious on time.
         * Be serious on time.
         * Be serious on time.
         * Think about it that will be invoked around 6,0000 times on every time of starting.
         */
        @Override
        protected Pair<Boolean, Integer> isBlock(ArrayList<BlockModel> mBlockList, Object o) {
            final View view = (View) o;

            final String className = ViewUtils.getClassName(view.getClass());
            final int id = view.getId();
            final String ids = getViewId(view);

            CoolapkBlocker.getInstance().setList(mBlockList);
            //Block
            GoogleBlocker.getInstance().treatOrBlock(view, ids, className);
            CoolapkBlocker.getInstance().treatOrBlock(view, ids, className);
            ShareThroughBlocker.getInstance().treatOrBlock(view, ids, className);

            final String strId = id + "";
            final int len = mBlockList.size();
            final String postion = getViewPosition(view);
            final String p = getViewPath(view);
            if (singleStr(p, '/')) {
                return new Pair<>(false, -1);
            }
            for (int i = 0; i < len; i++) {
                final BlockModel model = mBlockList.get(i);
                //className都不对，免谈了，直接跳过
                if (model.className.length() != 0 && model.className.charAt(0) != '*' && !model.className.equals(className)) {
                    continue;
                }
                if (model instanceof ViewModel) {
                    final String path = getViewPath(view);
                    int successTimes = 0;
                    if (path.equals(((ViewModel) model).getPath())) {
                        ++successTimes;
                    }
                    if (id != 0 && id != android.R.id.text1 && id != android.R.id.text2 && ((ViewModel) model).getId().equals(strId)) {
                        ++successTimes;
                    }
                    if (postion.equals(((ViewModel) model).getPosition())) {
                        successTimes += 2;
                    }
                    if (model.text.length() != 0 && model.text.equals(getText(view))) {
                        ++successTimes;
                    }
                    if (successTimes >= 2) {
                        return new Pair<>(true, i);
                    }
                }
            }
            return new Pair<>(false, -1);
        }

        @Override
        public void block(Object o) {
            View v = (View) o;
            try {
                final ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.height = 0;
                    layoutParams.width = 0;
                    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(0, 0, 0, 0);
                    }
                    v.setLayoutParams(layoutParams);
                }
                v.setPadding(0, 0, 0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    v.setAlpha(0f);
                }
                v.setVisibility(View.GONE);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    private static class DialogBlocker extends AbstractBlocker {
        private static DialogBlocker instance;

        static DialogBlocker getInstance() {
            if (instance == null) {
                instance = new DialogBlocker();
            }
            return instance;
        }

        @NonNull
        @Override
        public BlockModel log(Object o) {
            return new BlockModel(((View) o).getContext().getPackageName(), getAllText((View) o), "", DIALOG_VIRTUAL_CLASSNAME);
        }

        @Override
        protected Pair<Boolean, Integer> isBlock(ArrayList<BlockModel> arrayList, Object o) {
            return isBlockDialog(arrayList, (View) o);
        }

        @Override
        public void block(Object o) {
        }

        /**
         * Block对话框判断
         */
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


    private class WindowHooker extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            if (param.args.length == 0 || param.args[0] == null) {
                return;
            }
            View view = (View) param.args[0];
            log("净眼:Message --> RemoveView");
            final BlockModel model = DialogBlocker.getInstance().log(view);
            log("净眼:Removed View -->" + model.record);
            //todo no measures are taken here.
        }
    }

    private class BooleanSetterHooker extends XC_MethodHook {
        private boolean mSetValue;

        BooleanSetterHooker(boolean s) {
            super();
            mSetValue = s;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            param.args[0] = mSetValue;
        }
    }

    private class ConstructorHooker extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            View view = (View) param.thisObject;

            // java.lang.RuntimeException:
            // Don't call setOnClickListener for an AdapterView.
            // You probably want setOnItemClickListener() instead.

            if (isAdapterView(view) || view == null) {
                return;
            }
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
