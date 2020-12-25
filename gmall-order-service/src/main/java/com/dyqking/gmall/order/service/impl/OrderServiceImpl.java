package com.dyqking.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.dyqking.gmall.bean.OrderDetail;
import com.dyqking.gmall.bean.OrderInfo;
import com.dyqking.gmall.bean.enums.OrderStatus;
import com.dyqking.gmall.bean.enums.ProcessStatus;
import com.dyqking.gmall.config.ActiveMQUtil;
import com.dyqking.gmall.config.RedisUtil;
import com.dyqking.gmall.order.mapper.OrderDetailMapper;
import com.dyqking.gmall.order.mapper.OrderInfoMapper;
import com.dyqking.gmall.service.OrderService;
import com.dyqking.gmall.service.PaymentService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.beans.Transient;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {

        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        //使用默认时区和区域设置获取日历。通过该方法生成Calendar对象 (日历)
        Calendar instance = Calendar.getInstance();
        //按照日历的规则，给指定字段添加或减少时间量。
        instance.add(Calendar.DATE, 1);
        //返回一个Date表示此日历的时间
        orderInfo.setExpireTime(instance.getTime());
        // 生成第三方支付编号
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        // 插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:" + userId + ":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeKey, 60 * 10, tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:" + userId + ":tradeCode";
        String value = jedis.get(tradeKey);

        jedis.close();

        return tradeNo.equals(value);
    }

    @Override
    public void delTradeNo(String userId) {

        Jedis jedis = redisUtil.getJedis();
        String tradeKey = "user:" + userId + ":tradeCode";
        jedis.del(tradeKey);
        jedis.close();
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));

        return orderInfo;

    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfo.setProcessStatus(processStatus);

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);

    }

    @Override
    public void sendOrderStatus(String orderId) {

        Connection connection = activeMQUtil.getConnection();
        //获得需要使用的具体参数的json串
        String orderJson = initWareOrder(orderId);

        try {
            //及库存消费队列 提供端
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");

            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(orderJson);

            MessageProducer producer = session.createProducer(order_result_queue);
            producer.send(activeMQTextMessage);

            session.commit();

            session.close();
            producer.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<OrderInfo> getExpiredOrderList() {

        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("processStatus", OrderStatus.UNPAID)
                .andLessThan("expireTime", new Date());

        return orderInfoMapper.selectByExample(example);
    }

    // 处理未完成订单
    @Async
    @Override
    public void execExpiredOrder(OrderInfo orderInfo) {
        //订单状态
        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);
        //付款信息状态
        paymentService.closePayment(orderInfo.getId());

    }

    private String initWareOrder(String orderId) {
        //根据orderId获得orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);
    }

    public Map initWareOrder(OrderInfo orderInfo) {
        Map<String,Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        //仓库ID 拆单时使用
        map.put("wareId",orderInfo.getWareId());

        // 组合json
        List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details",detailList);
        return map;

    }

    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {

        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        //1、原始订单信息
        OrderInfo orderInfoOrigin  = getOrderInfo(orderId);
        //2、仓库信息
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        //3、拆单方案 按仓库拆分  [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        for (Map map : maps) {
            String wareId = (String) map.get("wareId");
            List<String> skuIds = (List<String>) map.get("skuIds");
            // 4 生成被拆分订单，从原始订单复制，新的订单号，父订单
            OrderInfo subOrderInfo = new OrderInfo();
            BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
            //5、设置子订单的仓库号，父订单号，主键自增
            subOrderInfo.setWareId(wareId);
            subOrderInfo.setParentOrderId(orderId);
            subOrderInfo.setId(null);

            //父订单商品详情
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            //子订单详情
            ArrayList<OrderDetail> subOrderDetails = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetailList) {
                //6、将父订单详情 遍历，根据map中的规则将sku加入到子订单详情 并保存
                for (String skuId : skuIds) {
                    if(orderDetail.getSkuId().equals(skuId)){
                        orderDetail.setId(null);
                        subOrderDetails.add(orderDetail);
                    }
                }
            }
            subOrderInfo.setOrderDetailList(subOrderDetails);
            subOrderInfo.sumTotalAmount(); //子订单总价

            //7、将子订单保存到数据库
            saveOrder(subOrderInfo);
            subOrderInfoList.add(subOrderInfo);
        }
        //8、更新父订单状态
        updateOrderStatus(orderId, ProcessStatus.SPLIT);
        return subOrderInfoList;
    }
}
