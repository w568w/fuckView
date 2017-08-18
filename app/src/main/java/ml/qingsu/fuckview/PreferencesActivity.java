package ml.qingsu.fuckview;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.widget.Toast;

import java.util.Locale;

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

                MainActivity.Write_File(o.toString(), SUPER_MODE_NAME);
                return true;
            }
        });
        findPreference("only_once").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MainActivity.Write_File(newValue.toString(), ONLY_ONCE_NAME);
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
                                MainActivity.Append_File("\n" + editText.getText().toString(), MainActivity.LIST_NAME);
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
        findPreference("qq").

                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://jq.qq.com/?_wv=1027&k=4EepPOs")));
                        } catch (Throwable ignored) {

                        }
                        return false;
                    }
                });
    }
}
