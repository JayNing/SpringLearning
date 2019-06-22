package com.jayning.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @Author JAY
 * @Date 2019/6/22 13:45
 * @Description TODO
 **/
public class JNModelAndView {

    private String viewName;
    private Map<String,?> model;

    public JNModelAndView(String viewName) {
        this.viewName = viewName;
    }
    public JNModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
