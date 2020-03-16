package com.levis.springbootrabbitmq.messageConfirm.producer;


import com.levis.springbootrabbitmq.bean.MessageBean;

/**
 * Created by le on 2019/6/3.
 */
public interface IproducerService {

    void saveMsgDb(MessageBean bean);

    void sendMsg(MessageBean bean);

    void sendDlxMsg(MessageBean bean);

}
