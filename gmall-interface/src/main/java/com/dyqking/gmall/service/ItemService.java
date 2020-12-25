package com.dyqking.gmall.service;

import com.dyqking.gmall.bean.SkuInfo;
import com.dyqking.gmall.bean.SkuSaleAttrValue;
import com.dyqking.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface ItemService {

    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuId 与 spuId 查询 商品销售属性
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 根据spuId 查询该分类下所有的 可用Sku属性
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValue(String spuId);
}
