package com.miaoshaproject.utils.validator;


import com.miaoshaproject.dao.SequenceInfoDoMapper;
import com.miaoshaproject.dataobject.SequenceInfoDo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GenerateOrderNoImpl {


    @Autowired
    private SequenceInfoDoMapper sequenceInfoDoMapper;

    //@Transactional注解的propagation的值为 REQUIRES_NEW时，就是这段代码表明重新开启一个是否，不管外部的事务是否重构，这个事务都要提交，不受外部事务影响
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo(){
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder(16);
        //前8位为时间信息，年月日（yyyyMMdd）
        LocalDateTime now = LocalDateTime.now();
        String newDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(newDate);

        //中间6位为自增序列
        //获取当前的sequence
        Integer sequence = 0;
        SequenceInfoDo sequenceInfoDo = sequenceInfoDoMapper.getSequenceByName("order_info");
        sequence = sequenceInfoDo.getCurrentValue();
        sequenceInfoDo.setCurrentValue(sequenceInfoDo.getCurrentValue() + sequenceInfoDo.getStep());
        sequenceInfoDoMapper.updateByPrimaryKeySelective(sequenceInfoDo);
        //补齐6位
        String sequenceStr = String.valueOf(sequence);
        for(int i = 0; i < 6-sequenceStr.length(); i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //最后2位为分库分表位( 00 到 99)，后续的订单的水平拆分
        /*
        //伪代码，扩展
        //Integer userId = 1000122;//用户id始终不变
        //(userId % 100) 计算得到的值，作为用户最后2位的分库分表位，
        //也就是说，我们保证订单可以最终被拆分到100个库里对应的100个表里面，分散我们数据库查询和追加落单的压力
        //只要保证用户id不变，所产生的订单号就会永远落在某一个固定的库固定的表上面，这2位数就是分库分表的路由信息；
         */
        stringBuilder.append("00");//目前写死00

        return stringBuilder.toString();
    }


}
