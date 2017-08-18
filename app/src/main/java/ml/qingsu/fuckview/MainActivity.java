package ml.qingsu.fuckview;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import ml.qingsu.fuckview.about.Faq;
import ml.qingsu.fuckview.helper.Helper;
import ml.qingsu.fuckview.wizard.TutorialWizard;

public class MainActivity extends AppCompatActivity {
    public boolean shouldShowFAQ = false;

    public static final String DIR_NAME = "fuckView/";
    public static final String LIST_FILE_NAME = "block_list";
    public static final String SUPER_MODE_FILE_NAME = "super_mode";
    public static final String ONLY_ONCE_FILE_NAME = "only_once";
    public static final String LIST_NAME = DIR_NAME + LIST_FILE_NAME;
    public static final String SUPER_MODE_NAME = DIR_NAME + SUPER_MODE_FILE_NAME;
    public static final String ONLY_ONCE_NAME = DIR_NAME + ONLY_ONCE_FILE_NAME;
    public static final String PACKAGE_NAME_NAME = DIR_NAME + "package_name";

    public static final String LAUNCHER_VIRTUAL_CLASSNAME = "launcher";
    private static final int REQUEST_PERMISSION = 0x123;
    private static final String ALL_SPLIT = "~~";
    public Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndCallPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        SharedPreferences sharedPreferences = getSharedPreferences("info", MODE_PRIVATE);
        boolean firstRun = sharedPreferences.getBoolean("first_run", true);
        //给root
        //ShellUtils.execCommand("", true);

        if (firstRun) {
            setFragmentWithoutBack(new Helper());
        } else if (Read_File(LIST_NAME).equals("")) {
            setFragmentWithoutBack(new TutorialWizard());
            if (!isModuleActive()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("模块未启用/更新")
                        .setMessage("请在Xposed Installer中启用我，然后软重启以激活!")
                        .setPositiveButton("好", null)
                        .show();
            }
        } else {
            setFragmentWithoutBack(new MainFragment());
            if (!isModuleActive()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("模块未启用/更新")
                        .setMessage("请在Xposed Installer中启用我，然后软重启以激活!")
                        .setPositiveButton("好", null)
                        .show();
            }
        }

