package com.levis.springbootrabbitmq.producer;

import com.levis.cn.Annotation.RabbitmqAnnotation;

/**
 * Created by le on 2019/5/10.
 */
public abstract class BasicSender {

     public RabbitmqAnnotation analysisAnnotation(Object message){
          if(message.getClass().isAnnotationPresent(RabbitmqAnnotation.class)){
               RabbitmqAnnotation rabbitmqAnnotation=message.getClass().getAnnotation(RabbitmqAnnotation.class);
               return rabbitmqAnnotation;
          }else{
               return  null;
          }
     }

}
