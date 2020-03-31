package com.itmuch.contentcenter.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserAddBonseDTO {
    private Integer userId;
    /**
     * 积分
     */
    private Integer bonus;
}
