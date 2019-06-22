package com.jayning.spring.framework.beans.factory;

import com.sun.istack.internal.Nullable;

/**
 * @Author JAY
 * @Date 2019/6/22 10:29
 * @Description TODO
 **/
public interface JNBeanFactory {

    //根据bean的名字，获取在IOC容器中得到bean实例
    Object getBean(String name) throws Exception;

    <T> T getBean(String name, @Nullable Class<T> requiredType);

    //提供对bean的检索，看看是否在IOC容器有这个名字的bean
    boolean containsBean(String name);

    //根据bean名字得到bean实例，并同时判断这个bean是不是单例
    boolean isSingleton(String name);



}
