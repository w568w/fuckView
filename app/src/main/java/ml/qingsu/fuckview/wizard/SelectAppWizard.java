package ml.qingsu.fuckview.wizard;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.WindowManager;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import de.robv.android.xposed.XposedBridge;
import ml.qingsu.fuckview.DumpViewerPopupView;
import ml.qingsu.fuckview.MainActivity;
import ml.qingsu.fuckview.MyApplication;
import ml.qingsu.fuckview.Searchable;
import ml.qingsu.fuckview.wizard_library.BasicWizard;
import ml.qingsu.fuckview.wizard_library.WizardStep;

/**
 * Created by w568w on 17-6-18.
 */
public class SelectAppWizard extends BasicWizard implements Searchable {
    Step1 step1;

    @Override
    protected Settings getSettings() {
        step1 = new Step1();
        if (getArguments() != null)
            step1.setArguments(getArguments());
        else
            step1.setArguments(new Bundle());
        return new Settings("上一步", "下一步", "开始标记", new WizardStep[]{step1});
    }

    public SelectAppWizard() {
        super();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCon instanceof MainActivity) {
            ((MainActivity) mCon).shouldShowFAQ = true;
            ((MainActivity) mCon).currentFragment = this;
            //AppCompatActivity类提供了一个可以在API9上使用的invalidateOptionsMenu方法
            //本来这个方法在Activity类上需要API11的
            //所以需要转型
            //(PS:呆萌的AS告诉我转型是多余的2333)
            ((MainActivity) mCon).invalidateOptionsMenu();

        }
    }

    @SuppressLint({"ApplySharedPref", "WorldReadableFiles"})
    @Override
    public void onWizardComplete() {
        super.onWizardComplete();
        if (Step1.selected == null) return;
        getActivity().getSharedPreferences("data", Context.MODE_WORLD_READABLE)
                .edit().putString(MainActivity.PACKAGE_NAME_NAME, Step1.selected.packageName).commit();
        //等待數據寫入完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //killProcess(Step1.selected.packageName);
        PackageManager pm = MyApplication.con.getPackageManager();
        try {
            MyApplication.con.startActivity(pm.getLaunchIntentForPackage(Step1.selected.packageName));
            Toast.makeText(MyApplication.con, "点击以标记View", Toast.LENGTH_SHORT).show();
            new DumpViewerPopupView(mCon, Step1.selected.packageName).show();
            mCon.finish();
        } catch (WindowManager.BadTokenException e){
            Toast.makeText(MyApplication.con, "无法启动悬浮窗，请给予净眼悬浮窗权限！", Toast.LENGTH_SHORT).show();
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(MyApplication.con, "无法启动应用，请检查您是否将其冻结或隐藏", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(MyApplication.con, "无法启动应用，请检查您是否将其冻结或隐藏", Toast.LENGTH_SHORT).show();
            CrashReport.postCatchedException(e);
        }
    }

    @Override
    public void setSearchText(String text) {
        if (step1 != null)
            step1.setSearchText(text);
    }
}