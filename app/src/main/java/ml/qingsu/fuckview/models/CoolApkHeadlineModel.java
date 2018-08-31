package ml.qingsu.fuckview.models;

import ml.qingsu.fuckview.Constant;

import static ml.qingsu.fuckview.ui.activities.MainActivity.ALL_SPLIT;

/**
 * Created by w568w on 18-5-27.
 *
 * @author w568w
 */

public class CoolApkHeadlineModel extends BlockModel {
    private HeadlineType mType;

    public HeadlineType getType() {
        return mType;
    }

    public enum HeadlineType {
        FROM, CONTENT
    }


    public CoolApkHeadlineModel(HeadlineType type, String text) {
        super(Constant.COOLAPK_MARKET_PKG_NAME, type.ordinal() + ALL_SPLIT + text, text, Constant.VIRTUAL_COOLAPK_CLASSNAME);
        mType = type;
    }

    protected CoolApkHeadlineModel(HeadlineType type, String text, boolean enable) {
        super(Constant.COOLAPK_MARKET_PKG_NAME, type.ordinal() + ALL_SPLIT + text, text, Constant.VIRTUAL_COOLAPK_CLASSNAME, enable);
        mType = type;
    }

    public CoolApkHeadlineModel(String packageName, String record, String text, String className) {
        super(packageName, record, text, className);
        prepare();
    }

    protected CoolApkHeadlineModel(String packageName, String record, String text, String className, boolean enable) {
        super(packageName, record, text, className, enable);
        prepare();
    }

    protected void prepare() {
        String[] values = record.split(ALL_SPLIT);
        if (values.length == 2) {
            try {
                mType = HeadlineType.values()[Integer.parseInt(values[0])];
            } catch (Exception e) {
                e.printStackTrace();
                mType = null;
            }
        } else {
            mType = null;
        }
    }

    public static CoolApkHeadlineModel fromString(String text) {
        String[] var = text.split("@@@");
        if (var.length == 4) {
            return new CoolApkHeadlineModel(var[0], var[1], var[2], var[3]);
        }
        if (var.length == 5) {
            return new CoolApkHeadlineModel(var[0], var[1], var[2], var[3], Boolean.valueOf(var[4]));
        }
        return null;
    }

    public static boolean isInstance(String str) {
        try {
            if (fromString(str).getType() != null) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
