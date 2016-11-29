package com.wh.starboot.config.annotation;

import com.wh.starboot.util.DistributedLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * Created by kingbo on 2016/11/28.
 */
@Component
public class PrintHaAnnotationProcesser implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PrintHaAnnotationProcesser.class);

    @Autowired
    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    @Autowired
    private DistributedLocker distributedLocker;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        scanPrintHaAnnotation(bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void scanPrintHaAnnotation(Object bean) {
        Class<?> managedBeanClass = bean.getClass();
        FieldCallback fcb = new PrintHaFieldCallback(configurableListableBeanFactory, bean);
        ReflectionUtils.doWithFields(managedBeanClass, fcb);
    }

}
