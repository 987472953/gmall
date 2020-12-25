package com.dyqking.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.*;
import com.dyqking.gmall.service.ListService;
import com.dyqking.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
//   @ResponseBody
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request) {

        skuLsParams.setPageSize(1);
        SkuLsResult skuLsResult = listService.search(skuLsParams);
//       return JSON.toJSONString(search);
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        request.setAttribute("skuLsInfoList", skuLsInfoList);


        //根据skuLsParams中的参数进行url拼接， 用来实现搜索条件的添加和删除
        String urlParams = makeUrlParam(skuLsParams, null);
        request.setAttribute("urlParams", urlParams);

        ArrayList<BaseAttrValue> baseAttrValueList = new ArrayList<>();
        //根据valueIdList 获得商品属性 和 商品属性值
        List<BaseAttrInfo> attrList = manageService.getAttrList(skuLsResult.getAttrValueIdList());
        //根据已选择的商品属性值，让已选择的商品属性值不显示
        //hideCheckedAttrValue(attrList, skuLsParams);
        // itco
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                    for (String valueId : skuLsParams.getValueId()) {
                        //选中的属性值 和 查询结果的属性值
                        if (valueId.equals(baseAttrValue.getId())) {
                            iterator.remove();
                            //面包屑
                            BaseAttrValue baseAttrValued = new BaseAttrValue();
                            baseAttrValued.setValueName(baseAttrInfo.getAttrName() + " : " + baseAttrValue.getValueName());
                            String newParam = makeUrlParam(skuLsParams, baseAttrValue.getId());
                            baseAttrValued.setUrlParam(newParam);
                            baseAttrValueList.add(baseAttrValued);
                        }
                    }
                }
            }
        }
        request.setAttribute("baseAttrValueList", baseAttrValueList);
        request.setAttribute("keyword", skuLsParams.getKeyword());

        request.setAttribute("attrList", attrList);


        //分页
        request.setAttribute("totalPages", skuLsResult.getTotalPages());
        request.setAttribute("pageNo", skuLsParams.getPageNo());
        request.setAttribute("urlParam", makeUrlParam(skuLsParams,null));
        return "list";
    }

    private void hideCheckedAttrValue(List<BaseAttrInfo> attrList, SkuLsParams skuLsParams) {
        String valueIds = "";
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            String[] valueId = skuLsParams.getValueId();
            valueIds = StringUtils.join(valueId, ",");
        }
        // base_attr_info
        if (attrList != null && attrList.size() > 0) {
            for (int i = 0; i < attrList.size(); i++) {
                BaseAttrInfo baseAttrInfo = attrList.get(i);
                // base_attr_value
                if (baseAttrInfo.getAttrValueList() != null) {
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    for (int j = 0; j < attrValueList.size(); j++) {
                        BaseAttrValue baseAttrValue = attrValueList.get(j);
                        if (valueIds.contains(baseAttrValue.getId())) {
                            attrValueList.remove(j);
                        }
                    }
                }
            }
        }

    }

    private String makeUrlParam(SkuLsParams skuLsParams, String removeValueId) {
        String urlParam = "";
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            if (urlParam.length() > 0) urlParam += "&";
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                if (valueId.equals(removeValueId)) continue;
                if (urlParam.length() > 0) {
                    urlParam += "&";
                }
                urlParam += "valueId=" + valueId;
            }
        }
        return urlParam;
    }
}
