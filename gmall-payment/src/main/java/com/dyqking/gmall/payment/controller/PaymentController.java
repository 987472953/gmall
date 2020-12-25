package com.dyqking.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.dyqking.gmall.bean.OrderInfo;
import com.dyqking.gmall.bean.PaymentInfo;
import com.dyqking.gmall.bean.enums.PaymentStatus;
import com.dyqking.gmall.payment.config.AlipayConfig;
import com.dyqking.gmall.payment.mapper.PaymentInfoMapper;
import com.dyqking.gmall.service.OrderService;
import com.dyqking.gmall.service.PaymentService;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @Autowired // dubbo是基于spring的所以它的Service注解也能将其添加到容器中
    private PaymentService paymentService;

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private AlipayClient alipayClient;

    @RequestMapping("index")
    public String index(HttpServletRequest request, Model model) {

        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        model.addAttribute("orderId", orderId);
        model.addAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";
    }

    @RequestMapping(value = "/alipay/submit", method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response) {

        //获取订单
        String orderId = request.getParameter("orderId");
        //根据orderId获取订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 保存支付信息   支付状态
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("-----");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID); // 未支付

        //保存订单信息
        paymentInfoMapper.insertSelective(paymentInfo);

        // 支付宝参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        // 声明一个Map
        Map<String, Object> bizContnetMap = new HashMap<>();
        bizContnetMap.put("out_trade_no", paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject", paymentInfo.getSubject());
        bizContnetMap.put("total_amount", paymentInfo.getTotalAmount());
        // 将map变成json
        String Json = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(Json);
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //https://unitradeprod.alipaydev.com/acq/cashierReturn.htm?sign=K1iSL19gsMALQ83qNg5ibRMR%252BTDOIp%252FJaxPU%252BKYsvV2zeBKPEr1com7LIm4vBs%252BDKKZcb9eWauiL&outTradeNo=ATGUIGU1608612407453597&pid=2088621955169132&type=1
        response.setContentType("text/html;charset=UTF-8");
        // 对交易状态的检查  15秒执行一次，总共需要执行3次。
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(), 15, 3);

        return form;
    }

    @RequestMapping("alipay/callback/return")
    public String callbackReturn() {
        return "redirect:" + AlipayConfig.return_order_url;
    }

    @RequestMapping("alipay/callback/notify")
    @ResponseBody
    public String callbacknotify(@RequestBody Map<String, String> paramMap, HttpServletRequest request) {

        //Map<String,String> paramsMap = ...//将异步通知中收到的所有参数都存放到map中
        boolean flag = false;
        try {
            flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        String trade_status = paramMap.get("trade_status");
        String out_trade_no = paramMap.get("out_trade_no");
        if (!("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status))) return "fail";
        if (flag) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentInfo = paymentService.getPaymentInfo(paymentInfo);
            if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID
                    || paymentInfo.getPaymentStatus() == PaymentStatus.ClOSED) {
                return "fail";
            }
            //更新订单信息
            PaymentInfo paymentInfoUp = new PaymentInfo();
            paymentInfoUp.setPaymentStatus(PaymentStatus.PAID);
            paymentInfoUp.setCallbackTime(new Date());
            paymentInfoUp.setCallbackContent(paramMap.toString());
            paymentService.updatePaymentInfo(out_trade_no, paymentInfoUp);

            //发送消息队列给订单服务
            paymentService.sendPaymentResult(paymentInfo, "success");
            return "success";

        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "fail";
        }
    }

    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId) {

        boolean flag = paymentService.refund(orderId);
        return flag + "";
    }

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(String orderId) {
        // 做一个判断：支付日志中的订单支付状态 如果是已支付，则不生成二维码直接重定向到消息提示页面！
        // 调用服务层数据
        // 第一个参数是订单Id ，第二个参数是多少钱，单位是分
        if (orderId == null || orderId.length() <= 0) {
            orderId = UUID.randomUUID().toString().replaceAll("-", "");
        }
        Map map = paymentService.createNative(orderId, "1");
        System.out.println(map.get("code_url"));
        // data = map
        return map;
    }

    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo, @RequestParam("result") String result) {
        paymentService.sendPaymentResult(paymentInfo, result);
        return "sent payment result";
    }

    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        boolean flag = paymentService.checkPayment(paymentInfo);
        return flag + "";
    }
}
