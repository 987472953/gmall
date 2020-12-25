package com.dyqking.gmall.manage.mapper;

import com.dyqking.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    /**
     * 根据skuId spuId 查询商品销售属性
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String skuId, String spuId);

    /**
     * 根据spuId查询商品销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> spuSaleAttrList(String spuId);
}
