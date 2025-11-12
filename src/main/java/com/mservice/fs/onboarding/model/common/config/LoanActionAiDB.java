package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/26/2023
 */
@Getter
@Setter
public class LoanActionAiDB {

    private String actionName;
    private int resultCode;
    private String redirectProcessName;
    private OBAiTypeConfig type;
}
