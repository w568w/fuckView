package ml.qingsu.fuckview.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import ml.qingsu.fuckview.base.BaseAppCompatActivity;
import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.models.ViewModel;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.ui.fragments.CheckerFragment;
import ml.qingsu.fuckview.ui.fragments.MainFragment;
import ml.qingsu.fuckview.ui.fragments.OnlineRulesFragment;
import ml.qingsu.fuckview.ui.fragments.WelcomeFragment;
import ml.qingsu.fuckview.ui.fragments.select_app.SelectAppWizard;
import ml.qingsu.fuckview.utils.ConvertUtils;
import ml.qingsu.fuckview.utils.FirstRun;

/**
 * @author w568w
 */
public class MainActivity extends BaseAppCompatActivity {
    public boolean shouldShowFAQ = false;

    private static final int REQUEST_PERMISSION = 0x123;
    private static final int REQUEST_NEW_FRAGMENT = 0x124;
    public static final String ALL_SPLIT = "~~~";
    private static SharedPreferences sSharedPreferences;
    public Fragment currentFragment;

    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("theme", false)) {
            setTheme(R.style.DayTheme);
        }
        setContentView(R.layout.activity_main);
        checkAndCallPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, R.string.cant_open_popup, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse(String.format("package:%s", getPackageName()))));
            }
        }
        sSharedPreferences = getSharedPreferences("data", Context.MODE_WORLD_READABLE);

        dealWithIntent();
        //If it is the first time to run...
        if (FirstRun.isFirstRun(this, "app")) {
            setFragmentWithoutBack(new WelcomeFragment());
            //else if there's no rule...
        } else if ("".equals(readPreferences(Constant.LIST_NAME))) {
            setFragmentWithoutBack(new SelectAppWizard());
            if (!isModuleActive()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.xposed_is_unabled)
                        .setMessage(R.string.enable_module)
                        .setPositiveButton(R.string.OK, null)
                        .show();
            }
            //else we go to the main fragment...
        } else {
            setFragmentWithoutBack(new MainFragment());
            if (!isModuleActive()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.xposed_is_unabled)
                        .setMessage(R.string.enable_module)
                        .setPositiveButton(R.string.OK, null)
                        .show();
            }
        }


    }

    private void dealWithIntent() {
        writePreferences("", Constant.PACKAGE_NAME_NAME);

        //此处详见Hook.java
        //是否从通知栏里点过来
        boolean isDialog = getIntent().getBooleanExtra("Dialog", false);
        String cache = getIntent().getStringExtra("cache");
        if (cache == null) {
            return;
        }
        if (isDialog) {
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

    private void checkAndCallPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            try {
                ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.no_file_ro_permission, Toast.LENGTH_SHORT).show();
            }
        } else {
            //Process old-style rules
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    processFile();
                }
            });
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
        //为啥不用switch呢？
        //懒....
        if (item.getItemId() == R.id.action_about) {
            setFragment(new CheckerFragment());
        } else if (item.getItemId() == R.id.action_settings) {
            startActivityForResult(new Intent(this, PreferencesActivity.class), REQUEST_NEW_FRAGMENT);
        }
        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<BlockModel> read() {
        ArrayList<BlockModel> list = new ArrayList<>();

        ArrayList<String> lines = readPreferenceByLine(Constant.LIST_NAME);
        for (String str : lines) {
            BlockModel model = BlockModel.fromString(str);
            if (model == null) {
                continue;
            }
            if (model.record.contains(ALL_SPLIT)) {
                model = ViewModel.fromString(str);
            } else {
                //轉換老版(0.8.5-)規則到新版
                model = ViewModel.fromString(ConvertUtils.oldToNew(model).toString());
            }
            if (model != null) {
                list.add(model);
            }
        }
        return list;
    }

    private void processFile() {
        final File oldFile = new File(getSDPath() + "/fuckview/" + Constant.LIST_NAME);
        if (oldFile.exists()) {
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
     * @return SD卡路径
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

    public static boolean isModuleActive() {
        return false;
    }

}
