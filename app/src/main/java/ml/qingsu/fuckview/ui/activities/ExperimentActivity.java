package ml.qingsu.fuckview.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ml.qingsu.fuckview.R;
import ml.qingsu.fuckview.models.CoolApkHeadlineModel;

public class ExperimentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment);
        setTitle(R.string.fuckview_lab);
        findViewById(R.id.lab_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et= (EditText) findViewById(R.id.lab_word);
                new CoolApkHeadlineModel(CoolApkHeadlineModel.HeadlineType.CONTENT,et.getText().toString()).save();
                Toast.makeText(ExperimentActivity.this, "规则保存成功！强制停止酷安之后看看效果吧！", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
