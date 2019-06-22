package com.jayning.spring.framework.beans.factory;

/**
 * @Author JAY
 * @Date 2019/6/22 12:16
 * @Description TODO
 **/
public class JNBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public JNBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }


    // 返回代理以后的Class
    // 可能会是这个 $Proxy0
    public Class<?> getWrappedClass(){
        return this.wrappedInstance.getClass();
    }
}
