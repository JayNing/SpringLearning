package com.jayning.spring.framework.aop.aspect;

import com.jayning.spring.framework.aop.intercept.JNMethodInterceptor;
import com.jayning.spring.framework.aop.intercept.JNMethodInvocation;

import java.lang.reflect.Method;

/**
 * @Author JAY
 * @Date 2019/6/23 12:22
 * @Description TODO
 **/
public class JNMethodBeforeAdviceInterceptor extends JNAbstractAspectAdvice implements JNAdvice,JNMethodInterceptor {

    private JNJoinPoint joinPoint;

    public JNMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    private void before(Method method,Object[] args,Object target) throws Throwable{
        //传送了给织入参数
        //method.invoke(target);
        super.invokeAdviceMethod(this.joinPoint,null,null);

    }
    @Override
    public Object invoke(JNMethodInvocation mi) throws Throwable {
        //从被织入的代码中才能拿到，JoinPoint
        this.joinPoint = mi;
        before(mi.getMethod(), mi.getArguments(), mi.getThis());
        return mi.proceed();
    }
}
