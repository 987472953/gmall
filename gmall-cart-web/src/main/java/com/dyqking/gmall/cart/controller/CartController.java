package com.dyqking.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.CartInfo;
import com.dyqking.gmall.bean.SkuInfo;
import com.dyqking.gmall.config.LoginRequire;
import com.dyqking.gmall.service.CartService;
import com.dyqking.gmall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ItemService itemService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {

        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");

        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            // 已登录添加购物车
            cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        } else {
            // 未登录添加购物车
            // 说明用户没有登录没有登录放到 cookie 中
            cartCookieHandler.addToCart(request, response, skuId, null, Integer.parseInt(skuNum));
        }
        // 取得sku信息对象
        SkuInfo skuInfo = itemService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);

        return "success";
    }

    @LoginRequire(autoRedirect = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response) {

        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartInfoList = null;
        if (userId != null) {
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
            if (cartListFromCookie != null && cartListFromCookie.size() > 0) {
                // 合并缓存
                cartInfoList = cartService.mergeToCartList(cartListFromCookie, userId);
                //删除缓存中数据
                cartCookieHandler.deleteCartCookie(request, response);
            } else {
                cartInfoList = cartService.getCartList(userId);
            }
        } else {
            cartInfoList = cartCookieHandler.getCartList(request);
        }

        request.setAttribute("cartInfoList", cartInfoList);
        return "cartList";
    }

    @LoginRequire(autoRedirect = false)
    @RequestMapping("checkCart")
    public void checkCart(HttpServletRequest request, HttpServletResponse response) {
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            cartService.checkCart(skuId, isChecked, userId);
        } else {
            cartCookieHandler.checkCart(request, response, skuId, isChecked);
        }
    }

    @LoginRequire
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, HttpServletResponse response) {

        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);

        if (cartListCK != null && cartListCK.size() > 0) {
            cartService.mergeToCartList(cartListCK, userId);
            cartCookieHandler.deleteCartCookie(request, response);
        }
        return "redirect://order.gmall.com/trade";
    }
}
