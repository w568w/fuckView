package ml.qingsu.fuckview.ui.fragments.first_run_wizard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.ui.fragments.select_app.SelectAppWizard;
import ml.qingsu.fuckview.utils.wizard.BasicWizard;
import ml.qingsu.fuckview.utils.wizard.WizardStep;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by w568w on 2017-7-6.
 */

public class Helper extends BasicWizard {
    public Helper() {
        super();

    }

    public static View getTextView(Context context, String titleStr, String str) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(10, 10, 10, 10);

        TextView title = new TextView(context);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f);
        title.setShadowLayer(5, 5, 5, Color.GRAY);
        title.setText(titleStr);
        //中文粗体
        title.getPaint().setFakeBoldText(true);

        TextView tv = new TextView(context);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        tv.setText(str);
        tv.setHorizontalScrollBarEnabled(true);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        ll.addView(title);
        ll.addView(tv);
        return ll;
    }

    @Override
    protected Settings getSettings() {
        return new Settings("上一步", "下一步", "我们开始吧", new WizardStep[]{new Step1(),
                new Step2(),
                new Step3(),
                new Step4(),
                new Step5(),
                new Step6(),
                new Step7(),
                new Step8(),});
    }

    @Override
    public void onWizardComplete() {
        super.onWizardComplete();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("info", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("first_run", false).apply();
        if (getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).setFragmentWithoutBack(new SelectAppWizard());
        Toast.makeText(mCon, "有问题请按右上角问号!\n有问题请按右上角问号!\n有问题请按右上角问号!\n\n重要的事情说三遍", Toast.LENGTH_LONG).show();
    }
}
