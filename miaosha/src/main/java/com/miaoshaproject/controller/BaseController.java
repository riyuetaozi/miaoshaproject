package com.miaoshaproject.controller;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    //springboot中定义exceptionhandler解决未被controller层吸收处理的exception
    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(HttpServletRequest request, Exception ex){
        Map<String, Object> responseData = new HashMap<>();
        if(ex instanceof BussinessException){
            BussinessException bussinessException = (BussinessException)ex;
            responseData.put("errCode", bussinessException.getErrCode());
            responseData.put("errMsg", bussinessException.getErrMsg());
        }else{
            logger.error("出错啦：", ex);
            responseData.put("errCode", EmBussinessError.UNKONW_ERROR.getErrCode());
            responseData.put("errMsg", EmBussinessError.UNKONW_ERROR.getErrMsg());
        }
        return CommonReturnType.create(responseData, "fail");
    }
}
