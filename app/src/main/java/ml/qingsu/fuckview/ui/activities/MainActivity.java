package ml.qingsu.fuckview.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.MyApplication;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.base.BaseAppCompatActivity;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.models.ViewModel;
import ml.qingsu.fuckview.receiver.BootCompleteReceiver;
import ml.qingsu.fuckview.ui.fragments.CheckerFragment;
import ml.qingsu.fuckview.ui.fragments.MainFragment;
import ml.qingsu.fuckview.ui.fragments.OnlineRulesFragment;
import ml.qingsu.fuckview.ui.fragments.WelcomeFragment;
import ml.qingsu.fuckview.ui.popups.guide.GuidePopupToast;
import ml.qingsu.fuckview.utils.FirstRun;
import ml.qingsu.fuckview.utils.ViewUtils;

import static ml.qingsu.fuckview.Constant.COOLAPK_MARKET_PKG_NAME;
import static ml.qingsu.fuckview.Constant.KEY_DONT_SHOW_RATE_DIALOG;
import static ml.qingsu.fuckview.Constant.KEY_THEME;
import static ml.qingsu.fuckview.ui.activities.PreferencesActivity.RESULT_GUIDE;

/**
 * @author w568w
 */
public class MainActivity extends BaseAppCompatActivity {

    public boolean shouldShowFAQ = false;
    private boolean hasShownGuide = false;
    private static final int REQUEST_PERMISSION = 0x123;
    private static final int REQUEST_NEW_FRAGMENT = 0x124;
    public static final String ALL_SPLIT = "~~~";
    private static SharedPreferences sSharedPreferences;
    private SharedPreferences mSettings;
    public Fragment currentFragment;

    public static boolean isModuleActive() {
        Log.d("asklmx", "asduiwhledlweeejkdwa");
        Log.d("asdewrde", "asdufewfwefiwhledlweeejkdwa");
        Log.i("asjsixask", "iufwehuiidw");
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && isExpModuleActive(MyApplication.getContext());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static boolean isExpModuleActive(Context context) {

        boolean isExp = false;
        if (context == null) {
            throw new IllegalArgumentException("context must not be null!!");
        }

        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = null;
            try {


                result = contentResolver.call(uri, "active", null, null);

            } catch (RuntimeException e) {
                // TaiChi is killed, try invoke
                try {
                    Intent intent = new Intent("me.weishu.exp.ACTION_ACTIVE");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Throwable e1) {
                    return false;
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, "active", null, null);
            }

            if (result == null) {
                return false;
            }
            isExp = result.getBoolean("active", false);
        } catch (Throwable ignored) {
        }
        return isExp;
    }

    public static boolean isDayTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_THEME, false);
    }

    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        //Setting the theme
        if (isDayTheme(this)) {
            setTheme(R.style.DayTheme);
        }
        setContentView(R.layout.activity_main);

        checkAndCallPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, R.string.cant_open_popup, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse(String.format("package:%s", getPackageName()))));
                finish();
            }
        }


        sSharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);

        dealWithIntent();
        //keepAlive();
        //If it is the first time to run...
        if (FirstRun.isFirstRun(this, "app")) {
            // if(true){
            //setFragmentWithoutBack(new WelcomeFragment());
            //else if there's no rule...
        } else {
            setFragmentWithoutBack(new MainFragment());
            if (isModuleActive()) {
                giveMeFive();
            } else {
                checkModuleActivated();
            }
        }
    }

