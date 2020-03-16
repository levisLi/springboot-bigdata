package com.levis.springbootrabbitmq.messageConfirm.producer;

import com.levis.springbootrabbitmq.bean.MessageBean;
import com.levis.springbootrabbitmq.producer.RabbitmqSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by le on 2019/6/3.
 */
@Slf4j
@Service
public class ProducerServiceImpl implements IproducerService {


    @Value("${spring.rabbitmq.exchange.business}")
    private String bizExchange;

    @Value("${spring.rabbitmq.routKey.business}")
    private  String bizRoutKey;

    @Value("${spring.rabbitmq.routKey.temp}")
    private  String tempRoutKey;



    @Autowired
    private RabbitmqSender rabbitmqSender;

    @Override
    public void saveMsgDb(MessageBean bean) {
        log.info("保存信息成功 {}",bean);
    }

    @Override
    public void sendMsg(MessageBean bean) {
        rabbitmqSender.sendMessage(bizExchange,bizRoutKey,bean);
        log.info("发送消息成功");
    }

    @Override
    public void sendDlxMsg(MessageBean bean) {
        Object messageBody=bean.getBody();
        Map<String,Object> header=bean.getHeaders();
        int ttl=2*60*1000;
        rabbitmqSender.sendMessage(bizExchange,tempRoutKey,bean,ttl);
        log.info("发送延迟消息成功");
    }
}
