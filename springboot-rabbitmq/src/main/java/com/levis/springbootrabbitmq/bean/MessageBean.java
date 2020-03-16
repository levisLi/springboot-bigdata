package com.levis.springbootrabbitmq.bean;

import com.levis.springbootrabbitmq.constants.MessageStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by le on 2019/5/5.
 */
@Data
public  class  MessageBean <T>  implements Serializable {

    private int sendTimes=1;

    /*消息状态*/
    private String status= MessageStatusEnum.WAITING_HANDLE.getCode();

    /*消息id*/
    private String msgId;

    /*消息主体*/
    private T body;

    /*消息头*/
    private Map<String,Object> headers;

}
