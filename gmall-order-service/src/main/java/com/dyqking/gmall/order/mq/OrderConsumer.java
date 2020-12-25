package com.dyqking.gmall.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.enums.ProcessStatus;
import com.dyqking.gmall.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;

    @JmsListener(destination = "PAYMENT_RESULT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) {
        String orderId = null;
        String result = null;
        try {
            orderId = mapMessage.getString("orderId");
            result = mapMessage.getString("result");
        } catch (JMSException e) {
            e.printStackTrace();
        }
        if ("success".equals(result)) {
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            //发送到 减库存的消息队列消费端接口
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
        }else {
            orderService.updateOrderStatus(orderId,ProcessStatus.UNPAID);
        }
    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        //从库存系统中返回的成功与否消息队内

        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");

        if ("DEDUCTED".equals(status)) {
            orderService.updateOrderStatus(orderId, ProcessStatus.WAITING_DELEVER);
        } else {
            orderService.updateOrderStatus(orderId, ProcessStatus.STOCK_EXCEPTION);
        }


    }
}
