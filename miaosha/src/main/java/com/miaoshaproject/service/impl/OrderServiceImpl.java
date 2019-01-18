package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDoMapper;
import com.miaoshaproject.dataobject.OrderDo;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.utils.validator.GenerateOrderNoImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDoMapper orderDoMapper;
    @Autowired
    private GenerateOrderNoImpl generateOrderNo;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BussinessException {
        //1.校验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if(userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "用户不存在");
        }
        if(amount <= 0 || amount > 99){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "数量信息不正确");
        }
        //校验活动信息
        if(promoId != null){
            //(1).校验对应活动是否存在这个商品
            if(promoId.intValue() != itemModel.getPromoModel().getId().intValue()){
                throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            }//(2).校验活动是否正在进行中
            else if(itemModel.getPromoModel().getStatus() != 2){
                throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
            }
        }
        //2.落单减库存；支付减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if(!result){
            throw new BussinessException(EmBussinessError.STOCK_NOT_ENOUGH);
        }
        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号（订单号）
        orderModel.setId(generateOrderNo.generateOrderNo());

        OrderDo orderDo = this.convertItemDoFromItemModel(orderModel);
        orderDoMapper.insertSelective(orderDo);

        //增加商品的销量
        itemService.increaseSales(itemId, amount);

        //4.返回前端
        return orderModel;
    }
    private OrderDo convertItemDoFromItemModel(OrderModel orderModel) {
        if(orderModel == null){
            return null;
        }
        OrderDo orderDo = new OrderDo();
        BeanUtils.copyProperties(orderModel, orderDo);
        orderDo.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDo.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDo;
    }

}