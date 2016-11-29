package com.wh.starboot.config.annotation;

import java.lang.annotation.*;

/**
 * Created by kingbo on 2016/11/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Documented
public @interface PrintHa {

    String value() default "Yes!!!";
}
