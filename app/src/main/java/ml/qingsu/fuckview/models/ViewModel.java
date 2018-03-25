package ml.qingsu.fuckview.models;

import ml.qingsu.fuckview.ui.activities.MainActivity;

/**
 * Moved by w568w on 18-2-4.
 * @author w568w
 */
public class ViewModel extends BlockModel {
    private String id;
    private String path;
    private String position;

    public ViewModel(String packageName, String record, String text, String className) {
        super(packageName, record, text, className);
        prepare();
    }

    protected ViewModel(String packageName, String record, String text, String className, boolean enable) {
        super(packageName, record, text, className, enable);
        prepare();
    }

    private void prepare() {
        String[] spilted = record.split(MainActivity.ALL_SPLIT);
        id = spilted[0];
        path = spilted[1];
        position = spilted[2];
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getPosition() {
        return position;
    }

    public static ViewModel fromString(String text) {
        String[] var = text.split("@@@");
        if (var.length == 4) {
            return new ViewModel(var[0], var[1], var[2], var[3]);
        }
        if (var.length == 5) {
            return new ViewModel(var[0], var[1], var[2], var[3], Boolean.valueOf(var[4]));
        }
        return null;
    }

    public static boolean isInstance(String str) {
        try {
            if (!"".equals(fromString(str).getPath())) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
