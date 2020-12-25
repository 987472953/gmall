package com.atguigu.gware.controller;

import com.atguigu.gware.config.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GwareManageApplicationTests {

	@Autowired
	ActiveMQUtil activeMQUtil;

	@Test
	public void contextLoads() throws JMSException {

		Connection conn = activeMQUtil.getConn();
		conn.start();
		Session session = conn.createSession(true, Session.SESSION_TRANSACTED);

		Queue queue = session.createQueue("水电费加上了");
		MessageProducer producer = session.createProducer(queue);

		ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
		activeMQTextMessage.setText("sdfaf");
		producer.send(activeMQTextMessage);
		session.commit();

		session.close();
		producer.close();
		conn.close();
	}
	@Test
	public void asdf() throws JMSException {

		Connection conn = activeMQUtil.getConn();
		conn.start();
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue queue = session.createQueue("水电费加上了");
		MessageConsumer consumer = session.createConsumer(queue);

		consumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				if(message instanceof TextMessage){
					try {
						String text = ((TextMessage) message).getText();
						System.out.println(text);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Test
	@JmsListener(destination = "ORDER_RESULT_QUEUE",containerFactory = "jmsQueueListener")
	public void aaa() {
		System.out.println("2safdasg");
	}

}
