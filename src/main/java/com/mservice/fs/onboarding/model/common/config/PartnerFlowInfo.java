package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/1/2023
 */
@Getter
@Setter
public class PartnerFlowInfo {

    private String serviceId;
    private String partnerId;
    private String processName;
    private String nextProcessName;

}
