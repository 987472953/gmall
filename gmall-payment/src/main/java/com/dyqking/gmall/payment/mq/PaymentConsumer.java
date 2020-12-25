package com.dyqking.gmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.PaymentInfo;
import com.dyqking.gmall.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.awt.font.OpenType;

@Component
public class PaymentConsumer {
    @Reference
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        //获得消息队列中的数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");

        //根据outTradeNo获得交易订单信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);

        //对交易状态进行检查
        boolean b = paymentService.checkPayment(paymentInfoQuery);
        System.out.println("检查结果：" + b);
        if(!b && checkCount!=0){
            System.out.println("检查次数：" + checkCount);
            paymentService.sendDelayPaymentResult(outTradeNo, delaySec, checkCount -1);
        }
    }
}
