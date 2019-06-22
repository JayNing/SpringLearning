package com.jayning.spring.demo.controller;

import com.jayning.spring.annotation.JNAutowired;
import com.jayning.spring.annotation.JNController;
import com.jayning.spring.annotation.JNRequestMapping;
import com.jayning.spring.annotation.JNRequestParam;
import com.jayning.spring.demo.service.UserService;
import com.jayning.spring.framework.context.JNApplicationContext;
import com.jayning.spring.framework.webmvc.servlet.JNModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author JAY
 * @Date 2019/6/9 13:21
 * @Description TODO
 **/
@JNController
@JNRequestMapping("test")
public class TestController {

    @JNAutowired
    private UserService userService;

    @JNRequestMapping("get")
    public String getName(@JNRequestParam("name") String name){
        return "My name is " + name;
    }

    @JNRequestMapping("add")
    public String addUser(@JNRequestParam("name") String name,@JNRequestParam("password") String password){
        return userService.addUser(name,password);
    }

    @JNRequestMapping("/query.json")
    public JNModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                @JNRequestParam("name") String name){
        String result = userService.query(name);
        return out(response,result);
    }

    private JNModelAndView out(HttpServletResponse resp,String str){
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
