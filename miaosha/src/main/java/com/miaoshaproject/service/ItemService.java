package com.miaoshaproject.service;

import com.miaoshaproject.error.BussinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    /**
     * 创建商品
     * @param itemModel
     * @return
     */
    ItemModel createItem(ItemModel itemModel) throws BussinessException;

    /**
     * 商品列表浏览
     * @return
     */
    List<ItemModel> listItem();

    /**
     * 商品详情浏览
     * @param id
     * @return
     */
    ItemModel getItemById(Integer id);

    /**
     * 库存扣减
     * @param itemId
     * @param amount
     * @return
     * @throws BussinessException
     */
    boolean decreaseStock(Integer itemId, Integer amount)throws BussinessException;

    /**
     * 商品销量增加
     * @param itemId
     * @param amount
     * @throws BussinessException
     */
    void increaseSales(Integer itemId, Integer amount)throws BussinessException;
}
