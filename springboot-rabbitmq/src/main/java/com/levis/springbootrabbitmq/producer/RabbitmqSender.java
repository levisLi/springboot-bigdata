package com.levis.springbootrabbitmq.producer;


import com.levis.springbootrabbitmq.bean.MessageBean;

public interface RabbitmqSender {

    void sendMessage(String exchangeName, String routKey, MessageBean bean) ;

    void sendMessage(String exchangeName, String routKey, MessageBean bean, int ttl) ;

}
