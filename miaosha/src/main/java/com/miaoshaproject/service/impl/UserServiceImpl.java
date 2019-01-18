package com.miaoshaproject.service.impl;


import com.miaoshaproject.dao.UserDoMapper;
import com.miaoshaproject.dao.UserPasswordDoMapper;
import com.miaoshaproject.dataobject.UserDo;
import com.miaoshaproject.dataobject.UserPasswordDo;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.utils.validator.ValidationResult;
import com.miaoshaproject.utils.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDoMapper userDoMapper;
    @Autowired
    private UserPasswordDoMapper userPasswordDoMapper;
    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userDoMapper获取到对应的用户dataObject
        UserDo userDo = userDoMapper.selectByPrimaryKey(id);
        if(userDo == null){
            return null;
        }
        //通过用户id获取用户密码信息
        UserPasswordDo userPasswordDo = userPasswordDoMapper.selectByUserId(userDo.getId());
        return this.convertUserModelFromUserDoAndPwdDo(userDo, userPasswordDo);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BussinessException {
        if(userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        ValidationResult validationResult = validator.validate(userModel);
        if(validationResult.isHasErrors()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }

        //实现model -> dataobject 方法
        UserDo userDo = this.convertUserDoFromUserModel(userModel);
        try {
            userDoMapper.insertSelective(userDo);
        }catch (DuplicateKeyException ex){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "手机号已经重复注册");
        }
        userModel.setId(userDo.getId());

        UserPasswordDo userPasswordDo = this.convertUserPasswordDoFromUserModel(userModel);
        userPasswordDoMapper.insertSelective(userPasswordDo);
    }

    @Override
    public UserModel validataLogin(String telphone, String encrptPassword) throws BussinessException {
        //通过用户的手机获取用户信息
        UserDo userDo = userDoMapper.selectByTelphone(telphone);
        if(userDo == null){
            throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDo userPasswordDo = userPasswordDoMapper.selectByUserId(userDo.getId());
        UserModel userModel = this.convertUserModelFromUserDoAndPwdDo(userDo, userPasswordDo);

        //对比用户信息内加密的密码是否和传输进来的密码相匹配
        if(!StringUtils.equals(encrptPassword, userModel.getEncrptPassword())) {
            throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    private UserDo convertUserDoFromUserModel(UserModel userModel) throws BussinessException {
        if(userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserDo userDo = new UserDo();
        BeanUtils.copyProperties(userModel, userDo);
        return userDo;
    }
    private UserPasswordDo convertUserPasswordDoFromUserModel(UserModel userModel) throws BussinessException {
        if(userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserPasswordDo userPasswordDo = new UserPasswordDo();
        userPasswordDo.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDo.setUserId(userModel.getId());
        return userPasswordDo;
    }
    private UserModel convertUserModelFromUserDoAndPwdDo(UserDo userDo, UserPasswordDo userPasswordDo){
        if(userDo == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDo, userModel);

        if(userPasswordDo != null){
            userModel.setEncrptPassword(userPasswordDo.getEncrptPassword());
        }
        return  userModel;
    }
}
