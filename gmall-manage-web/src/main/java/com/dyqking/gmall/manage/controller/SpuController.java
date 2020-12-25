package com.dyqking.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.*;
import com.dyqking.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SpuController {

    @Reference
    private ManageService manageService;

    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(SpuInfo spuInfo){
        return manageService.getSpuInfoList(spuInfo);
    }


    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        Boolean flag = manageService.saveSpuInfo(spuInfo);
        if(flag){
            return "OK";
        }else{
            return "false";
        }
    }

    //spuImageList?spuId=67
    @GetMapping("spuImageList")
    public List<SpuImage> getSpuImageList(SpuImage spuImage){
        return manageService.getSpuImageList(spuImage);
    }

    //spuSaleAttrList?spuId=67
    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        return manageService.getSpuSaleAttrList(spuId);
    }

    @DeleteMapping("deleteAttrInfo")
    public String deleteAttrInfoById(String attrId){
        Boolean flag = manageService.deleteAttrInfoById(attrId);
        if(flag)return "true";
        else return "false";
    }

    @GetMapping("spuInfo")
    public SpuInfo getSpuInfo(String spuId){
        return manageService.getSpuInfo(spuId);
    }
}
