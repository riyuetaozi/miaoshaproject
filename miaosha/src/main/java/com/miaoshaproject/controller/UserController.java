package com.miaoshaproject.controller;


import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVo;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 用户信息控制器
 *
 * @CrossOrigin 注解是解决前端跨越请求，
 * 注意：方法上需要注明RequestMethod的值；
 * session共享问题,@CrossOrigin注解需要加入allowCredentials = "true", allowedHeaders = "*"
 * 说明：DEFAULT_ALLOWED_HEADERS ：允许跨域传输所有的header参数，将用于使用token放入header域做session共享的跨域请求
 * DEFAULT_ALLOW_CREDENTIALS = true ：需配合前端设置xhrFields授信后使得跨域session共享（也就是前端所有ajax需要加入 xhrFields:{withCredentials:true} 参数）
 */
@Controller
@RequestMapping("/user")
@CrossOrigin(origins = "*", allowCredentials = "true")
//@CrossOrigin(allowedHeaders = "*", allowCredentials = "true") //或者这样写也是可以的

public class UserController extends BaseController{

    @Autowired
    private UserService userService;


    /**
     * 这里使用spring的bean的方式将HttpServletRequest注入进来（spring的bean注入，是单例模式，也就是说这里的HttpServletRequest是单例的模式）
     *
     * 单例的模式怎么可以支持一个request，使多个用户的并发访问呢？
     * 其实这个通过spring Bean包装的HttpServletRequest，它的本质是一个proxy(代理)，它的内部拥有ThreadLocal方式的Map，去让用户在每个线程当中处理它自己对应的request，并且由ThreadLocal清除的机制
     */
    @Autowired
    private HttpServletRequest httpServletRequest;

    @PostMapping(value = "/login", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name="password")String password)
            throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登录服务，用来校验用户密码是否合法
        UserModel userModel = userService.validataLogin(telphone, this.EncodeByMd5(password));

        //将登录凭证加入到用户登录成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);
    }
    @PostMapping(value = "/getotp", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone){
        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);//random.nextInt：0到最大数值(包含0不包含最大值)  此时随机数取值区间：[0,99999)
        randomInt += 10000;//加上一个数值之后，此时随机数取值区间：[10000,99999)
        String otpCode = String.valueOf(randomInt);

        /**
         * 将OTP验证码与对应的用户手机号关联
         * （分布式中，存放在redis中，redis能存储key-value的方式，并且可以简单的控制时间有效性）
         * 这里我们还没有涉及到分布式，所以使用HTTPSession的方式绑定他的手机号与otpcode
         */
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        //将OTP验证码通过短信通道发送给用户（这里省略，短信发送是可以开发短信平台完成，这里只调用短信平台提供的相关接口即可）
        System.out.println("telphone:" + telphone + "&otpCode:" + otpCode);
        return CommonReturnType.create(null);
    }


    @PostMapping("/register")
    @ResponseBody
    public CommonReturnType register (@RequestParam(name = "name")String name,
                                      @RequestParam(name = "age")Integer age,
                                      @RequestParam(name = "gender")Integer gender,
                                      @RequestParam(name = "password")String password,
                                      @RequestParam(name = "telphone")String telphone,
                                      @RequestParam(name = "otpCode")String otpCode)
            throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //验证手机号和对应的otpcode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(otpCode, inSessionOtpCode)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender)));
        userModel.setName(name);
        userModel.setTelphone(telphone);
        userModel.setEncrptPassword(EncodeByMd5(password));
        userService.register(userModel);

        return CommonReturnType.create(null);
    }

    @GetMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BussinessException{
        //调用service服务获取用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        if(userModel == null){
            throw new BussinessException(EmBussinessError.USER_NOT_EXIST);
        }
        return CommonReturnType.create(convertFormViewObject(userModel));
    }

    private UserVo convertFormViewObject(UserModel userModel) {
        if(userModel == null){
            return null;
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userModel, userVo);
        return userVo;
    }
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密字符串
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }


}
