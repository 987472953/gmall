package com.dyqking.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.bean.CartInfo;
import com.dyqking.gmall.bean.SkuInfo;
import com.dyqking.gmall.config.CookieUtil;
import com.dyqking.gmall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE = 7 * 24 * 3600;

    @Reference
    private ItemService itemService;


    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = new ArrayList<>();
        SkuInfo skuInfo = null;
        boolean ifExist = false;
        if (cartJson != null) {
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if (skuId.equals(cartInfo.getSkuId())) {
                    cartInfo.setSkuNum(skuNum + cartInfo.getSkuNum());
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist = true;
                }
            }
        }
        if (!ifExist) {
            //把商品信息取出来，新增到购物车
            skuInfo = itemService.getSkuInfo(skuId);

            CartInfo cartInfo = new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(new BigDecimal(skuInfo.getPrice()));
            cartInfo.setSkuPrice(new BigDecimal(skuInfo.getPrice()));
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);


            cartInfoList.add(cartInfo);
        }

        // 将 cartInfoList 添加到cookie
        String newCartJSON = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request, response, cookieCartName, newCartJSON, COOKIE_CART_MAXAGE, true);
        request.setAttribute("skuInfo", skuInfo);
    }

    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cartStrs = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartStrs, CartInfo.class);
        return cartInfoList;
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        List<CartInfo> cartInfoList = getCartList(request); // 获得cookie中购物车
        for (CartInfo cartInfo : cartInfoList) {
            if (cartInfo.getSkuId().equals(skuId)) {
                cartInfo.setIsChecked(isChecked);
            }
        }
        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);
    }
}
