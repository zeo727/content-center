package com.itmuch.contentcenter.service.content;

import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public ShareDTO findById(Integer id) {

        //获取分享详情
        Share share = this.shareMapper.selectByPrimaryKey(id);
        // 发布人id
        Integer userId = share.getUserId();

        /**
         * 强调：
         * 了解steam -->jdk 8新特性
         * lambda表达式
         * functional --> 函数式编程
         * **/


        // 用户中心所有实例的信息
        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
        String targetURL = instances.stream()
                //数据变换
                .map(instance -> instance.getUri().toString() + "users/{id}")
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("当前没有实例！"));

        log.info("请求的目标地址：{}",targetURL);
        // 怎么调用用户微服务/users/{userId}??

        //用HTTP GET方法去请求，并且返回一个对象
        UserDTO userDTO = this.restTemplate.getForObject(
                targetURL, UserDTO.class, userId
        );

        ShareDTO shareDTO =new ShareDTO();
        //消息的装配
        BeanUtils.copyProperties(share,shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        // 用HTTP GET方法去请求，并且返回一个对象
        ResponseEntity<String> forEntity = restTemplate.getForEntity(
                "http://localhost:8080/users/{id}",
                String.class, 2
        );

        System.out.println(forEntity.getBody());
        // 200 OK
        // 500
        // 502 bad gateway...
        System.out.println(forEntity.getStatusCode());
    }
}