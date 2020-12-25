package com.dyqking.gmall.service;

import com.dyqking.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    /**
     * 根据outTradeNo查询paymentinfo
     * @param paymentInfo
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据 out_trade_no 进行状态更新
     * @param out_trade_no
     * @param paymentInfoUp
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUp);

    /**
     * 退款
     * @param orderId
     * @return
     */
    boolean refund(String orderId);

    /**
     * 微信支付订单
     * @param orderId
     * @param s
     * @return
     */
    Map createNative(String orderId, String s);

    /**
     * 将订单结果发送给消息队列
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    /**
     * 查询订单是否交易成功
     * @param paymentInfo
     * @return bool
     */
    boolean checkPayment(PaymentInfo paymentInfo);

    /**
     * 延迟队列检查交易状态
     * @param outTradeNo
     * @param delaySec
     * @param checkCount
     */
    void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount);

    /**
     * 关闭付款账单
     * @param orderId
     */
    void closePayment(String orderId);
}
