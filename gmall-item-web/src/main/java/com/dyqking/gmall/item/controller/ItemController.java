package com.dyqking.gmall.item.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.bean.SkuInfo;
import com.dyqking.gmall.bean.SkuSaleAttrValue;
import com.dyqking.gmall.bean.SpuSaleAttr;
import com.dyqking.gmall.config.LoginRequie;
import com.dyqking.gmall.service.ItemService;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    private ItemService itemService;

    @RequestMapping("{skuId}.html")
    public String getSkuInfo(@PathVariable String skuId, HttpServletRequest request) {

        SkuInfo skuInfo = itemService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);

        if(skuInfo!=null){
            List<SpuSaleAttr> spuSaleAttrList = itemService.getSpuSaleAttrListCheckBySku(skuInfo);
            request.setAttribute("spuSaleAttrList", spuSaleAttrList);
        }

        //拼接可跳转的sku集合
        if (skuInfo != null && skuInfo.getSpuId() != null && skuInfo.getSpuId().length() > 0) {
            List<SkuSaleAttrValue> skuSaleAttrValueList = itemService.getSkuSaleAttrValue(skuInfo.getSpuId());
            String key = "";
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
                SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
                if (key.length() != 0) {
                    key = key + "|";
                }
                key = key + skuSaleAttrValue.getSaleAttrValueId();

                if ((i + 1) == skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i + 1).getSkuId())) {
                    map.put(key, skuSaleAttrValue.getSkuId());
                    key = "";
                }

            }
            //把map变成json串
            String valuesSkuJson = JSON.toJSONString(map);

            request.setAttribute("valuesSkuJson", valuesSkuJson);
        }

        return "item";
    }


}
