package com.jayning.spring.framework.aop.aspect;

import java.lang.reflect.Method;

/**
 * @Author JAY
 * @Date 2019/6/23 12:21
 * @Description TODO
 **/
public interface JNJoinPoint {
    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
