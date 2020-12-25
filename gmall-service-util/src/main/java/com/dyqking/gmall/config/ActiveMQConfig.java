package com.dyqking.gmall.config;

import com.dyqking.gmall.config.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Session;

@Configuration
public class ActiveMQConfig {

    @Value("${spring.activemq.broker-url:disabled}")
    String brokerURL;

    @Value("${activemq.listener.enable:disabled}")
    String listenerEnable;

    @Value("${spring.activemq.user:disabled}")
    String user;

    @Value("${spring.activemq.password:disabled}")
    String password;

    // 发送队列
    @Bean
    public ActiveMQUtil getActiveMQUtil() {
        if ("disabled".equals(brokerURL)) {
            return null;
        }

        ActiveMQUtil activeMQUtil = new ActiveMQUtil();
        if (user == null || password == null) {
            activeMQUtil.init(null, null, brokerURL);
        } else {
            activeMQUtil.init(user, password, brokerURL);
        }
        return activeMQUtil;
    }

    @Bean(name = "jmsQueueListener")
    public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {

        if ("disabled".equals(listenerEnable)) {
            return null;
        }
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory);
        // 设置事务
        factory.setSessionTransacted(false);
        // 手动签收
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        // 设置并发数
        factory.setConcurrency("5");
        // 重连间隔时间
        factory.setRecoveryInterval(5000L);

        return factory;
    }

    // 接收消息
    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        if (user == null || password == null) {
            return new ActiveMQConnectionFactory(brokerURL);
        } else {
            return new ActiveMQConnectionFactory(user, password, brokerURL);
        }
    }
}
