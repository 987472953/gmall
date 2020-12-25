package com.dyqking.gmall;

import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.bean.SkuInfo;
import org.junit.Test;

public class test {

    @Test
    public void  test(){
        SkuInfo skuInfo = JSON.parseObject(null, SkuInfo.class);
        System.out.println(skuInfo);
    }
}
