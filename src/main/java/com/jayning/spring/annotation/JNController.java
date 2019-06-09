package com.jayning.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author JAY
 * @Date 2019/6/9 13:15
 * @Description TODO
 **/

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JNController {
    String value() default "";
}
