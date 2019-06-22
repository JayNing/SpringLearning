package com.jayning.spring.framework.beans.factory.config;

/**
 * @Author JAY
 * @Date 2019/6/22 12:43
 * @Description TODO
 **/
public class JNBeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }
}
