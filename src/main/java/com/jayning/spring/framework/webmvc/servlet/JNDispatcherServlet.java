package com.jayning.spring.framework.webmvc.servlet;

import com.jayning.spring.annotation.JNController;
import com.jayning.spring.annotation.JNRequestMapping;
import com.jayning.spring.framework.beans.factory.config.JNBeanDefinition;
import com.jayning.spring.framework.context.JNApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author JAY
 * @Date 2019/6/22 11:42
 * @Description TODO
 **/
public class JNDispatcherServlet extends JNFrameworkServlet {

    private final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";
    private JNApplicationContext context;
    private List<JNHandlerMapping> handlerMappings = new ArrayList<JNHandlerMapping>();
    private Map<JNHandlerMapping,JNHandlerAdapter> handlerAdapters = new HashMap<JNHandlerMapping,JNHandlerAdapter>();
    private List<JNViewResolver> viewResolvers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、初始化ApplicationContext,此时，如果bean不是懒加载的，则在refresh中，通过getBean方法进行一轮依赖注入
        context = new JNApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));
        //2、init方法中，在执行initHandlerMapping的时候，会再进行一轮getBean操作，里面会重新对bean进行依赖注入
        initStrategies(context);

        //3、两轮的依赖注入，可以保证循环依赖的bean，能够被注入到各自的依赖类中去

    }

    @Override
    public void onRefresh() {
        //2、初始化Spring MVC 九大组件
        initStrategies(context);
    }

    //初始化策略
    protected void initStrategies(JNApplicationContext context) {
        //多文件上传的组件
        initMultipartResolver(context);
        //初始化本地语言环境
        initLocaleResolver(context);
        //初始化模板处理器
        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
        //参数缓存器
        initFlashMapManager(context);
    }

    private void initFlashMapManager(JNApplicationContext context) {
    }

    private void initViewResolvers(JNApplicationContext context) {
        //拿到模板的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();
        for (int i = 0; i < templates.length; i ++) {
            //这里主要是为了兼容多模板，所有模仿Spring用List保存
            //在我写的代码中简化了，其实只有需要一个模板就可以搞定
            //只是为了仿真，所有还是搞了个List
            this.viewResolvers.add(new JNViewResolver(templateRoot));
        }
    }

    private void initRequestToViewNameTranslator(JNApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(JNApplicationContext context) {
    }

    private void initHandlerAdapters(JNApplicationContext context) {
        //把一个requet请求变成一个handler，参数都是字符串的，自动配到handler中的形参

        //可想而知，他要拿到HandlerMapping才能干活
        //就意味着，有几个HandlerMapping就有几个HandlerAdapter
        for (JNHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new JNHandlerAdapter());
        }
    }

    private void initHandlerMappings(JNApplicationContext context) {

        String [] beanNames = context.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            try {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(JNController.class)){
                    continue;
                }
                //只有controller层才会解析方法handler
                String baseUrl = "";
                //获取Controller的url配置
                if(clazz.isAnnotationPresent(JNRequestMapping.class)){
                    JNRequestMapping requestMapping = clazz.getAnnotation(JNRequestMapping.class);
                    baseUrl = requestMapping.value();
                }

                //获取Method的url配置
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {

                    //没有加RequestMapping注解的直接忽略
                    if(!method.isAnnotationPresent(JNRequestMapping.class)){ continue; }

                    //映射URL
                    JNRequestMapping requestMapping = method.getAnnotation(JNRequestMapping.class);
                    //  /demo/query

                    //  (//demo//query)

                    String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);

                    this.handlerMappings.add(new JNHandlerMapping(pattern,controller,method));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void initThemeResolver(JNApplicationContext context) {
    }

    private void initLocaleResolver(JNApplicationContext context) {
    }

    private void initMultipartResolver(JNApplicationContext context) {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            this.doDispatch(req,resp);
        }catch(Exception e){
            resp.getWriter().write("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
//            new GPModelAndView("500");

        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、通过从request中拿到URL，去匹配一个HandlerMapping
        JNHandlerMapping handler = getHandler(req);
        if(handler == null){
            processDispatchResult(req,resp,new JNModelAndView("404"));
            return;
        }
        //2、准备调用前的参数
        JNHandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        //3、真正的调用方法,返回ModelAndView存储了要穿页面上值，和页面模板的名称
        JNModelAndView mv = null;
        mv = handlerAdapter.handle(req,resp,handler);
        //这一步才是真正的输出
        processDispatchResult(req, resp, mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, JNModelAndView mv) throws Exception {
        if(null == mv){return;}
        if(this.viewResolvers.isEmpty()){return;}
        for (JNViewResolver viewResolver : this.viewResolvers) {
            JNView view = viewResolver.resolveViewName(mv.getViewName(),null);
            view.render(mv.getModel(),req,resp);
            return;
        }

    }

    private JNHandlerAdapter getHandlerAdapter(JNHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return null;}
        JNHandlerAdapter ha = this.handlerAdapters.get(handler);
        if(ha.supports(handler)){
            return ha;
        }
        return null;
    }

    private JNHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){ return null; }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        for (JNHandlerMapping handler : this.handlerMappings) {
            try{
                Matcher matcher = handler.getPattern().matcher(url);
                //如果没有匹配上继续下一个匹配
                if(!matcher.matches()){ continue; }

                return handler;
            }catch(Exception e){
                throw e;
            }
        }
        return null;
    }

}
