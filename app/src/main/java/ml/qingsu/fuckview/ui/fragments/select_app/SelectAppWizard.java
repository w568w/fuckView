package ml.qingsu.fuckview.ui.fragments.select_app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.WindowManager;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;

import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.implement.Searchable;
import ml.qingsu.fuckview.models.PageEvent;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.ui.popups.FloatingPopupView;
import ml.qingsu.fuckview.ui.popups.guide.GuidePopupToast;
import ml.qingsu.fuckview.utils.PackageUtils;
import ml.qingsu.fuckview.utils.wizard.BaseWizard;
import ml.qingsu.fuckview.utils.wizard.WizardStep;

/**
 * @author w568w
 * @date 17-6-18
 */
public class SelectAppWizard extends BaseWizard implements Searchable {
    Step1 step1;

    @Override
    protected Settings getSettings() {
        step1 = new Step1();
        if (getArguments() != null) {
            step1.setArguments(getArguments());
        } else {
            step1.setArguments(new Bundle());
        }
        return new Settings(getResources().getString(R.string.prev_step), getResources().getString(R.string.next_step), getResources().getString(R.string.start_mark), new WizardStep[]{step1});
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
        if (Step1.sSelected == null) {
            return;
        }
        mCon.getSharedPreferences("data", Context.MODE_WORLD_READABLE)
                .edit().putString(Constant.PACKAGE_NAME_NAME, Step1.sSelected.packageName).commit();
        //等待數據寫入完成
        SystemClock.sleep(100);
        PackageUtils.asyncStopProcess(Step1.sSelected.packageName, new Runnable() {
            @Override
            public void run() {
                mCon.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity();
                    }
                });
            }
        }, null);
    }

    private void startActivity() {
        PackageManager pm = mCon.getPackageManager();
        try {
            mCon.startActivity(pm.getLaunchIntentForPackage(Step1.sSelected.packageName));
            Toast.makeText(mCon, R.string.start_mark_toast, Toast.LENGTH_SHORT).show();
            new FloatingPopupView(mCon, Step1.sSelected.packageName).show();
            mCon.finish();
            EventBus.getDefault().post(new PageEvent(GuidePopupToast.Page.APP_STARTED.ordinal()));
        } catch (WindowManager.BadTokenException | SecurityException e) {
            Toast.makeText(mCon, R.string.cant_open_popup, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mCon, R.string.cant_start_app, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(mCon, R.string.cant_start_app, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        }
    }

    @Override
    public void setSearchText(String text) {
        if (step1 != null) {
            step1.setSearchText(text);
        }
    }
}