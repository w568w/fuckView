package ml.qingsu.fuckview.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import ml.qingsu.fuckview.Constant;
import ml.qingsu.fuckview.ui.activities.MainActivity;
import ml.qingsu.fuckview.utils.ConvertUtils;

/**
 * Moved by w568w on 18-2-4.
 *
 * @author w568w
 */
public class BlockModel implements Serializable {
    public String record;
    public String packageName;


    public String text;
    public String className;
    public boolean enable;


    public BlockModel(String packageName, String record, String text, String className) {
        this.packageName = packageName;
        this.record = record;
        this.text = text;
        this.className = className;
        enable = true;
    }

    protected BlockModel(String packageName, String record, String text, String className, boolean enable) {
        this.packageName = packageName;
        this.record = record;
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

    public static ArrayList<BlockModel> readModel() {
        final ArrayList<BlockModel> list = new ArrayList<>();

        ArrayList<String> lines = MainActivity.readPreferenceByLine(Constant.LIST_NAME);
        final int len = lines.size();
        if(len==0){
            return list;
        }
        for (int i = 0; i < len; i++) {
            String str = lines.get(i);
            BlockModel model = fromString(str);
            if (model == null) {
                continue;
            }
            if (model.record.contains(MainActivity.ALL_SPLIT)) {
                model = ViewModel.fromString(str);
            } else {
                //轉換老版(0.8.5-)規則到新版
                model = ViewModel.fromString(ConvertUtils.oldToNew(model).toString());
            }
            if (model != null) {
                list.add(model);
            }
        }
        return list;
    }


    public void save() {
        MainActivity.appendPreferences("\n" + toString(),Constant.LIST_NAME);
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA, "%s@@@%s@@@%s@@@%s@@@%s", packageName, record, text, className, enable + "");
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || !(o instanceof BlockModel)) && o.toString().equals(toString());
    }
}
