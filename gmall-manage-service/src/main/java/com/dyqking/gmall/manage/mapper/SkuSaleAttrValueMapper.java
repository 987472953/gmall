package com.dyqking.gmall.manage.mapper;

import com.dyqking.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    //根据spuId查询sku的属性
    List<SkuSaleAttrValue> selectSkuSaleAttrValue(String spuId);
}
