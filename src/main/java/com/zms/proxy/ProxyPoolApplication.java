package com.zms.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description : Object
 * @author: zeng.maosen
 */

@EnableAsync
@EnableRetry
@EnableScheduling
@SpringBootApplication
public class ProxyPoolApplication {

    public static void main(String[] args) {
      SpringApplication.run(ProxyPoolApplication.class, args);
    }
}
