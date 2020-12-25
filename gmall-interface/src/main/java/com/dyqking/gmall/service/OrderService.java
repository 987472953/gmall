package com.dyqking.gmall.service;

import com.dyqking.gmall.bean.OrderInfo;
import com.dyqking.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    /**
     * 保存订单信息
     * @param orderInfo
     * @return
     */
    String saveOrder(OrderInfo orderInfo);

    /**
     * 添加tradeNo到redis
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 判断是否存在该订单
     * @param userId
     * @param tradeNo
     * @return
     */
    boolean checkTradeCode(String userId, String tradeNo);

    /**
     * 删除tradeNo
     * @param userId
     */
    void delTradeNo(String userId);

    /**
     * 获得订单详情
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);

    /***
     * 更新订单状态
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(String orderId, ProcessStatus processStatus);


    /**
     *
     * @param orderId
     */
    void sendOrderStatus(String orderId);

    /**
     * 获得过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 处理过期订单
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);

    Map initWareOrder(OrderInfo orderInfo);

    /**
     * 根据wareSkuMap进行拆单
     * @param orderId
     * @param wareSkuMap 存放的是订单详情中订单的创库信息
     * @return
     */
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);
}
