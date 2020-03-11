package com.itmuch.contentcenter.service.content;

import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;
    private final UserCenterFeignClient userCenterFeignClient;
//    private final RestTemplate restTemplate;
//    private final DiscoveryClient discoveryClient;

    public ShareDTO findById(Integer id) {

        //获取分享详情
        Share share = this.shareMapper.selectByPrimaryKey(id);
        // 发布人id
        Integer userId = share.getUserId();

//        /**
//         * 强调：
//         * 了解steam -->jdk 8新特性
//         * lambda表达式
//         * functional --> 函数式编程
//         * **/
//
//
//        // 用户中心所有实例的信息
//        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
//        // 所有用户中心所有实例的请求地址
//        List<String> targetURLS = instances.stream()
//                //数据变换
//                .map(instance -> instance.getUri().toString() + "users/{id}")
//                .collect(Collectors.toList());
//        int i = ThreadLocalRandom.current().nextInt(targetURLS.size());
//
//        String targetURL = targetURLS.get(i);
//        log.info("请求的目标地址：{}",targetURL);
//        // 怎么调用用户微服务/users/{userId}??

        //用HTTP GET方法去请求，并且返回一个对象
        /*
         * 1.代码不可读
         * 2.复杂的url难以维护:https://www.baidu.com/s?wd=a&rsv_spt=1&rsv_iqid=0x81a9ee5e00024dfa&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=0&rsv_dl=tb&rsv_sug3=1&inputT=1082&rsv_sug4=1081
         * 3.难以响应需求的变化，变化没有幸福感
         * 编程体验不统一
         * */
//        UserDTO userDTO = this.restTemplate.getForObject(
//                "http://user-center/users/{userId}", UserDTO.class, userId
//        );
        UserDTO userDTO = this.userCenterFeignClient.findById(userId);
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