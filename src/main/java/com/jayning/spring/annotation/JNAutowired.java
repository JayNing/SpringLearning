package com.jayning.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author JAY
 * @Date 2019/6/9 13:15
 * @Description TODO
 **/

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JNAutowired {
    String value() default "";
}
