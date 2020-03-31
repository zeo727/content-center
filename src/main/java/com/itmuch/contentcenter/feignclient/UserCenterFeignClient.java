package com.itmuch.contentcenter.feignclient;

import com.itmuch.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.itmuch.contentcenter.domain.dto.user.UserAddBonseDTO;
import com.itmuch.contentcenter.domain.dto.user.UserDTO;
import com.itmuch.contentcenter.feignclient.fallbackFactory.UserCenterFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "user-center",configuration = GlobalFeignConfiguration.class)
@FeignClient(name = "user-center"
//        ,fallback = UserCenterFeignClientFallback.class
        , fallbackFactory = UserCenterFeignClientFallbackFactory.class
)
public interface UserCenterFeignClient {

    /**
     * http://user-center/users/{id}
     *
     * @param id
     * @return
     */
    @GetMapping("/users/{id}")
    UserDTO findById(@PathVariable(value = "id") Integer id
//            , @RequestHeader("X-Token") String token
    );

    @PutMapping("/users/add-bonus")
    UserDTO addBonus(@RequestBody UserAddBonseDTO userAddBonusDTO);
}
