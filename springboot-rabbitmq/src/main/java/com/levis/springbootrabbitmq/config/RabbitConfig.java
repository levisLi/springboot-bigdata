package com.levis.springbootrabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by le on 2019/5/5.
 */
@Slf4j
@Configuration
@EnableRabbit
public class RabbitConfig {


 /*业务交换机配置*/
  @Value("${spring.rabbitmq.exchange.business}")
  private String bizExchangeName;

  @Value("${spring.rabbitmq.queue.business}")
  private String bizQueueName;

  @Value("${spring.rabbitmq.routKey.business}")
  private String bizRoutKey;

  /*临时交换机*/
  @Value("${spring.rabbitmq.queue.temp}")
  private String tempQueueName;

  @Value("${spring.rabbitmq.routKey.temp}")
  private String tempRoutKey;

   /*消息处理失败队列*/
  @Value("${spring.rabbitmq.queue.fail}")
  private String failQueueName;

  @Value("${spring.rabbitmq.routKey.fail}")
  private String failRoutKey;

 /*死信队列配置*/
  @Value("${spring.rabbitmq.exchange.dlx}")
  private String dlxExchangeName;

  @Value("${spring.rabbitmq.queue.dlx}")
  private String dlxQueueName;

  @Value("${spring.rabbitmq.routKey.dlx}")
  private String dlxRoutKey;

    /*临时交换机*/
  @Value("${spring.rabbitmq.queue.manual}")
  private String manualQueueName;

  @Value("${spring.rabbitmq.routKey.manual}")
  private String manualRoutKey;

  @Autowired
  private ConnectionFactory connectionFactory;

  @Autowired
  private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

  /*消费者数量，默认10*/
  private static final int DEFAULT_CONCURRENT = 10;

  /*最大消费者数量*/
  private static final int MAX_DEFAULT_CONCURRENT=20;

  /*每个消费者获取最大投递数量 默认50*/
  private static final int DEFAULT_PREFETCH_COUNT = 10;

  /*=============================================================================*/

  /*声明队列*/
  /*定义一个死信队列*/
    @Bean
    public Queue dlxQueue(){
      return QueueBuilder.durable(dlxQueueName).build();
  }

    /*消息队列绑定死信交换机中*/
    @Bean
    public Queue tempQueue(){return QueueBuilder.durable(tempQueueName).withArgument("x-dead-letter-exchange",dlxExchangeName).build();}

    @Bean()
    public Queue manualQueue(){
        return QueueBuilder.durable(manualQueueName).build();
    }

    @Bean()
    public Queue bizQueue(){
        return QueueBuilder.durable(bizQueueName).build();
    }

    @Bean()
    public Queue failQueue(){
        return QueueBuilder.durable(failQueueName).build();
    }

    /*=============================================================================*/

    /*声明交互机*/
    @Bean(name = "bizExchange")
    public Exchange bizExchange(){
     return ExchangeBuilder.directExchange(bizExchangeName).durable(true).build();
    }

    @Bean
    public Exchange dlxExchange(){
    return ExchangeBuilder.topicExchange(dlxExchangeName).durable(true).build();
  }

    /*=============================================================================*/
    /*绑定交换机*/
    @Bean
    public Binding tempBind(){
        return BindingBuilder.bind(tempQueue()).to(bizExchange()).with(tempRoutKey).noargs();
    }



    @Bean
    public Binding bizBind(){
        return BindingBuilder.bind(bizQueue()).to(bizExchange()).with(bizRoutKey).noargs();
    }

    @Bean
    public Binding failBind(){
        return BindingBuilder.bind(failQueue()).to(bizExchange()).with(failRoutKey).noargs();
    }

    /*死信交换机绑定*/
    @Bean
    public Binding dlxBind(){ return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(dlxRoutKey).noargs();}

    /*=============================================================================*/

 /* @Bean
  public Queue DirectQueue(){
    return QueueBuilder.durable(queueName).withArgument("x-message-ttl","10000").build();
  }*/




    @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
    RabbitAdmin rabbitAdmin=new RabbitAdmin(connectionFactory);
    rabbitAdmin.setAutoStartup(true);
    return  rabbitAdmin;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
    RabbitTemplate rabbitTemplate=new RabbitTemplate(connectionFactory);
    rabbitTemplate.setConfirmCallback(confirmCallback);
    rabbitTemplate.setReturnCallback(returnCallback);
    return  rabbitTemplate;
  }


  final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
      log.info("correlationData : {}",correlationData);
      if(!ack){
        log.error("消息没有确认{}",cause);
      }
    }
  };

  final RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
    @Override
    public void returnedMessage(org.springframework.amqp.core.Message message, int repayCode, String relpyText, String exchange, String routingKey) {
      log.info("交换机：{},路由key{},错误原因{}",exchange,routingKey,relpyText);
    }
  };

  /*多消费着*/
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        /*消息格式转化*/
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //消息ack机制无
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        //每一个消费者当次获取的消息数量
        factory.setPrefetchCount(DEFAULT_PREFETCH_COUNT);
        //设置消费者的并发数量
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT);
        /*最大并发数量*/
        factory.setMaxConcurrentConsumers(MAX_DEFAULT_CONCURRENT);
        factoryConfigurer.configure(factory,connectionFactory);
        return factory;
    }

    /**
     * 单一消费者
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factoryConfigurer.configure(factory,connectionFactory);
        return factory;
    }

}
