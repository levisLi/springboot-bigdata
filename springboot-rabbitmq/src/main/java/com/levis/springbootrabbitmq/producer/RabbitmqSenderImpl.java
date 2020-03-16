package com.levis.springbootrabbitmq.producer;

import com.alibaba.fastjson.JSON;
import com.levis.springbootrabbitmq.bean.MessageBean;
import com.levis.springbootrabbitmq.constants.Constants;
import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@Slf4j
public class RabbitmqSenderImpl extends BasicSender  implements RabbitmqSender {

    @Value("${spring.rabbitmq.exchange.business}")
    private String topicExchange;

    @Value("${spring.rabbitmq.exchange.business}")
    private final static String ROUTING_KEY="spring.rabbitmq";


    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final DefaultMessagePropertiesConverter messagePropertiesConverter=new DefaultMessagePropertiesConverter();


    @Override
    public void sendMessage(String exchangeName, String routKey, MessageBean message) {
        AMQP.BasicProperties basicProperties=new AMQP.BasicProperties.Builder().deliveryMode(2).headers(message.getHeaders()).build();
        MessageProperties properties=this.messagePropertiesConverter.toMessageProperties(basicProperties,null,"UTF-8");
        this.basicSend(exchangeName,routKey,message,properties);
      /*  MessageHeaders messageHeaders = new MessageHeaders(heads);
        RabbitmqAnnotation rabbitmqAnnotation=analysisAnnotation(message);
        if(null == rabbitmqAnnotation){
            log.error("消息体没有包含rabbitmqAnnotation注解");
        }else{
            Message msg= MessageBuilder.createMessage(message,messageHeaders);
            log.info("原型消息消息体属性=={}",messageHeaders);
            String exchangeName=rabbitmqAnnotation.exchange();
            String routingKey=rabbitmqAnnotation.routingKey();
            if(!StringUtils.isEmpty(exchangeName)&&!StringUtils.isEmpty(routingKey)){
                //发送消息
                rabbitTemplate.convertAndSend(exchangeName,routingKey,msg,buildCorrelationData());
            }else {
                log.error("消息体中rabbitmqAnnotation注解缺少必要的参数");
            }
        }*/

    }

    @Override
    public void sendMessage(String exchangeName,String routKey,MessageBean message, int ttl) {
        AMQP.BasicProperties basicProperties=new AMQP.BasicProperties.Builder().headers(message.getHeaders()).deliveryMode(2).expiration(String.valueOf(ttl)).build();
        log.info("原始消费者消息体属性={}",basicProperties);
        MessageProperties properties=this.messagePropertiesConverter.toMessageProperties(basicProperties,null,"UTF-8");
        this.basicSend(exchangeName,routKey,message,properties);
    }

    private void basicSend(String exchangeName,String routKey,MessageBean message,MessageProperties properties){
        log.info("消息体属性===={}",properties);
        String msgId=message.getMsgId();
        if(!ObjectUtils.isEmpty(message.getBody())){
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setContentEncoding("UTF-8");
            if(StringUtils.isEmpty(message.getMsgId())){
                properties.setMessageId(UUID.randomUUID().toString());
            }else{
                properties.setMessageId(message.getMsgId());
            }
            String json= JSON.toJSONString(message);
            try {
                org.springframework.amqp.core.Message msg= new org.springframework.amqp.core.Message(json.getBytes(),properties);
                rabbitTemplate.convertAndSend(exchangeName,routKey,msg,this.buildCorrelationData());
//                Constants.cache.put(msgId,message);
            }catch (Exception e){
                log.error("消息Id{}异常发送{}",msgId,e.getMessage());
            }
        }else{
            log.error("消息体主题为空");
        }
    }

    private CorrelationData buildCorrelationData(){
        return new CorrelationData(UUID.randomUUID().toString());
    }

    private Object convertBytesToObject(byte[] body, String encoding,
                                        Class<?> clazz) throws UnsupportedEncodingException {
        String contentAsString = new String(body, encoding);
        return JSON.parseObject(contentAsString, clazz);
    }

}
