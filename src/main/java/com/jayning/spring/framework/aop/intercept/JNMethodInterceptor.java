package com.jayning.spring.framework.aop.intercept;

/**
 * @Author JAY
 * @Date 2019/6/23 12:23
 * @Description TODO
 **/
public interface JNMethodInterceptor {
    Object invoke(JNMethodInvocation invocation) throws Throwable;
}
