package com.jayning.spring.demo.service.impl;

import com.jayning.spring.annotation.JNService;
import com.jayning.spring.demo.service.UserService;

/**
 * @Author JAY
 * @Date 2019/6/9 13:25
 * @Description TODO
 **/
@JNService
public class UserServiceImpl implements UserService {

    @Override
    public String addUser(String username, String password) {
        return "Add new user, the name is " + username + " and password is " + password;
    }
}
