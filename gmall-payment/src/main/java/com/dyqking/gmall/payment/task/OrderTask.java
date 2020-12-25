package com.dyqking.gmall.payment.task;


import com.alibaba.dubbo.config.annotation.Reference;
import com.dyqking.gmall.bean.OrderInfo;
import com.dyqking.gmall.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
public class OrderTask {

    @Reference
    private OrderService orderService;

    //每分钟第五秒执行
    @Scheduled(cron = "5 * * * * ?")
    public void test1() {
        System.out.println("Thread ======" + Thread.currentThread().getName());
    }

    //每隔6秒执行一次
    @Scheduled(cron = "0/6 * * * * ?")
    public void test2() {
        System.out.println("Thread1 ======" + Thread.currentThread().getName());
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder() {
        System.out.println("开始执行过期订单");
        long nowTime = System.currentTimeMillis(); // 返回当前时间，以毫秒为单位
        //获得过期订单
        List<OrderInfo> expiredOrderList = orderService.getExpiredOrderList();
        for (OrderInfo orderInfo : expiredOrderList) {
            //处理未完成订单 将订单和付款账单状态设为close
            orderService.execExpiredOrder(orderInfo);
        }
        long costtime = System.currentTimeMillis() - nowTime;
        System.out.println("一共处理" + expiredOrderList.size() + "个订单 共消耗" + costtime + "毫秒");
    }
}
