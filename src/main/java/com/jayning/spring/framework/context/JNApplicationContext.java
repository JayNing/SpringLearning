package com.jayning.spring.framework.context;

import com.jayning.spring.annotation.JNAutowired;
import com.jayning.spring.annotation.JNController;
import com.jayning.spring.annotation.JNService;
import com.jayning.spring.framework.beans.factory.JNBeanFactory;
import com.jayning.spring.framework.beans.factory.JNBeanWrapper;
import com.jayning.spring.framework.beans.factory.config.JNBeanDefinition;
import com.jayning.spring.framework.beans.factory.config.JNBeanPostProcessor;
import com.jayning.spring.framework.beans.factory.support.JNBeanDefinitionReader;
import com.jayning.spring.framework.beans.factory.support.JNDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author JAY
 * @Date 2019/6/22 11:10
 * @Description TODO
 **/
public class JNApplicationContext extends JNDefaultListableBeanFactory implements JNBeanFactory {

    private String [] configLoactions;
    private JNBeanDefinitionReader reader;

    //通用的IOC容器
    private Map<String,JNBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public JNApplicationContext(String... configLoactions){
        this.configLoactions = configLoactions;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //依赖注入，从这里开始，通过读取BeanDefinition中的信息
    //然后，通过反射机制创建一个实例并返回
    //Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式：
    //1、保留原来的OOP关系
    //2、我需要对它进行扩展，增强（为了以后AOP打基础）
    @Override
    public Object getBean(String beanName) throws Exception {

        Object bean = null;

        //获取给定Bean的实例对象，主要是完成FactoryBean的相关处理
        //注意：BeanFactory是管理容器中Bean的工厂，而FactoryBean是
        //创建创建对象的工厂Bean，两者之间有区别

        //根据name获取beanDefinition
        JNBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        // 在生成beanWrapper之前，可以添加前置处理
        JNBeanPostProcessor postProcessor = new JNBeanPostProcessor();

        postProcessor.postProcessBeforeInitialization(bean,beanName);
        bean = instantiateBean(beanName,beanDefinition);
        //3、把这个对象封装到BeanWrapper中
        JNBeanWrapper beanWrapper = new JNBeanWrapper(bean);
        //4、把BeanWrapper存到IOC容器里面
        this.factoryBeanInstanceCache.put(beanName,beanWrapper);
        // 在生成beanWrapper之后，可以添加后置处理
        postProcessor.postProcessAfterInitialization(bean,beanName);

        //5、注入
        populateBean(beanName,new JNBeanDefinition(),beanWrapper);

        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    private void populateBean(String beanName, JNBeanDefinition jnBeanDefinition, JNBeanWrapper beanWrapper) {

        Object instance = beanWrapper.getWrappedInstance();

//        gpBeanDefinition.getBeanClassName();

        Class<?> clazz = beanWrapper.getWrappedClass();
        //判断只有加了注解的类，才执行依赖注入
        if(!(clazz.isAnnotationPresent(JNController.class) || clazz.isAnnotationPresent(JNService.class))){
            return;
        }

        //获得所有的fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(JNAutowired.class)){ continue;}

            JNAutowired autowired = field.getAnnotation(JNAutowired.class);

            String autowiredBeanName =  autowired.value().trim();
            if("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }

            //强制访问
            field.setAccessible(true);

            try {
                //为什么会为NULL，先留个坑
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){ continue; }
//                if(instance == null){
//                    continue;
//                }
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    private Object instantiateBean(String beanName, JNBeanDefinition beanDefinition) {
        //1、拿到要实例化的对象的类名
        String className = beanDefinition.getBeanClassName();
        //2、反射实例化，得到一个对象
        Object instance = null;
        try{
            //此处省略单例的判断，如果是单例，则先去单例缓存中取，如果没取到，通过反射生成一个，再把生成的放入单例缓存
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        return instance;
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        return null;
    }

    @Override
    public boolean containsBean(String name) {
        return false;
    }

    @Override
    public boolean isSingleton(String name) {
        return false;
    }

    @Override
    public void refresh() throws Exception {
        //1、定位，定位配置文件
        reader = new JNBeanDefinitionReader(this.configLoactions);

        //2、加载配置文件，扫描相关的类，把它们封装成BeanDefinition
        List<JNBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3、注册，把配置信息放到容器里面(伪IOC容器)
        doRegisterBeanDefinition(beanDefinitions);

        //4、把不是延时加载的类，有提前初始化
        doAutowrited();
    }

    //只处理非延时加载的情况,包含DI操作
    private void doAutowrited() {
        for (Map.Entry<String, JNBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            //判断是否是懒加载
            if (!beanDefinitionEntry.getValue().isLazyInit()){
                try {
                    getBean(beanDefinitionEntry.getKey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doRegisterBeanDefinition(List<JNBeanDefinition> beanDefinitions) throws Exception {
        if (beanDefinitions.size() == 0){
            return;
        }

        for (JNBeanDefinition beanDefinition : beanDefinitions) {
            if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
        //到这里为止，容器初始化完毕
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new  String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}
