package ml.qingsu.fuckview.utils.faq;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ml.qingsu.fuckview.R;

/**
 *
 * @author w568w
 * @date 2017-7-27
 */

public abstract class BaseFaqWizard extends Fragment {

    protected int currentId = FIRST_ID;
    public static final int FIRST_ID = 0;

    protected abstract ArrayList<FaqStep> getData();

    protected class FaqStep {
        private int id;
        @NonNull
        private String question = "null";
        @Nullable
        private String[] choose;
        private int[] to;

        public FaqStep(int id, @NonNull String question, String[] choose, int[] to) {
            this.id = id;
            this.question = question;
            this.choose = choose;
            this.to = to;
        }

        public FaqStep(int id, @NonNull String question) {
            this.id = id;
            this.question = question;
        }
    }

    TextView title;
    ListView choose;
    ArrayList<FaqStep> data;
    ArrayAdapter<String> adapter;
    Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = inflater.getContext();
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.faq_wizard_layout, null);
        choose = (ListView) layout.findViewById(R.id.faq_wizard_list);
        title = (TextView) layout.findViewById(R.id.faq_wizard_title);
        choose.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (findStepById(currentId).to != null) {
                    jumpTo(findStepById(currentId).to[i]);
                }
            }
        });
        data = getData();
        jumpTo(FIRST_ID);
        return layout;
    }

    private void jumpTo(int id) {
        FaqStep faqStep = findStepById(id);
        if (faqStep != null) {
            if (faqStep.choose == null) {
                faqStep.choose = new String[]{faqStep.question};
                faqStep.question = "我们已找到解决方案";
                title.setTextColor(Color.parseColor("#ff669900"));
            }
            adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, faqStep.choose);
            title.setText(faqStep.question);
            choose.setAdapter(adapter);
            currentId = id;
        }


    }

    private FaqStep findStepById(int id) {
        for (FaqStep faqStep : data) {
            if (faqStep.id == id) {
                return faqStep;
            }
        }
        return new FaqStep(0, "", null, null);
    }
}
