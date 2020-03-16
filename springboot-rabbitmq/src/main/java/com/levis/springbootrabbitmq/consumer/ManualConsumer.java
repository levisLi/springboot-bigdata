package com.levis.springbootrabbitmq.consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.levis.cn.bean.MessageBean;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Created by le on 2019/5/13.
 */
@Slf4j
@Service
public class ManualConsumer {

    private static final Gson gson= new GsonBuilder().create();

    @Autowired
    private Channel channel;

    @Value("${spring.rabbitmq.queue.manual}")
    private String manualQueue;

    public void confirmMessage() throws IOException {
        log.info("=======手动确认消费者消费消息==========");
        // 限流处理：消息体大小不限制，每次限制消费一条，只作用于该Consumer层，不作用于Channel
        channel.basicQos(0,1,false);
        //声明消费者,直接在下面的监控中做
        Optional<GetResponse> responseOptional = Optional.ofNullable(channel.basicGet(manualQueue,false));
        if(responseOptional.isPresent()){
            GetResponse response=responseOptional.get();
            AMQP.BasicProperties props = response.getProps();
            byte[] body = response.getBody();
            String messageId=props.getMessageId();
            Long deliveryTag=response.getEnvelope().getDeliveryTag();
            Map<String , Object> Header=props.getHeaders();
            System.out.println("headers: "+Header);
            String messageBody =new String(body,"UTF-8");
            System.out.println("消息体==="+messageBody);
            MessageBean messageBean =gson.fromJson(messageBody, MessageBean.class);
            channel.basicAck(deliveryTag,false);
        }else{
            log.info("没有消息体入队列{}",manualQueue);
        }
    }

}
