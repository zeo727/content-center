package com.itmuch.contentcenter.service.content;

import com.alibaba.fastjson.JSON;
import com.itmuch.contentcenter.dao.content.ShareMapper;
import com.itmuch.contentcenter.dao.messaging.RocketmqTransactionLogMapper;
import com.itmuch.contentcenter.domain.dto.content.ShareAuditDTO;
import com.itmuch.contentcenter.domain.dto.content.ShareDTO;
import com.itmuch.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.domain.entity.content.Share;
import com.itmuch.contentcenter.domain.entity.messaging.RocketmqTransactionLog;
import com.itmuch.contentcenter.domain.enums.AuditStatusEnum;
import com.itmuch.contentcenter.feignclient.UserCenterFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareService {
    private final ShareMapper shareMapper;
    private final UserCenterFeignClient userCenterFeignClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;
    private final Source source;
//    private final RestTemplate restTemplate;
//    private final DiscoveryClient discoveryClient;

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
        ShareDTO shareDTO = new ShareDTO();
        //消息的装配
        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }

    public Share auditById(Integer id, ShareAuditDTO auditDTO) {
        // 1. 查询share是否存在，不存在或者当前的audit_status != NOT_YET，那么抛异常
        Share share = this.shareMapper.selectByPrimaryKey(id);
        if (share == null) {
            throw new IllegalArgumentException("参数非法！该分享不存在");
        }
        if (!Objects.equals("NOT_YET", share.getAuditStatus())) {
            throw new IllegalArgumentException("参数非法！该分享已审核");
        }
        // 3. 如果是PASS，那么发送消息给rocketmq，让用户中心去消费，并为发布人添加积分
        if (AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum())){
            // 发送半消息。。
            String transactionId = UUID.randomUUID().toString();

            this.source.output()
                    .send(MessageBuilder
                            .withPayload(
                                    UserAddBonusMsgDTO.builder()
                                            .userId(share.getUserId())
                                            .bonus(50)
                                            .build()
                            )
                            // header也有妙用
                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                            .setHeader("share_id",id)
                            .setHeader("dto", JSON.toJSONString(auditDTO))
                            .build()
                    );

//            this.rocketMQTemplate.sendMessageInTransaction("tx-add-bonus-group",
//                    "add-bonus",
//                    MessageBuilder
//                            .withPayload(
//                                    UserAddBonusMsgDTO.builder()
//                                            .userId(share.getUserId())
//                                            .bonus(50)
//                                            .build()
//                            )
//                            // header也有妙用
//                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
//                            .setHeader("share_id",id)
//                            .build(),
//                    //arg有大用处
//                    auditDTO
//            );
        }
        else {
            this.auditByIdInDB(id,auditDTO);
        }
//        // 2. 审核资源，将状态设为PASS/REJECT
//        share.setAuditStatus(auditDTO.getAuditStatusEnum().toString());
//        this.shareMapper.updateByPrimaryKey(share);
//        // 3. 如果是PASS，为发布人添加积分
//        // 异步执行
//        userCenterFeignClient.addBonus(id,500);
//
//        this.rocketMQTemplate.convertAndSend(
//                "add-bonus",
//                UserAddBonusMsgDTO.builder()
//                        .userId(share.getUserId())
//                        .bonus(50)
//                        .build());
        // 4.把share写到缓存
        return share;
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInDB(Integer id,ShareAuditDTO auditDTO) {
        Share share = Share.builder()
                .id(id)
                .auditStatus(auditDTO.getAuditStatusEnum().toString())
                .reason(auditDTO.getReason())
                .build();
        share.setAuditStatus(auditDTO.getAuditStatusEnum().toString());

        // updateByPrimaryKeySelective,updateByPrimaryKey
        //前者只是更新新的model中不为空的字段。后者则会将为空的字段在数据库中置为NULL。
        this.shareMapper.updateByPrimaryKeySelective(share);
    }
    @Transactional(rollbackFor = Exception.class)
    public void auditByIdWithRocketMqLog(Integer id, ShareAuditDTO auditDTO, String transactionId) {
        this.auditByIdInDB(id, auditDTO);

        this.rocketmqTransactionLogMapper.insertSelective(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .log("审核分享...")
                        .build()
        );
    }
}