package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 1/18/2024
 */
@Getter
@Setter
public class ObContractConfig {

    private String serviceId;
    private String partnerId;
    private String type;
    private Integer applyZeroInterest;
}
