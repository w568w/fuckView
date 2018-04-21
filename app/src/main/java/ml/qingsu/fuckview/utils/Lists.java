package ml.qingsu.fuckview.utils;

import java.util.ArrayList;
import java.util.List;

import ml.qingsu.fuckview.implement.ListFilter;

/**
 * Created by w568w on 18-4-1.
 *
 * @author w568w
 */

public class Lists {
    public static <T> List<T> filter(List<T> list, ListFilter<T> filter) {
        ArrayList<T> filtered = new ArrayList<>();
        int len = list.size();
        for (int i = 0; i < len; i++) {
            T obj = list.get(i);
            if (filter.filter(obj)) {
                filtered.add(obj);
            }
        }
        return filtered;
    }
}
