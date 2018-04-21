package ml.qingsu.fuckview.utils.root;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import de.robv.android.xposed.XposedBridge;

/**
 * @author YanLu
 * @since 17/12/12
 */

public class AppRulesUtils {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    // 规则文件的存放目录
    public static final String APP_PACKAGE = "ml.qingsu.fuckview";
    public static final String RULES_DIR = "rules";
    // 净眼的包名，可以用于放配置文件
    public static final String RULES_CFG = APP_PACKAGE;

    /**
     * 保存配置文件
     *
     * @param context context
     * @param config  配置字符串，一般直接存放 json 字符串
     * @return
     */
    public static boolean saveConfig(Context context, String config) {
        if (config == null) {
            return false;
        }
        File path = context.getDir(RULES_DIR, Context.MODE_PRIVATE);
        if (path == null) {
            return false;
        }
        File file = new File(path, RULES_CFG);

        // Make sure the directory exists.
        if (!path.exists()) {
            path.mkdirs();
            setFilePermissions(path, 0777, -1, -1);
        }


        Writer output;
        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8));
            output.write(config);
            output.close();
            setFilePermissions(file, 0777, -1, -1);
        } catch (Exception e) {
            XposedBridge.log(e);
            return false;
        }

        return true;
    }

    /**
     * 删除一个 App 规则
     *
     * @param context  context
     * @param fileName 一个App一个文件, 使用包名
     * @return
     */
    public static boolean delRule(Context context, String fileName) {
        File path = context.getDir(RULES_DIR, Context.MODE_PRIVATE);
        if (path == null) {
            return false;
        }
        File file = new File(path, fileName);
        if (!file.exists()) {
            return true;
        } else if (file.isFile()) {
            try {
                return file.delete();
            } catch (Exception e) {
                XposedBridge.log(e);
            }
        }
        return false;
    }

    /**
     * 删除所有的规则
     *
     * @param context context
     * @return
     */
    public static boolean delAllRules(Context context) {
        File path = context.getDir(RULES_DIR, Context.MODE_PRIVATE);
        if (path == null) {
            return false;
        }

        try {
            //目录
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }

                return true;
            }
        } catch (Exception e) {
            XposedBridge.log(e);
        }

        return false;
    }

    /**
     * 保存规则
     *
     * @param context context
     * @return
     */
    public static boolean saveRule(Context context, String fileName, String rule) {
        File path = context.getDir(RULES_DIR, Context.MODE_PRIVATE);
        if (path == null) {
            return false;
        }
        File file = new File(path, fileName);

        // Make sure the directory exists.
        if (!path.exists()) {
            path.mkdirs();
            setFilePermissions(path, 0777, -1, -1);
        }

        Writer output;
        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF_8));
            output.write(rule);
            output.close();
            setFilePermissions(file, 0777, -1, -1);
        } catch (Exception e) {
            XposedBridge.log(e);
            return false;
        }

        return true;
    }


    /**
     * todo 可以直接转成 对象
     * 读取规则文件
     *
     * @return
     */
    public static String readRule(String fileName) {
        String appRule = "";
        File path = new File("//data//data//" + APP_PACKAGE + "//app_rules");

        File file = new File(path, fileName);
        if (file.exists()) {
            if (file.canRead()) {
                try {
                    FileInputStream is = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader bufReader = new BufferedReader(isr);
                    String line;
                    while ((line = bufReader.readLine()) != null) {
                        appRule += ("\n" + line);
                    }

                    bufReader.close();
                    isr.close();
                    is.close();

                } catch (Exception e) {
                    XposedBridge.log(e);
                }
            } else {
                XposedBridge.log(file.getAbsolutePath() + " no read permission");
            }
        } else {
            XposedBridge.log("rule file doesn't exist");
        }

        return appRule;

    }


    /**
     * 修改文件权限
     * setFilePermissions(file, 0777, -1, -1);
     *
     * @param file
     * @param chmod
     * @param uid
     * @param gid
     * @return
     */
    public static boolean setFilePermissions(File file, int chmod, int uid, int gid) {
        if (file != null) {
            Class<?> fileUtils;
            try {
                fileUtils = Class.forName("android.os.FileUtils");
                Method setPermissions = fileUtils.getMethod("setPermissions", File.class, int.class, int.class, int.class);
                int result = (Integer) setPermissions.invoke(null, file, chmod, uid, gid);

                return result == 0;
            } catch (Exception e) {
                XposedBridge.log(e);
            }

            return false;
        } else {
            return false;
        }
    }

    /**
     * 修改文件权限
     * setFilePermissions(path, 0777, -1, -1);
     *
     * @param path
     * @param chmod
     * @param uid
     * @param gid
     * @return
     */
    public static boolean setFilePermissions(String path, int chmod, int uid, int gid) {
        if (!TextUtils.isEmpty(path)) {
            Class<?> fileUtils;
            try {
                fileUtils = Class.forName("android.os.FileUtils");
                Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
                int result = (Integer) setPermissions.invoke(null, path, chmod, uid, gid);

                return result == 0;
            } catch (Exception e) {
                XposedBridge.log(e);
            }

            return false;
        } else {
            return false;
        }
    }

}
