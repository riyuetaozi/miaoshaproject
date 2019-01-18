package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.ItemStockDo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemStockDoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Tue Jan 15 10:15:48 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Tue Jan 15 10:15:48 CST 2019
     */
    int insert(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Tue Jan 15 10:15:48 CST 2019
     */
    int insertSelective(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Tue Jan 15 10:15:48 CST 2019
     */
    ItemStockDo selectByPrimaryKey(Integer id);

    ItemStockDo selectByItemId(Integer itemId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Tue Jan 15 10:15:48 CST 2019
     */
    int updateByPrimaryKeySelective(ItemStockDo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Tue Jan 15 10:15:48 CST 2019
     */
    int updateByPrimaryKey(ItemStockDo record);

    /**
     * 商品库存扣减
     * @param itemId 商品id
     * @param amount 扣减的库存
     * @return
     */
    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);

}