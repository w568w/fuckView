package ml.qingsu.fuckview.models;

import java.io.Serializable;
import java.util.Locale;

import ml.qingsu.fuckview.ui.activities.MainActivity;

/**
 * Moved by w568w on 18-2-4.
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


    public void save() {
        MainActivity.writePreferences(MainActivity.readPreferences(MainActivity.LIST_NAME) + "\n" + toString(), MainActivity.LIST_NAME);
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
