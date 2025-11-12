package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 06/13/2024
 */
@Getter
@Setter
public class ConsentConfigDB {

    private String id;
    private String serviceId;
    private String partnerId;
    private String partnerCode;
    private String miniAppId;
    private String flow;
    private String active;
    private String attributeName;
    private String accessType;
    private String createTime;
    private String modifyTime;

}