        Write_File("", PACKAGE_NAME_NAME);
        //此处详见hook.java
        //是否从通知栏里点过来
        boolean isDialog = getIntent().getBooleanExtra("Dialog", false);
        String cache = getIntent().getStringExtra("cache");
        if (cache == null) return;
        if (isDialog) {

            final BlockModel blockInfo = BlockModel.fromString(cache);
            if (blockInfo == null) return;
            //~~←这是在hook的BlockModel中的getInstancebyAll中定义的，多种匹配方式的分隔符
            final String[] ids = blockInfo.id.split(ALL_SPLIT);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("模式选择")
                    .setMessage("经典模式:使用ID和文本进行定位,适合于一般情况，误杀率高。\n\n路径模式:使用类XPath的形式定位，适合于位置固定不动，但ID为-1或文本会不断变化的情况，误杀率低。\n\n坐标模式:前两种无效时试试它。")
                    .setPositiveButton("经典", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            blockInfo.id = ids[0];
                            //换行不能忘！
                            Append_File("\n" + blockInfo, LIST_NAME);
                            setFragmentWithoutBack(new MainFragment());
                        }
                    })
                    .setNegativeButton("路径", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            blockInfo.id = ids[1];
                            Append_File("\n" + blockInfo, LIST_NAME);
                            setFragmentWithoutBack(new MainFragment());
                        }
                    })
                    .setNeutralButton("坐标", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            blockInfo.id = ids[2];
                            Append_File("\n" + blockInfo, LIST_NAME);
                            setFragmentWithoutBack(new MainFragment());
                        }
                    })
                    .show();
        } else {
            Append_File(cache, LIST_NAME);
        }

    }

    private void checkAndCallPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION);
        } else {
            findViewById(R.id.fl).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    findViewById(R.id.fl).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    checkFile();
                }
            });
        }
    }

    private void checkFile() {
        final String sdPath = File_Get_SD_Path() + "/";
        File dir = new File(sdPath + DIR_NAME);
        if (!dir.exists()) {
            if (!dir.mkdirs() && !dir.mkdir()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("错误")
                        .setMessage("创建文件夹失败!\n请检查您是否限制了净眼创建文件夹的权限。\n\n提示:您也可以手动在SD卡中创建fuckView(注意大小写)文件夹并重试。")
                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .show();
            }
        }
        final File oldFile = new File(sdPath + LIST_FILE_NAME);
        if (oldFile.exists()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("检测到旧文件")
                    .setMessage("检测到你曾经使用过0.7.1及之前版本的 净眼，是否要更新规则位置?")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Append_File(Read_File(LIST_FILE_NAME), LIST_NAME);
                            oldFile.delete();
                            File oldFile = new File(sdPath + SUPER_MODE_FILE_NAME);
                            if (oldFile.exists()) oldFile.delete();
                            Toast.makeText(MainActivity.this, "更新已完成.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "注意!\n净眼将暂时无法正常标记和屏蔽，要更新规则，请重新打开净眼。", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0) return;
        if (requestCode == REQUEST_PERMISSION
                && Build.VERSION.SDK_INT >= 23
                && grantResults[0] != PackageManager.PERMISSION_GRANTED)
            checkAndCallPermission(permissions[0]);
        else
            checkFile();
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
            setFragment(new Faq());
        else if (item.getItemId() == R.id.action_settings)
            startActivity(new Intent(this, PreferencesActivity.class));
        return super.onOptionsItemSelected(item);
    }

    //你可以在hook.java中看到一毛一样的以下代码
    //为啥要写两遍呢？
    //因为hook是脱离程序运行的，此时如果用MainActivity.XX()会扔出NPE
    //
    //MainActivity中调用hook.XX()也是相同道理(这句话未经验证，只是猜测)
    public static ArrayList<BlockModel> read() {
        ArrayList<BlockModel> list = new ArrayList<>();
        ArrayList<String> lines = readFileByLine(LIST_NAME);
        for (String str : lines) {
            BlockModel model = BlockModel.fromString(str);
            if (model != null)
                list.add(model);
        }
        return list;
    }

    public static class BlockModel {
        public String packageName;
        public String id;
        public String text;
        public String className;
        public boolean enable;

        public BlockModel(String packageName, String id, String text, String className) {
            this.packageName = packageName;
            this.id = id;
            this.text = text;
            this.className = className;
            enable=true;
        }

        private BlockModel(String packageName, String id, String text, String className, boolean enable) {
            this.packageName = packageName;
            this.id = id;
            this.text = text;
            this.className = className;
            this.enable = enable;
        }

        public static BlockModel fromString(String text) {
            String[] var = text.split("@@@");
            if (var.length == 4) {
                return new BlockModel(var[0], var[1], var[2], var[3]);
            }
            if (var.length == 5) {
                return new BlockModel(var[0], var[1], var[2], var[3], Boolean.valueOf(var[4]));
            }
            return null;
        }

        public void save() {
            Append_File("\n" + toString(), LIST_NAME);
        }

        @Override
        public String toString() {
            return String.format(Locale.CHINA, "%s@@@%s@@@%s@@@%s@@@%s", packageName, id, text, className, enable + "");
        }
    }

    public static void Append_File(String data, String filename) {
        Write_File(Read_File(filename) + data, filename);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void Write_File(String data, String filename) {
        final String sdPath = File_Get_SD_Path() + "/";
        File f = new File(String.format("%s%s", sdPath, filename));
        if (f.exists())
            f.delete();
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            FileOutputStream fops = new FileOutputStream(f);
            fops.write(data.getBytes());
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static ArrayList<String> readFileByLine(String filename) {
        final String sdPath = File_Get_SD_Path() + "/";
        File f = new File(sdPath + filename);
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

    private static boolean isModuleActive() {
        return false;
    }
}
