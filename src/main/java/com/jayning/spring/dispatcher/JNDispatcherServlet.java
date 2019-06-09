package com.jayning.spring.dispatcher;

import com.jayning.spring.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Author JAY
 * @Date 2019/6/9 13:14
 * @Description TODO
 **/
public class JNDispatcherServlet extends HttpServlet {

    //加载配置文件到内存
    private Properties contextConfig = new Properties();
    //扫描目标包下的所有类，存入List容器
    List<String> classList = new ArrayList<>();
    //IOC容器
    private Map<String, Object> ioc = new HashMap<>();
    //handlerMapping容器
    private Map<String, Method> handlerMapping = new HashMap<>();



    @Override
    public void init(ServletConfig config) throws ServletException {
        //模板模式

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        //3、初始化所有相关的类的实例【带有类相关注解的】，并且放入到IOC容器之中
        doInstance();
        //4、完成依赖注入
        doAutowired();
        //5、初始化HandlerMapping
        initHandlerMapping();

        System.out.println("JN Spring framework is init.");
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Object clazz = entry.getValue();
            if (!clazz.getClass().isAnnotationPresent(JNController.class)) {
                continue;
            }
            //只有controller层，才需要初始化方法映射
            JNRequestMapping controllerAnnotation = clazz.getClass().getAnnotation(JNRequestMapping.class);
            String baseUrl = controllerAnnotation.value();

            Method[] methods = clazz.getClass().getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(JNRequestMapping.class)){
                    continue;
                }
                //只有带有注解的，才能作为url映射
                JNRequestMapping methodAnnotation = method.getAnnotation(JNRequestMapping.class);
                String methodUrl = methodAnnotation.value();
                //  (//demo//query)
                //有时候url中会出现//多个/这种形式，下面进行兼容过滤
                //正则表达式， /+ 表示有多个/
                String url = ("/" + baseUrl + "/" + methodUrl).replaceAll("/+","/");
                handlerMapping.put(url, method);
                System.out.println("Mapped " + url + "," + method);
            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Object value = entry.getValue();
            //拿到实例对象中的所有属性
            Field[] declaredFields = value.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (!field.isAnnotationPresent(JNAutowired.class)){
                    //说明此属性不是依赖注入进来的
                    continue;
                }
                //如果是依赖进来的属性，需要set注入
                JNAutowired annotation = field.getAnnotation(JNAutowired.class);
                String beanName = annotation.value();
                if ("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                //强行授权，允许重新设置属性值
                field.setAccessible(true);
                try {
                    //执行注入动作
                    field.set(value, ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue ;
                }
            }
        }
    }

    private void doInstance() {
        if (classList.isEmpty()){
            return;
        }
        try {
            for (String className : classList) {
                Class<?> aClass = Class.forName(className);
                if (aClass.isAnnotationPresent(JNController.class)){
                    String beanName = toLowerFirstCase(aClass.getSimpleName());
                    Object instance = aClass.newInstance();
                    ioc.put(beanName, instance);
                }else if (aClass.isAnnotationPresent(JNService.class)){
                    //1、默认的类名首字母小写
                    String beanName = toLowerFirstCase(aClass.getSimpleName());
                    //service注解需要考虑自定义命名
                    JNService serviceAnnotation = aClass.getAnnotation(JNService.class);
                    String value = serviceAnnotation.value();
                    Object instance = aClass.newInstance();

                    if (null != value && !"".equals(value)){
                        //如果有自定义service名
                        beanName = value;
                        ioc.put(beanName, instance);
                    }
                    //3、根据类型注入实现类，投机取巧的方式
                    for (Class<?> i : aClass.getInterfaces()) {
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The beanName is exists!!");
                        }
                        ioc.put(i.getName(),instance);
                    }
                }else {
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return  String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        //包传过来包下面的所有的类全部扫描进来的
        URL url = this.getClass().getClassLoader()
                .getResource("/" + scanPackage.replaceAll("\\.","/"));
        File scanFile = new File(url.getFile());
        if (!scanFile.exists()){
            return;
        }
        for (File file : scanFile.listFiles()) {
            if (file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            } else {
                //不是文件夹,代码运行时，文件全是.class文件，所以要过滤掉不是.class的文件
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class","");
                classList.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        try {
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //派遣，分发任务
        try {
            //委派模式
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Excetion Detail:" +Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        //获取请求url
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI.replaceAll(contextPath,"").replaceAll("/+","/");
        //从handlerMapping中找到映射方法
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!");
            return;
        }
        Method method = handlerMapping.get(url);
        //获取请求中的参数
        Map<String,String[]> parametera = req.getParameterMap();
        //使用反射找到执行方法所需要的参数
        //获取方法的形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        Map<String,String[]> parameterMap = req.getParameterMap();
        //保存赋值参数的位置
        Object [] paramValues = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i ++){
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class){
                paramValues[i] = req;
                continue;
            }else if(parameterType == HttpServletResponse.class){
                paramValues[i] = resp;
                continue;
            }else if(parameterType == String.class){
                //提取方法中加了注解的参数
                //一个方法可以有多个参数，一个参数可以有多个不同的注解
                Annotation[] [] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length ; j ++) {
                    for(Annotation a : pa[i]){
                        if(a instanceof JNRequestParam){
                            String paramName = ((JNRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s",",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }

        //利用反射，invoke调用方法，得到返回结果
        //投机取巧的方式
        //通过反射拿到method所在class，拿到class之后还是拿到class的名称
        //再调用toLowerFirstCase获得beanName
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        Object invoke = method.invoke(ioc.get(beanName), paramValues);
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(invoke.toString());
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
