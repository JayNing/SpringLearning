package com.jayning.spring.framework.aop.aspect;

import com.jayning.spring.framework.aop.intercept.JNMethodInterceptor;
import com.jayning.spring.framework.aop.intercept.JNMethodInvocation;

import java.lang.reflect.Method;

/**
 * @Author JAY
 * @Date 2019/6/23 12:28
 * @Description TODO
 **/
public class JNAfterReturningAdviceInterceptor extends JNAbstractAspectAdvice implements JNAdvice,JNMethodInterceptor {

    private JNJoinPoint joinPoint;

    public JNAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(JNMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal,mi.getMethod(),mi.getArguments(),mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint,retVal,null);
    }
}
