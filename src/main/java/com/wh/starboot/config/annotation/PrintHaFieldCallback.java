package com.wh.starboot.config.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import java.lang.reflect.Field;

/**
 * Created by kingbo on 2016/11/28.
 */
public class PrintHaFieldCallback implements FieldCallback {

    private static final Logger logger = LoggerFactory.getLogger(PrintHaFieldCallback.class);

    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    private Object bean;

    public PrintHaFieldCallback(ConfigurableListableBeanFactory configurableListableBeanFactory, Object bean) {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
        this.bean = bean;
    }

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        if (!field.isAnnotationPresent(PrintHa.class)) {
            return;
        }
        ReflectionUtils.makeAccessible(field);
        String value = field.getDeclaredAnnotation(PrintHa.class).value();
        field.set(bean, value);
    }
}
