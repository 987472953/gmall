package com.dyqking.gmall.service;

import com.dyqking.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 已登录用户添加购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(String skuId, String userId, Integer skuNum);

    /**
     * 根据skuId查询购物车的内容
     * @param skuId
     * @return
     */
    List<CartInfo> getCartList(String skuId);

    /**
     * cookie中有数据时进行合并缓存
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    /**
     * 修改购物车中的选择信息
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 根据skuId查询购物车已选择的内容
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

    /**
     * 更新商品实时价格
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);
}
