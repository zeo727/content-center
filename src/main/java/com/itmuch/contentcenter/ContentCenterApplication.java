package com.itmuch.contentcenter;

import com.itmuch.contentcenter.rocketmq.MySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.spring.annotation.MapperScan;

// 扫描mybatis哪些包里面的接口
@MapperScan("com.itmuch.contentcenter.dao")
@SpringBootApplication
@EnableFeignClients //(defaultConfiguration = GlobalFeignConfiguration.class)
@EnableBinding({Source.class, MySource.class})
public class ContentCenterApplication {

    //启动类
    public static void main(String[] args) {
        SpringApplication.run(ContentCenterApplication.class, args);
    }

    // 在spring容器中，创建一个对象，类型RestTemplate；名称/ID是：restTemplate
    // <bean id="restTemplate" class="xxx.RestTemplate"/>
    @Bean
    @LoadBalanced
    //@SentinelRestTemplate
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
