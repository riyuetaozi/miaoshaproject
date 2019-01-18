package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDoMapper;
import com.miaoshaproject.dao.ItemStockDoMapper;
import com.miaoshaproject.dataobject.ItemDo;
import com.miaoshaproject.dataobject.ItemStockDo;
import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.error.EmBussinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.utils.validator.ValidationResult;
import com.miaoshaproject.utils.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ItemDoMapper itemDoMapper;
    @Autowired
    private ItemStockDoMapper itemStockDoMapper;
    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private PromoService promoService;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BussinessException {

        //校验入参
        ValidationResult validationResult = validator.validate(itemModel);
        if (validationResult.isHasErrors()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }

        //转化itemmodel -> dataobject
        ItemDo itemDo = this.convertItemDoFromItemModel(itemModel);
        //写入数据库
        itemDoMapper.insertSelective(itemDo);
        itemModel.setId(itemDo.getId());

        //转化itemmodel -> dataobject
        ItemStockDo itemStockDo = this.convertItemStockDoFromItemModel(itemModel);
        //写入数据库
        itemStockDoMapper.insertSelective(itemStockDo);

        //返回创建完成的对象
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDo> itemDoList = itemDoMapper.selectItemList();
        //使用jdk8的新功能 List集合的stream() api功能，将List集合中的 ItemDo和ItemStockDo 转化为 ItemModel
        List<ItemModel> itemModelList = itemDoList.stream().map(itemDo -> {
            ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());
            ItemModel itemModel = this.convertItemModelFromItemDOAndStockDo(itemDo, itemStockDo);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDo itemDo = itemDoMapper.selectByPrimaryKey(id);
        if(itemDo == null){
            return null;
        }
        //操作获得库存数量
        ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(id);
        if(itemStockDo == null){
            return null;
        }
        ItemModel itemModel = this.convertItemModelFromItemDOAndStockDo(itemDo, itemStockDo);
        //获取商品活动信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus() != 3){
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int affectedRow = itemStockDoMapper.decreaseStock(itemId, amount);
        if(affectedRow > 0){
            //更新库存成功
            return true;
        }else {
            //更新库存失败
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BussinessException {
        itemDoMapper.increaseSales(itemId, amount);
    }

    private ItemDo convertItemDoFromItemModel(ItemModel itemModel) throws BussinessException {
        if(itemModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        ItemDo itemDo = new ItemDo();
        BeanUtils.copyProperties(itemModel, itemDo);
        itemDo.setPrice(itemModel.getPrice().doubleValue());
        return itemDo;
    }
    private ItemStockDo convertItemStockDoFromItemModel(ItemModel itemModel) throws BussinessException {
        if(itemModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        ItemStockDo itemStockDo = new ItemStockDo();
        itemStockDo.setStock(itemModel.getStock());
        itemStockDo.setItemId(itemModel.getId());
        return itemStockDo;
    }
    private ItemModel convertItemModelFromItemDOAndStockDo(ItemDo itemDo, ItemStockDo itemStockDo) {
        if(itemDo == null || itemStockDo == null){
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDo, itemModel);
        itemModel.setPrice(new BigDecimal(itemDo.getPrice()));
        itemModel.setStock(itemStockDo.getStock());
        return itemModel;
    }
}
