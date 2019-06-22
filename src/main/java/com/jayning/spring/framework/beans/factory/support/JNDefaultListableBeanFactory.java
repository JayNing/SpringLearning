package com.jayning.spring.framework.beans.factory.support;

import com.jayning.spring.framework.beans.factory.config.JNBeanDefinition;
import com.jayning.spring.framework.context.support.JNAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author JAY
 * @Date 2019/6/22 10:58
 * @Description TODO
 **/
public class JNDefaultListableBeanFactory extends JNAbstractApplicationContext {

    //存储注册信息的BeanDefinition,伪IOC容器
    public final Map<String, JNBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, JNBeanDefinition>();


}
