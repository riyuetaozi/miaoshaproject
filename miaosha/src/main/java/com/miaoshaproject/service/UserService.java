package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.UserModel;


public interface UserService {
    /**
     * 通过用户id获取用户对象方法
     * @param id
     */
    UserModel getUserById(Integer id);

    /**
     * 用户注册
     * @param userModel
     */
    void register(UserModel userModel) throws BussinessException;

    /**
     * 用户登录
     * @param telphone 用户注册手机号
     * @param encrptPassword 用户加密后的密码
     */
    UserModel validataLogin(String telphone, String encrptPassword) throws BussinessException;

}
