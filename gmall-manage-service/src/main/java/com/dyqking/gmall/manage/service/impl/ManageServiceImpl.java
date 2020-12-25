package com.dyqking.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.dyqking.gmall.bean.*;
import com.dyqking.gmall.manage.mapper.*;
import com.dyqking.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import sun.nio.cs.ext.SJIS;

import java.beans.Transient;
import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        for (BaseAttrInfo attrInfo : baseAttrInfoList) {
            baseAttrValue.setAttrId(attrInfo.getId());
            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
            if (baseAttrValueList != null) {
                attrInfo.setAttrValueList(baseAttrValueList);
            }
        }

        return baseAttrInfoList;
    }

    @Transactional
    @Override
    public void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        //根据attrid进行 原数据的批量删除
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //添加新数据
        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            for (BaseAttrValue baseAttrValue : baseAttrInfo.getAttrValueList()) {
                //防止插入为空串
                baseAttrValue.setId(null);

                baseAttrValue.setAttrId(baseAttrInfo.getId());  // 主外键关系
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        if (baseAttrInfo == null) return new BaseAttrInfo();

        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> list = baseAttrValueMapper.select(baseAttrValue);

        baseAttrInfo.setAttrValueList(list);
        return baseAttrInfo;

    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public Boolean saveSpuInfo(SpuInfo spuInfo) {
        //判断是更新还是新增
        if (spuInfo.getId() == null || spuInfo.getId().length() == 0) {
            spuInfo.setId(null); // 防止空字符串
            spuInfoMapper.insertSelective(spuInfo);
        } else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }
        /*
        对图片进行操作
         */
        if (spuInfo.getSpuImageList() != null) {

            SpuImage deleteImage = new SpuImage();
            deleteImage.setSpuId(spuInfo.getId());
            spuImageMapper.delete(deleteImage);

            List<SpuImage> spuImageList = spuInfo.getSpuImageList();
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        /*
        对销售属性操作  注意不能将删除放入循环中，应该在循环开始时先进行删除
         */
        if (spuInfo.getSpuSaleAttrList() != null) {

            SpuSaleAttr spuSaleAttrDelete = new SpuSaleAttr();
            spuSaleAttrDelete.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.delete(spuSaleAttrDelete);

            SpuSaleAttrValue spuSaleAttrValueDelete = new SpuSaleAttrValue();
            spuSaleAttrDelete.setSpuId(spuInfo.getId());
            spuSaleAttrValueMapper.delete(spuSaleAttrValueDelete);

            List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setId(null);
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                /*
                对销售属性值操作
                 */
                if (spuSaleAttr.getSpuSaleAttrValueList() != null) {

                    List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setId(null);
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }

            }
        }


        return true;
    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        /**
         * SELECT ssa.id, ssa.spu_id, ssa.sale_attr_id, ssa.sale_attr_name, ssav.sale_attr_value_name FROM spu_sale_attr ssa
         * INNER JOIN spu_sale_attr_value ssav
         * ON ssa.sale_attr_id = ssav.sale_attr_id
         * AND ssa.spu_id = ssav.spu_id
         * where ssa.spu_id = 67
         */
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.spuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    @Transactional
    public Boolean saveSkuInfo(SkuInfo skuInfo) {


        //判断新增或修改
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //先删除
        SkuImage skuImageDelete = new SkuImage();
        SkuAttrValue skuAttrValueDelete = new SkuAttrValue();
        SkuSaleAttrValue skuSaleAttrValueDelete = new SkuSaleAttrValue();

        skuImageDelete.setSkuId(skuInfo.getId());
        skuAttrValueDelete.setSkuId(skuInfo.getId());
        skuSaleAttrValueDelete.setSkuId(skuInfo.getId());

        skuImageMapper.delete(skuImageDelete);
        skuAttrValueMapper.delete(skuAttrValueDelete);
        skuSaleAttrValueMapper.delete(skuSaleAttrValueDelete);

        //新数据添加
        if (skuInfo.getSkuImageList() != null) {
            List<SkuImage> skuImageList = skuInfo.getSkuImageList();
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

        if (skuInfo.getSkuAttrValueList() != null) {
            List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        if (skuInfo.getSkuSaleAttrValueList() != null) {
            List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }
        return true;
    }

    @Transactional
    @Override
    public Boolean deleteAttrInfoById(String attrId) {

        int i = baseAttrInfoMapper.deleteByPrimaryKey(attrId);
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        int delete = baseAttrValueMapper.delete(baseAttrValue);

        return i > 0 || delete > 0;
    }

    @Override
    public SpuInfo getSpuInfo(String spuId) {
        SpuInfo spuInfo = spuInfoMapper.selectByPrimaryKey(spuId);
        if (spuInfo != null) {
            SpuImage spuImage = new SpuImage();
            spuImage.setSpuId(spuId);
            List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
            if (spuImageList != null && spuImageList.size() > 0) {
                spuInfo.setSpuImageList(spuImageList);
            }

            SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
            spuSaleAttr.setSpuId(spuId);
            List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.select(spuSaleAttr);
            if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
                for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                    SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
                    spuSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
                    spuSaleAttrValue.setSpuId(spuId);
                    List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
                    saleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
                }
                spuInfo.setSpuSaleAttrList(spuSaleAttrList);
            }
        }
        return spuInfo;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {

        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");

        return baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);

    }
}
