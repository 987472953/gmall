package com.dyqking.gmall.service;

import com.dyqking.gmall.bean.*;

import java.util.List;

public interface ManageService {

    /**
     * 获得一级分类
     * @return
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 获得二级分类
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 获得三级分类
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类查询属性标签
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 根据新的BaseAttrInfo进行插入
     * @param baseAttrInfo
     */
    void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo);


    /**
     * 根据baseAttrInfo 的id查询它所有的 BaseAttrValue
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 根据attrId查询baseAttrInfo
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(String attrId);

    /**
     * 根据三级分类id查询spuInfo
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    /**
     * 获得基本销售属性
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spu详细信息
     * @param spuInfo
     * @return
     */
    Boolean saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId获得SpuImageList
     * @param SpuImage
     * @return
     */
    List<SpuImage> getSpuImageList(SpuImage SpuImage);

    /**
     * 根据spuId获得saleAttrList
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 保存sku详细信息，涉及sku详细表，skuImage skuSaleAttrValue skuAttrValue
     * @param skuInfo
     * @return
     */
    Boolean saveSkuInfo(SkuInfo skuInfo);

    /**
     * 删除BaseAttrInfo和他的值BaseAttrValue
     * @param attrId
     * @return
     */
    Boolean deleteAttrInfoById(String attrId);


    /**
     * 获得单个spuInfo
     * @param spuId
     * @return
     */
    SpuInfo getSpuInfo(String spuId);

    /**
     * 根据base_attr_value的Id 集合查询 商品分类信息 和 商品分类属性值
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
