package ml.qingsu.fuckview.ui.fragments.select_app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.ui.popups.DumpViewerPopupView;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.MyApplication;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.utils.wizard.BasicWizard;
import ml.qingsu.fuckview.utils.wizard.WizardStep;

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
        return new Settings(getResources().getString(R.string.prev_step), getResources().getString(R.string.next_step), getResources().getString(R.string.start_mark), new WizardStep[]{step1});
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
            Toast.makeText(MyApplication.con, R.string.start_mark_toast, Toast.LENGTH_SHORT).show();
            new DumpViewerPopupView(mCon, Step1.selected.packageName).show();
            mCon.finish();
        } catch (WindowManager.BadTokenException | SecurityException e) {
            Toast.makeText(MyApplication.con, "无法启动悬浮窗，请给予净眼悬浮窗权限！", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(MyApplication.con, "无法启动应用，请检查您是否将其冻结或隐藏", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
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