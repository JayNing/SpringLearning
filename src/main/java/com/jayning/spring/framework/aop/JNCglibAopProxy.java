package com.jayning.spring.framework.aop;

import com.jayning.spring.framework.aop.support.JNAdvisedSupport;

/**
 * Created by Tom on 2019/4/14.
 */
public class JNCglibAopProxy implements  JNAopProxy {
    public JNCglibAopProxy(JNAdvisedSupport config) {
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
