package com.dyqking.gmall.service;

import com.dyqking.gmall.bean.SkuLsInfo;
import com.dyqking.gmall.bean.SkuLsParams;
import com.dyqking.gmall.bean.SkuLsResult;

public interface ListService {

    void saveSkuInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult search(SkuLsParams skuLsParams);
}
