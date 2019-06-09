package com.jayning.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author JAY
 * @Date 2019/6/9 13:15
 * @Description TODO
 **/

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JNRequestParam {
    String value() default "";
}
