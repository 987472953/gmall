package com.dyqking.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class SkuInfo implements Serializable {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String spuId;

    @Column
    private String price;

    @Column
    private String skuName;

    @Column
    private String skuDesc;

    @Column
    private String weight;

    @Column
    private String tmId;

    @Column
    private String catalog3Id;

    @Column
    private String skuDefaultImg;

    @Transient
    private List<SkuImage> skuImageList;

    @Transient
    private List<SkuAttrValue> skuAttrValueList;

    @Transient
    private List<SkuSaleAttrValue> skuSaleAttrValueList;

}
