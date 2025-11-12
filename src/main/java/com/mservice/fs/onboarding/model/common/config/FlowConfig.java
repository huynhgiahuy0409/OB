package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/11/2023
 */
@Getter
@Setter
public class FlowConfig {

    private String serviceId;
    private String processName;
    private String nextProcessName;

}
