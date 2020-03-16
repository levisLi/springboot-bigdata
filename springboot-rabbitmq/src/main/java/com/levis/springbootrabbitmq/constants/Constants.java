package com.levis.springbootrabbitmq.constants;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Created by le on 2019/6/3.
 */
public class Constants {

    public static Cache<String,Object> cache= CacheBuilder.newBuilder()
            // 设置缓存的最大容量
            .maximumSize(100)
            .concurrencyLevel(10) // 设置并发级别为10
            .recordStats() // 开启缓存统计
            .build();
}
