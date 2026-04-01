package com.lyh.live;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.lyh.live.mapper")
public class LiveApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(LiveApplication.class, args);
    }
}
