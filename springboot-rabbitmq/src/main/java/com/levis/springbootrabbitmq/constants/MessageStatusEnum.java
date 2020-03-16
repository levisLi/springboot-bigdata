package com.levis.springbootrabbitmq.constants;

/**
 * Created by le on 2019/5/31.
 */
public enum MessageStatusEnum {

    WAITING_HANDLE("待处理","1"),

    FAILURE("处理失败","0"),

    SUCCESS("处理成功","2");

    private String status;

    private String code;

    MessageStatusEnum(String status,String code){
        this.status=status;
        this.code=code;
    }

    public String getCode(){
        return code;
    }
}
