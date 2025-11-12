package com.mservice.fs.onboarding.model.common.config;

import com.mservice.fs.json.Json;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 12/26/2023
 */
@Getter
@Setter
public class LoanActionAiConfig {

    private String actionName;
    private int resultCode;
    private String resultMessage;
    private String redirectProcessName;
    private OBAiTypeConfig aiTypeConfig;

    @Override
    public String toString() {
        return Json.encode(this);
    }
}
