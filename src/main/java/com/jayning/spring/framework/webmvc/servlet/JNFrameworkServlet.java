package com.jayning.spring.framework.webmvc.servlet;

import javax.servlet.ServletException;

/**
 * @Author JAY
 * @Date 2019/6/22 10:25
 * @Description TODO
 **/
public class JNFrameworkServlet extends JNHttpServletBean {

    @Override
    public final void initServletBean() throws ServletException {
        //1、调用了 DispatcherServlet 重写的 onRefresh()方法
        onRefresh();
    }

    public void onRefresh() {
    }

}
