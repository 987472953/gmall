package com.dyqking.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.SkuInfo;
import com.dyqking.gmall.bean.SkuLsInfo;
import com.dyqking.gmall.service.ItemService;
import com.dyqking.gmall.service.ListService;
import com.dyqking.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@CrossOrigin
public class SkuController {

    @Reference
    private ManageService manageService;

    @Reference
    private ItemService itemService;

    @Reference
    private ListService listService;


    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo) {

        Boolean flag = manageService.saveSkuInfo(skuInfo);
        if (flag) return "OK";
        else return "FALSE";
    }

    @RequestMapping("onSale")
    public String onSale(String skuId) {


        SkuInfo skuInfo = itemService.getSkuInfo(skuId);

        SkuLsInfo skuLsInfo = new SkuLsInfo();

        BeanUtils.copyProperties(skuInfo, skuLsInfo);
        BigDecimal bigDecimal = new BigDecimal(skuInfo.getPrice());
        skuLsInfo.setPrice(bigDecimal);
        listService.saveSkuInfo(skuLsInfo);

        return "ok";
    }
}
