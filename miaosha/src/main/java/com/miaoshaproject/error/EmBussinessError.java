package com.miaoshaproject.error;

/**
 * 通过枚举获取异常信息（所有的自定义异常信息码，统一在这里处理）
 * 枚举本质上就是面向对象的类，所以可以有变量
 */
public enum EmBussinessError implements CommonError {
    //通用错误类型1000*
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    UNKONW_ERROR(10002, "未知错误"),

    //2000* 开头为用户信息相关错误定义
    USER_NOT_EXIST(20001, "用户信息不存在"),
    USER_LOGIN_FAIL(20002, "用户手机号或密码不正确"),
    USER_NOT_LOGIN(20003, "用户未登录"),

    //3000*开头为交易信息相关错误定义
    STOCK_NOT_ENOUGH(30001, "库存不足"),
    ;

    private int errCode;
    private String errMsg;

    private EmBussinessError(int errCode, String errMesg){
        this.errCode = errCode;
        this.errMsg = errMesg;
    }

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
