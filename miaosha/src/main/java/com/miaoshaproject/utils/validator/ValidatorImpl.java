package com.miaoshaproject.utils.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean{

    private Validator validator;

    //实现校验方法并返回校验结果
    public ValidationResult validate(Object bean){
        ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size() > 0){ //有错误
            validationResult.setHasErrors(true);
            //使用JDK8的新功能 lambda表达式 循环遍历
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(propertyName, errMsg);
            });
        }
        return validationResult;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator通过 工厂的初始化方式 使其 实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
