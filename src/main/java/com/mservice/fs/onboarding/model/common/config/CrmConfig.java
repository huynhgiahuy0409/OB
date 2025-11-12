package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author muoi.nong
 */
@Getter
@Setter
public class CrmConfig {
    private String serviceId;
    private String status;
    private String statusMessage;
    private int isAllowDelete;

}
