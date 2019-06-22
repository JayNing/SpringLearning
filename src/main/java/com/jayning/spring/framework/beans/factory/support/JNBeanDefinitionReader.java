package com.jayning.spring.framework.beans.factory.support;

import com.jayning.spring.framework.beans.factory.config.JNBeanDefinition;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Author JAY
 * @Date 2019/6/22 11:40
 * @Description TODO
 **/
public class JNBeanDefinitionReader {

    private List<String> registyBeanClasses = new ArrayList<>();

    private Properties config = new Properties();

    //固定配置文件中的key，相对于xml的规范
    private final String SCAN_PACKAGE = "scanPackage";

    public JNBeanDefinitionReader(String ... configLoactions) {
        //通过URL定位找到其所对应的文件，然后转换为文件流
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLoactions[0].replace("classpath:",""));
            config.load(is);
        }catch (Exception e){
            e.printStackTrace();
        }

        //扫描路径，获取所有的beanName
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    private void doScanner(String scanPackage) {
        //把多个\换成一个\
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else{
                if(!file.getName().endsWith(".class")){ continue;}
                String className = (scanPackage + "." + file.getName().replace(".class",""));
                registyBeanClasses.add(className);
            }
        }
    }

    public Properties getConfig(){
        return this.config;
    }

    /**
     * Bean读取器真正实现加载的方法
     * @return
     */
    public List<JNBeanDefinition> loadBeanDefinitions() {
        List<JNBeanDefinition> definitions = new ArrayList<>();
        if (registyBeanClasses.size() == 0){
            return definitions;
        }
        try{
            for (String beanName : registyBeanClasses) {
                Class<?> beanClass = Class.forName(beanName);
                //如果是一个接口，是不能实例化的
                //用它实现类来实例化
                if(beanClass.isInterface()) { continue; }
                //beanName有三种情况:
                //1、默认是类名首字母小写
                //2、自定义名字
                //3、接口注入
                String simpleName = beanClass.getSimpleName();
                definitions.add(new JNBeanDefinition(toLowerFirstCase(simpleName),beanName));
                Class<?> [] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    //如果是多个实现类，只能覆盖
                    //为什么？因为Spring没那么智能，就是这么傻
                    //这个时候，可以自定义名字
                    definitions.add(new JNBeanDefinition(i.getName(),beanClass.getName()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return definitions;
    }

    //如果类名本身是小写字母，确实会出问题
    //但是我要说明的是：这个方法是我自己用，private的
    //传值也是自己传，类也都遵循了驼峰命名法
    //默认传入的值，存在首字母小写的情况，也不可能出现非字母的情况

    //为了简化程序逻辑，就不做其他判断了，大家了解就OK
    //其实用写注释的时间都能够把逻辑写完了
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
