package com.levis.springbootrabbitmq.consumer;

import com.alibaba.fastjson.JSON;
import com.levis.springbootrabbitmq.bean.MessageBean;
import com.levis.springbootrabbitmq.constants.Constants;
import com.levis.springbootrabbitmq.constants.MessageStatusEnum;
import com.levis.springbootrabbitmq.messageConfirm.producer.IproducerService;
import com.levis.springbootrabbitmq.producer.RabbitmqSender;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Created by le on 2019/5/7.
 */
@Slf4j
@Component
public class RabbitConsumer {

    private static final int MAX_SEND_TIMES=3;

    @Autowired
    private RabbitmqSender rabbitmqSender;

    @Autowired
    private IproducerService iproducerService;

    @Value("${spring.rabbitmq.exchange.business}")
    private String bizExchange;

    @Value("${spring.rabbitmq.routKey.fail}")
    private String failRoutKey;

    @RabbitListener(queues = "${spring.rabbitmq.queue.business}",containerFactory = "singleListenerContainer")
    @RabbitHandler
    public void onOrderMessage(Message message , Channel channel, @Headers Map<String,Object> headers) throws IOException {
        System.out.println("=======================");
        // 限流处理：消息体大小不限制，每次限制消费一条，只作用于该Consumer层，不作用于Channel
        channel.basicQos(0,1,false);
        MessageProperties properties=message.getMessageProperties();
        String messageId=properties.getMessageId();
        Map<String , Object> Header=properties.getHeaders();
        System.out.println("headers: "+Header);
        String messageBody=new String(message.getBody(),"utf-8");
        System.out.println("消息体==="+messageBody);
        MessageBean messageBean = JSON.parseObject(messageBody, MessageBean.class);
        System.out.println("messageBean: "+ messageBean);
        Long deliveryTag=(Long)headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            /*处理业务逻辑*/
            Boolean handlerResult=false;
            if(handlerResult){
                System.out.println("==处理成功==");
                messageBean.setStatus(MessageStatusEnum.SUCCESS.getCode());
            }else{
                System.out.println("===待处理中===");
                messageBean.setStatus(MessageStatusEnum.WAITING_HANDLE.getCode());
            }
        }catch (Exception e){
            System.out.println("==处理失败==");
            messageBean.setStatus(MessageStatusEnum.FAILURE.getCode());
            log.error("消息处理失败{}",e.getMessage());
        }finally {
//            Constants.cache.put(messageId,JSON.toJSONString(messageBean));
            // 手工ACK,不批量ack，multiple:当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
            channel.basicAck(deliveryTag,false);
        }
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.dlx}")
    @RabbitHandler
    public void onMessage(Message message , Channel channel) throws IOException {
        System.out.println("=========死信队列消费==============");
        // 限流处理：消息体大小不限制，每次限制消费一条，只作用于该Consumer层，不作用于Channel
        channel.basicQos(0,1,false);
        MessageProperties properties=message.getMessageProperties();
        String messageId=properties.getMessageId();
        Long deliveryTag=properties.getDeliveryTag();
        Map<String , Object> Header=properties.getHeaders();
        System.out.println("headers: "+Header);
        String messageBody=new String(message.getBody(),"utf-8");
        System.out.println("消息体==="+messageBody);
        MessageBean messageBean =JSON.parseObject(messageBody, MessageBean.class);
        System.out.println("messageBean: "+ messageBean);
        Optional valueOptional= Optional.ofNullable(Constants.cache.getIfPresent(messageId));
        if(valueOptional.isPresent()){
            MessageBean bean=JSON.parseObject(valueOptional.get().toString(),MessageBean.class);
            if(null != bean){
                String status=bean.getStatus();
                System.out.println("消息Id【"+messageId+"】当前状态【"+status);
                if(status.equals(MessageStatusEnum.SUCCESS.getCode())){
                    System.out.println("消息Id【"+messageId+"】处理成功,即将移除");
                }else if(status.equals(MessageStatusEnum.WAITING_HANDLE.getCode())){
                    System.out.println("消息Id【"+messageId+"】还没有处理,需重新发送");
                    messageBean.setSendTimes(messageBean.getSendTimes()+1);
                    iproducerService.sendMsg(messageBean);
                    iproducerService.sendDlxMsg(messageBean);
                }else {
                    System.out.println("消息Id【"+messageId+"】处理失败");
                    /*发送至失败消息体mq*/
                    rabbitmqSender.sendMessage(bizExchange,failRoutKey,messageBean);
                }
                channel.basicAck(deliveryTag,false);
            }
        }
       /* if(!StringUtils.isEmpty(messageBean.getSendTimes())){
            int sendTimes= messageBean.getSendTimes();
            if(sendTimes<MAX_SEND_TIMES){
                System.out.println("消息Id【"+messageId+"】第【"+sendTimes+"】拒接接受消息体");
                messageBean.setSendTimes(sendTimes+1);
                //重复机制
               // rabbitmqSender.sendMessage(messageBean,Header,sendTimes*60*1000);
                channel.basicReject(deliveryTag,false);
            }
            // 手工ACK,不批量ack，multiple:当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
            //第二个参数说明如何处理这个失败消息。requeue 值为 true 表示该消息重新放回队列头，值为 false 表示放弃这条消息
            channel.basicAck(deliveryTag,false);
        }*/
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue.fail}",containerFactory = "singleListenerContainer")
    @RabbitHandler
    public void onFailMessage(Message message , Channel channel) throws IOException {
        System.out.println("=========失败队列消费==============");
        // 限流处理：消息体大小不限制，每次限制消费一条，只作用于该Consumer层，不作用于Channel
        channel.basicQos(0,1,false);
        MessageProperties properties=message.getMessageProperties();
        String messageId=properties.getMessageId();
        Long deliveryTag=properties.getDeliveryTag();
        Map<String , Object> Header=properties.getHeaders();
        System.out.println("headers: "+Header);
        String messageBody=new String(message.getBody(),"utf-8");
        System.out.println("消息体Id【"+messageId+"】消息结构【==="+messageBody);
        channel.basicAck(deliveryTag,false);

    }



}
