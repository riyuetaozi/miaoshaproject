package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderService {
    //1.（建议使用）通过前端url上传过来的秒杀活动id，然后在下单接口内校验对应id是否属于对应商品且活动已经开始
    //2.直接在下单接口内判断对应的商品是否存在秒杀活动，若存在进行中的则以秒杀价格下单
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BussinessException;

}
