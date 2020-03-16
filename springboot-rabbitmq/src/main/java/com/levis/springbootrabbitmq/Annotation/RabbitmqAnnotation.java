package com.levis.springbootrabbitmq.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by le on 2019/5/13.
 */
@Target({ ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RabbitmqAnnotation {
    /*交换机名称*/
    String exchange() default "";

    /*队列名称*/
    String queue() default "";

    /*路由key*/
    String routingKey() default "";
}
