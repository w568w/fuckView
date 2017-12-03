package ml.qingsu.fuckview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.widget.Toast;

import java.util.Locale;

import static ml.qingsu.fuckview.MainActivity.STANDARD_MODE_NAME;
import static ml.qingsu.fuckview.MainActivity.SUPER_MODE_NAME;
import static ml.qingsu.fuckview.MainActivity.ONLY_ONCE_NAME;

public class PreferencesActivity extends PreferenceActivity {
    private int clickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        findPreference("super_mode").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                MainActivity.Write_Preferences(o.toString(), SUPER_MODE_NAME);
                return true;
            }
        });
        findPreference("only_once").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MainActivity.Write_Preferences(newValue.toString(), ONLY_ONCE_NAME);
                return true;
            }
        });
        findPreference("standard_mode").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MainActivity.Write_Preferences(newValue.toString(), STANDARD_MODE_NAME);
                return true;
            }
        });
        findPreference("import").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AppCompatEditText editText = new AppCompatEditText(PreferencesActivity.this);
                editText.setHint("添加规则，每行一个");
                new AlertDialog.Builder(PreferencesActivity.this)
                        .setTitle("导入规则")
                        .setView(editText)
                        .setPositiveButton("导入", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.Append_Preferences("\n" + editText.getText().toString(), MainActivity.LIST_NAME);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return false;
            }
        });

        findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                clickTime++;

                if (clickTime >= 5 * 59) {
                    clickTime = 0;

                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (Throwable ignored) {

                    }
                    Toast.makeText(PreferencesActivity.this, "清明时节雨纷纷，我为长者续五分!", Toast.LENGTH_LONG).show();
                }
                preference.setSummary(String.format(Locale.CHINA, "已献出生命%d分%d秒!", clickTime / 59, clickTime % 59));
                return false;
            }
        });
        findPreference("pay").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(PreferencesActivity.this, "直接加群就好啦\n管那么多干嘛", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        findPreference("source").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(PreferencesActivity.this)
                        .setTitle("开放源代码许可")
                        .setMessage("并没有这种东西")
                        .setPositiveButton("好", null)
                        .show();
                return false;
            }
        });
        findPreference("qq").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://jq.qq.com/?_wv=1027&k=4EepPOs")));
                } catch (Throwable ignored) {

                }
                return false;
            }
        });
        findPreference("log").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String bug=String.format(Locale.CHINA,"Logcat:\n\n%s\n\n" +
                                "=================\n\n" +
                                "XposedLog:\n\n%s\n\n" +
                                "=================\n\n" +
                                "Phone:\n\n%s\n\n",getLogcatInfo(),getXposedLogInfo(),getPhoneInfo());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                shareText(bug);
                            }
                        });
                    }
                }).start();
                return false;
            }
        });
    }
    private String getLogcatInfo(){
            return ShellUtils.execCommand("logcat -d -v time",false,true).successMsg;
    }
    private String getXposedLogInfo(){
        return ShellUtils.execCommand("cat /data/data/de.robv.android.xposed.installer/log/error.log",false,true).successMsg;
    }
    private String getPhoneInfo(){
        return String.format(Locale.CHINA,"版本:%s(%s)\n" +
                        "Android版本:%s\n" +
                        "指纹:%s\n",
                getVersionName(this),getVersionCode(this),
                System.getProperty("ro.build.version.release"),
                Build.FINGERPRINT);
    }
    /**
     * 返回版本名字
     * 对应build.gradle中的versionName
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 返回版本号
     * 对应build.gradle中的versionCode
     *
     * @param context
     * @return
     */
    public static String getVersionCode(Context context) {
        String versionCode = "" ;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = String.valueOf(packInfo.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    private void shareText(String text){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("*/*");
        try {
            Intent chooserIntent = Intent.createChooser(sendIntent, "选择分享途径");
            if (chooserIntent == null) {
                return;
            }
            startActivity(chooserIntent);
        } catch (Exception e) {
            startActivity(sendIntent);
        }
    }
}
