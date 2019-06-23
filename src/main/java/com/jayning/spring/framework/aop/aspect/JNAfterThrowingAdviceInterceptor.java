package com.jayning.spring.framework.aop.aspect;

import com.jayning.spring.framework.aop.intercept.JNMethodInterceptor;
import com.jayning.spring.framework.aop.intercept.JNMethodInvocation;

import java.lang.reflect.Method;

/**
 * Created by Tom on 2019/4/15.
 */
public class JNAfterThrowingAdviceInterceptor extends JNAbstractAspectAdvice implements JNAdvice,JNMethodInterceptor {


    private String throwingName;

    public JNAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(JNMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }catch (Throwable e){
            invokeAdviceMethod(mi,null,e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName){
        this.throwingName = throwName;
    }
}
