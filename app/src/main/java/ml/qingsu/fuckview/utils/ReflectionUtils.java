package ml.qingsu.fuckview.utils;

import java.lang.reflect.Field;

/**
 * Created by w568w on 18-6-13.
 *
 * @author w568w
 */

public class ReflectionUtils {
    public static Object getField(Object obj, String name) throws IllegalAccessException {
        Class clz = obj.getClass();
        while (clz != null) {
            try {
                Field field = clz.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clz = clz.getSuperclass();
            }
        }
        return null;
    }

}
