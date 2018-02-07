package ml.qingsu.fuckview.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import ml.qingsu.fuckview.models.BlockModel;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.models.ViewModel;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.ui.fragments.CheckerFragment;
import ml.qingsu.fuckview.ui.fragments.MainFragment;
import ml.qingsu.fuckview.ui.fragments.OnlineRulesFragment;
import ml.qingsu.fuckview.ui.fragments.faq.Faq;
import ml.qingsu.fuckview.ui.fragments.WelcomeFragment;
import ml.qingsu.fuckview.ui.fragments.select_app.SelectAppWizard;
import ml.qingsu.fuckview.utils.ConvertUtils;
import ml.qingsu.fuckview.utils.FirstRun;

public class MainActivity extends AppCompatActivity {
    public boolean shouldShowFAQ = false;


    public static final String LIST_NAME = "block_list";
    public static final String SUPER_MODE_NAME = "super_mode";
    public static final String ONLY_ONCE_NAME = "only_once";
    public static final String STANDARD_MODE_NAME = "standard_mode";
    public static final String ENABLE_LOG_NAME = "enable_log";

    public static final String PACKAGE_NAME_NAME = "package_name";

    private static final int REQUEST_PERMISSION = 0x123;
    private static final int REQUEST_NEW_FRAGMENT = 0x124;
    public static final String ALL_SPLIT = "~~~";
    private static SharedPreferences mSharedPreferences;
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
        mSharedPreferences = getSharedPreferences("data", Context.MODE_WORLD_READABLE);

        dealWithIntent();
        if (FirstRun.isFirstRun(this, "app")) {
            setFragmentWithoutBack(new WelcomeFragment());
        } else if (Read_Preferences(LIST_NAME).equals("")) {
            setFragmentWithoutBack(new SelectAppWizard());
            if (!isModuleActive()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.xposed_is_unabled)
                        .setMessage(R.string.enable_module)
                        .setPositiveButton(R.string.OK, null)
                        .show();
            }
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
        Write_Preferences("", PACKAGE_NAME_NAME);

        //此处详见Hook.java
        //是否从通知栏里点过来
        boolean isDialog = getIntent().getBooleanExtra("Dialog", false);
        String cache = getIntent().getStringExtra("cache");
        if (cache == null) return;
        if (isDialog) {

            final ViewModel blockInfo = ViewModel.fromString(cache);
            if (blockInfo == null) return;
            blockInfo.save();

        } else {
            Append_Preferences(cache, LIST_NAME);
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
                    process_file();
                }
            });
        }
    }
    //Useless?

//    private void checkFile() {
//        final String sdPath = File_Get_SD_Path() + "/";
//        File dir = new File(sdPath + DIR_NAME);
//        if (!dir.exists()) {
//            if (!dir.mkdirs() && !dir.mkdir()) {
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("错误")
//                        .setMessage("创建文件夹失败!\n请检查您是否限制了净眼创建文件夹的权限。\n\n提示:您也可以手动在SD卡中创建fuckView(注意大小写)文件夹并重试。")
//                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                finish();
//                            }
//                        })
//                        .show();
//            }
//        }
//        final File oldFile = new File(sdPath + LIST_FILE_NAME);
//        if (oldFile.exists()) {
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("检测到旧文件")
//                    .setMessage("检测到你曾经使用过0.7.1及之前版本的 净眼，是否要更新规则位置?")
//                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Append_Preferences(Read_Preferences(LIST_FILE_NAME), LIST_NAME);
//                            oldFile.delete();
//                            File oldFile = new File(sdPath + SUPER_MODE_FILE_NAME);
//                            if (oldFile.exists()) oldFile.delete();
//                            Toast.makeText(MainActivity.this, "更新已完成.", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Toast.makeText(MainActivity.this, "注意!\n净眼将暂时无法正常标记和屏蔽，要更新规则，请重新打开净眼。", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .show();
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0) return;
        if (requestCode == REQUEST_PERMISSION
                && Build.VERSION.SDK_INT >= 23
                && grantResults[0] != PackageManager.PERMISSION_GRANTED)
            checkAndCallPermission(permissions[0]);
        else
            process_file();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEW_FRAGMENT && resultCode == RESULT_OK)
            setFragment(new OnlineRulesFragment());
    }

    public void setFragmentWithoutBack(Fragment fragment) {
        setFragment(fragment, false);
    }

    public void setFragment(Fragment fragment) {
        setFragment(fragment, true);
    }

    private void setFragment(Fragment fragment, boolean backable) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);

        transaction.replace(R.id.fl, fragment);
        if (backable)
            transaction.addToBackStack(null);
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
                if (shouldShowFAQ)
                    ((Searchable) currentFragment).setSearchText(newText);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //为啥不用switch呢？
        //懒....
        if (item.getItemId() == R.id.action_about)
            setFragment(new CheckerFragment());
        else if (item.getItemId() == R.id.action_settings)
            startActivityForResult(new Intent(this, PreferencesActivity.class), REQUEST_NEW_FRAGMENT);
        return super.onOptionsItemSelected(item);
    }

    public static ArrayList<BlockModel> read() {
        ArrayList<BlockModel> list = new ArrayList<>();

        ArrayList<String> lines = readPreferenceByLine(LIST_NAME);
        for (String str : lines) {
            BlockModel model = BlockModel.fromString(str);
            if (model == null)
                continue;
            if (model.record.contains(ALL_SPLIT)) {
                model = ViewModel.fromString(str);
            } else {
                //轉換老版(0.8.5-)規則到新版
                model = ViewModel.fromString(ConvertUtils.oldToNew(model).toString());
            }
            if (model != null)
                list.add(model);
        }
        return list;
    }

    private void process_file() {
        final File oldFile = new File(File_Get_SD_Path() + "/fuckview/" + LIST_NAME);
        if (oldFile.exists()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("检测到您使用过版本0.8.3.1之前的净眼，是否要更新规则位置？")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Append_Preferences("\n" + Read_File("fuckview/" + LIST_NAME), LIST_NAME);
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


    public static void Append_Preferences(String data, String filename) {
        Write_Preferences(Read_Preferences(filename) + data, filename);
    }

    public static String Read_File(String filename) {
        final String sdPath = File_Get_SD_Path() + "/";
        File f = new File(sdPath + filename);
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

    public static void Write_Preferences(String data, String filename) {
        mSharedPreferences.edit().putString(filename, data).apply();
    }

    //暴力美学
    //检测SD卡路径
    @Nullable
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

    public static String Read_Preferences(String filename) {
        return mSharedPreferences.getString(filename, "");
    }

    public static ArrayList<String> readPreferenceByLine(String filename) {
        String data = Read_Preferences(filename);
        ArrayList<String> arrayList = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (!line.equals("")) arrayList.add(line);
        }
        return arrayList;
    }

    public static boolean isModuleActive() {
        return false;
    }
}