//    private void keepAlive() {
//        if (mSettings.getBoolean("keep_alive", true)) {
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(Intent.ACTION_SCREEN_ON);
//            filter.addAction(Intent.ACTION_SCREEN_OFF);
//            try {
//                //registerReceiver(new BootCompleteReceiver(), filter);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

    private void startGuide() {

        new GuidePopupToast(this).show();
    }

    private void checkModuleActivated() {
        if (!isModuleActive() && !mSettings.getBoolean(Constant.KEY_DONT_SHOW_ACTIVE_DIALOG, false)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.xposed_is_unabled)
                    .setMessage(R.string.enable_module)
                    .setPositiveButton(R.string.OK, null)
                    .setNegativeButton(R.string.dont_show_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSettings.edit()
                                    .putBoolean(Constant.KEY_DONT_SHOW_ACTIVE_DIALOG, true).apply();
                        }
                    })
                    .show();
        }
    }

    private void dealWithIntent() {
        writePreferences("", Constant.PACKAGE_NAME_NAME);

        //Does the activity start from the notification bar?
        boolean isFromNotification = getIntent().getBooleanExtra("Dialog", false);
        String cache = getIntent().getStringExtra("cache");
        if (cache == null) {
            return;
        }
        if (isFromNotification) {
            final ViewModel blockInfo = ViewModel.fromString(cache);
            if (blockInfo == null) {
                return;
            }
            blockInfo.save();

        } else {
            appendPreferences(cache, Constant.LIST_NAME);
        }
        Toast.makeText(this, R.string.rule_saved, Toast.LENGTH_SHORT).show();
    }

    private void giveMeFive() {

        if (!mSettings.getBoolean(KEY_DONT_SHOW_RATE_DIALOG, false)) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.ask_for_rating)
                    .setPositiveButton(R.string.thats_okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Only published in CoolApk Market, Github and Xposed until now
                            if (hasPackage(COOLAPK_MARKET_PKG_NAME)) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                            } else {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/w568w/fuckView")));
                            }
                        }
                    })
                    .setNegativeButton(R.string.fuck, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, R.string.feel_bad, Toast.LENGTH_LONG).show();
                            mSettings.edit().putBoolean(KEY_DONT_SHOW_RATE_DIALOG, true).apply();
                        }
                    }).show();
        }
    }

    private boolean hasPackage(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !hasShownGuide) {
            //startGuide();
            hasShownGuide = true;
        }
        if (hasFocus) {
            if (mSettings.getBoolean(KEY_THEME, false)) {
                ViewUtils.setActionBarTextColor(this, Color.WHITE);
            }
        }
    }

    private void checkAndCallPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            //Process old-style rules
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    processFile();

                }
            });
        } else {
            try {
                ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.no_file_ro_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0) {
            return;
        }
        if (requestCode == REQUEST_PERMISSION
                && Build.VERSION.SDK_INT >= 23
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            checkAndCallPermission(permissions[0]);
        } else {
            processFile();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEW_FRAGMENT && resultCode == RESULT_OK) {
            setFragment(new OnlineRulesFragment());
        } else if (requestCode == REQUEST_NEW_FRAGMENT && resultCode == RESULT_GUIDE) {
            startGuide();
        }
    }

    public void setFragmentWithoutBack(Fragment fragment) {
        setFragment(fragment, false);
    }

    public void setFragment(Fragment fragment) {
        setFragment(fragment, true);
    }

    private void setFragment(Fragment fragment, boolean backable) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl, fragment);
        if (backable) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
        currentFragment = fragment;
        shouldShowFAQ = fragment instanceof Searchable;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_about).setVisible(shouldShowFAQ);
        menu.findItem(R.id.action_search).setVisible(shouldShowFAQ);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (shouldShowFAQ) {
                    ((Searchable) currentFragment).setSearchText(newText);
                }
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            setFragment(new CheckerFragment());
            //Debug.stopMethodTracing();
        } else if (item.getItemId() == R.id.action_settings) {
            startActivityForResult(new Intent(this, PreferencesActivity.class), REQUEST_NEW_FRAGMENT);
        }
        return super.onOptionsItemSelected(item);
    }

    private void processFile() {
        final File oldFile = new File(getSDPath() + "/fuckview/" + Constant.LIST_NAME);
        if (oldFile.exists()) {
            //Do not need to be translated
            //because (I'm lazy and) the version 0.8.3.1 and below can never be downloaded out of a Chinese App Market named CoolApk.
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("检测到您使用过版本0.8.3.1之前的净眼，是否要更新规则位置？")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            appendPreferences("\n" + readFile("fuckview/" + Constant.LIST_NAME), Constant.LIST_NAME);
                            oldFile.delete();
                            Toast.makeText(MainActivity.this, "更新已完成.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "注意!\n净眼将暂时无法正常标记和屏蔽，要更新规则，请重新打开净眼。", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    }


    public static void appendPreferences(String data, String filename) {
        writePreferences(readPreferences(filename) + data, filename);
    }

    public static String readFile(String filename) {
        final String sdPath = getSDPath() + "/";
        File f = new File(sdPath + filename);
        if (!f.exists()) {
            return "";
        }
        String result = "";
        try {
            FileInputStream is = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufReader = new BufferedReader(isr);
            String line;
            while ((line = bufReader.readLine()) != null) {
                result += ("\n" + line);
            }

            bufReader.close();
            isr.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void writePreferences(String data, String filename) {
        sSharedPreferences.edit().putString(filename, data).apply();
    }

    /**
     * @return SD card Path (only when it's readable) or External Storage Path
     */
    @Nullable
    public static String getSDPath() {
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

    public static String readPreferences(String filename) {
        return sSharedPreferences.getString(filename, "");
    }

    private void changeIcon(String activityPath) {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, activityPath),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    public static ArrayList<String> readPreferenceByLine(String filename) {
        String data = readPreferences(filename);
        ArrayList<String> arrayList = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (!"".equals(line)) {
                arrayList.add(line);
            }
        }
        return arrayList;
    }

}
