package com.dyqking.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class BaseAttrInfo implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY) //设置主键回显，用于插入
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;

    @Transient  //标注该字段不是数据库字段，业务需要
    private List<BaseAttrValue> attrValueList;

}
