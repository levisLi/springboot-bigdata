package com.levis.springbootrabbitmq.config;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

/**
 * Created by le on 2019/6/4.
 */
@Configuration
public class ManualConfig {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("${spring.rabbitmq.queue.manual}")
    private String manualQueueName;

    @Value("${spring.rabbitmq.routKey.manual}")
    private String manualRoutKey;

    /*业务交换机配置*/
    @Value("${spring.rabbitmq.exchange.business}")
    private String bizExchangeName;

    @Bean
    @Primary
    public Channel channel() throws IOException {
        Connection connection=connectionFactory.createConnection();
        Channel channel=connection.createChannel(false);
        //声明通道
        // channel.queueDeclare(manualQueueName,true,false,false,null);
        channel.queueBind(manualQueueName,bizExchangeName,manualRoutKey);
        return channel;
    }

}
