package com.levis.springbootrabbitmq.constants;

/**
 * Created by le on 2019/5/29.
 */
public enum RabbitEnum {

     SEND_TIMES("sendTimes");

     private String code;

    RabbitEnum(String code){
        this.code=code;
    }

    public String getCode(){
        return code;
    }

}
