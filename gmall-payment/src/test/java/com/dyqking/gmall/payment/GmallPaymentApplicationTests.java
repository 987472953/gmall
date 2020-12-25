package com.dyqking.gmall.payment;

import com.dyqking.gmall.config.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Test
    public void tesss() throws JMSException {
        Connection connection = activeMQUtil.getConnection();

        connection.start();
        //第一个参数 是否开启事务
        //第二个参数 开启/关闭事务的相应配置参数
//        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue dyqking = session.createQueue("qwert");
        MessageProducer producer = session.createProducer(dyqking);

        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("activeMQ提供者测试");
        producer.send(activeMQTextMessage);

        //如果开启了事务必须提交
        session.commit();

        producer.close();
        session.close();
        connection.close();
    }

}
