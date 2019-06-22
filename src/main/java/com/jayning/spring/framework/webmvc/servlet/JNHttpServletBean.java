package com.jayning.spring.framework.webmvc.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @Author JAY
 * @Date 2019/6/22 10:21
 * @Description TODO
 **/
public abstract class JNHttpServletBean extends HttpServlet {

    @Override
    public final void init() throws ServletException {
        //调用了JNFrameworkServlet的initServletBean();
        initServletBean();
    }

    //创建钩子方法，供子类实现
    public void initServletBean() throws ServletException {
    }

}
