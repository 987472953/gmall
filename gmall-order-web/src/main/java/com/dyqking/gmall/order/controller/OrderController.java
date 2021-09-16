package com.dyqking.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.bean.*;
import com.dyqking.gmall.bean.enums.OrderStatus;
import com.dyqking.gmall.bean.enums.ProcessStatus;
import com.dyqking.gmall.common.util.HttpClientUtil;
import com.dyqking.gmall.config.LoginRequire;
import com.dyqking.gmall.service.CartService;
import com.dyqking.gmall.service.ItemService;
import com.dyqking.gmall.service.OrderService;
import com.dyqking.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    //@Autowired
    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private ItemService itemService;

    @Reference
    private OrderService orderService;

    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList", userAddressList);

        //获得选中的商品
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);

        // 订单信息集合 将选中商品信息变为orderDetail
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());

            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailList", orderDetailList);

        //总价格
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());

        //防止重复提交
        String tradeCode = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo", tradeCode);
        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");

        //防止重复提交
        String tradeNo = request.getParameter("tradeNo");
        boolean result = orderService.checkTradeCode(userId, tradeNo);
        if (!result) {
            request.setAttribute("errMsg", "该页面已失效，请重新结算!");
            return "tradeFail";
        }
        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);


        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean checkStock = checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!checkStock) {
                request.setAttribute("errMsg", "商品：" + orderDetail.getSkuName() + "库存不足！");
                return "tradeFail";
            }
            SkuInfo skuInfo = itemService.getSkuInfo(orderDetail.getSkuId());
            int i = orderDetail.getOrderPrice().compareTo(new BigDecimal(skuInfo.getPrice()));
            if (i != 0) {
                request.setAttribute("errMsg", "商品：" + orderDetail.getSkuName() + "价格错误！");
                //更新商品实时价格
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
        }

        // 保存
        String orderId = orderService.saveOrder(orderInfo);

        orderService.delTradeNo(userId);


        return "redirect://payment.gmall.com/index?orderId=" + orderId;
    }

    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");

        //拆单 返回子订单详情
        List<OrderInfo>  subOrderInfoList = orderService.splitOrder(orderId, wareSkuMap);
        ArrayList<Map> mapArrayList = new ArrayList<>();
        for (OrderInfo orderInfo : subOrderInfoList) {
            //将子订单制造为库存服务传入参数
            Map map = orderService.initWareOrder(orderInfo);
            mapArrayList.add(map);
        }
        return JSON.toJSONString(mapArrayList);
    }

    private boolean checkStock(String skuId, Integer skuNum) {

        //http://www.gware.com/hasStock?skuId=?&skuNum=?
        //返回1有库存， 返回0没有库存
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

}
