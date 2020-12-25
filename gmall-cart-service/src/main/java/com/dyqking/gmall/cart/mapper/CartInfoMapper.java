package com.dyqking.gmall.cart.mapper;

import com.dyqking.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface CartInfoMapper extends BaseMapper<CartInfo> {

    /**
     * 根据用户id关联cart_info 和 sku_info 表
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
