package com.homi.utils;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;


public final class BeanCopyUtils {

    private BeanCopyUtils() {
    }

    public static <V> V copyBean(Object source, Class<V> clazz) {
        //创建目标对象
        V result = null;
        try {
            result = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return result;
        }
        //实现属性copy
        BeanUtils.copyProperties(source, result);

        return result;
    }
}
