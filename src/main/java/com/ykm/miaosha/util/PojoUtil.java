package com.ykm.miaosha.util;

import java.lang.reflect.Field;

/**
 * @Author ykm
 * @Date 2019/6/5 17:17
 */
public class PojoUtil {
    public static boolean isAllFieldNotEmpty(Object object) {
        for (Field f : object.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (f.get(object) == null || f.get(object).toString().length() == 0) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
