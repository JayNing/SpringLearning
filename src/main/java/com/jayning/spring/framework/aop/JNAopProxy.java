package com.jayning.spring.framework.aop;

/**
 * @Author JAY
 * @Date 2019/6/23 12:31
 * @Description TODO
 **/
public interface JNAopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
