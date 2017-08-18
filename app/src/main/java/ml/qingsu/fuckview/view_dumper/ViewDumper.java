package ml.qingsu.fuckview.view_dumper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Xml;
import android.view.accessibility.AccessibilityNodeInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import ml.qingsu.fuckview.MainActivity;
import ml.qingsu.fuckview.ShellUtils;

/**
 * Created by w568w on 2017-7-12.
 * No GPL ,MIT,Apache or any other fucking licence.
 * Just do anything fucking you'd like to.
 */

public class ViewDumper {
    private static final int NO_PARENT = -1;

    public static final class ViewItem {
        public String simpleClassName;
        public Point bounds;
        public Point wh;
        public int id;
        public String text;
        public int parentId;
        public int level;

        @Override
        public String toString() {
            return String.format(Locale.CHINA, "%s,%d,%d,%d,%d", simpleClassName, bounds.x, bounds.y, wh.x, wh.y);
        }
    }

    public static synchronized ArrayList<ViewItem> parseCurrentView() {
//        File f = new File("/mnt/sdcard/dump.xml");
//        if (f.exists()) f.delete();


        ShellUtils.execCommand("uiautomator dump /mnt/sdcard/dump.xml", true, false);
        String xml = MainActivity.Read_File("dump.xml").replace("\n", "");
        ArrayList<ViewItem> itemList = new ArrayList<>();
        ViewItem temp;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equals("node")) {
                            temp = new ViewItem();
                            //读类名
                            String[] className = parser.getAttributeValue(null, "class").split("\\.");
                            temp.simpleClassName = className[className.length - 1];
                            //读坐标
                            String bounds = parser.getAttributeValue(null, "bounds");
                            String[] temp_n = bounds.replaceFirst("\\[", "").split("\\]\\[");
                            String[] point = temp_n[0].split(",");
                            if (point.length == 2)
                                temp.bounds = new Point(Integer.valueOf(point[0]), Integer.valueOf(point[1]));
                            else
                                temp.bounds = new Point();
                            //读宽高
                            point = bounds.split("\\]\\[")[1].split(",");
                            if (point.length == 2)
                                temp.wh = new Point(Integer.valueOf(point[0]) - temp.bounds.x, Integer.valueOf(point[1].replaceFirst("\\]", "")) - temp.bounds.y);
                            else
                                temp.wh = new Point();
                            //其他杂类信息
                            temp.id = itemList.size();
                            temp.level = parser.getDepth();
                            ViewItem vi = getLastTopLevelNode(itemList, temp.level - 1);
                            temp.parentId = (vi == null ? NO_PARENT : vi.id);
                            itemList.add(temp);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemList;
    }

    private static ViewItem getLastTopLevelNode(ArrayList<ViewItem> al, int depth) {
        ArrayList<ViewItem> copy = new ArrayList<>(al);
        Collections.reverse(copy);
        for (ViewItem tn : copy) {
            if (tn.level == depth)
                return tn;
        }
        return null;
    }

    public static synchronized ArrayList<ViewItem> parseCurrentViewAbove16(Context context) {
        if (DumperService.getInstance() == null) {
            ServiceUtils.openSetting(context);
            return null;
        }
        AccessibilityNodeInfo root = DumperService.getInstance().getRootInActiveWindow();
        itemList = new ArrayList<>();
        if (root == null)
            return itemList;
        parseChild(root, 2);
        return itemList;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void parseChild(AccessibilityNodeInfo parent, int depth) {
        int childCount = parent.getChildCount();
        itemList.add(nodeInfoToViewItem(parent, depth));
        for (int i = 0; i < childCount; i++) {
            parseChild(parent.getChild(i), depth + 1);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private synchronized static ViewItem nodeInfoToViewItem(AccessibilityNodeInfo node, int depth) {
        ViewItem vi = new ViewItem();
        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        vi.bounds = new Point(rect.left, rect.top);
        vi.wh = new Point(rect.width(), rect.height());

        vi.id = id++;
        vi.level = depth;
        ViewItem v = getLastTopLevelNode(itemList, depth - 1);
        vi.parentId = (v == null ? NO_PARENT : v.id);
        vi.simpleClassName = node.getClassName().toString();
        if (vi.simpleClassName.contains(".")) {
            vi.simpleClassName = vi.simpleClassName.substring(vi.simpleClassName.lastIndexOf("."));
        }
        return vi;
    }

    private static int id = 0;
    private static ArrayList<ViewItem> itemList;
}
