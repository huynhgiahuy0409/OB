package com.mservice.fs.onboarding.model.common.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mservice.fs.onboarding.enums.AIMessageType;
import com.mservice.fs.onboarding.model.ai.LenderId;
import com.mservice.fs.onboarding.model.application.ScreenLog;
import com.mservice.fs.utils.JsonUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 9/5/2023
 */
@Getter
@Setter
public class KnockOutRuleRequest {

    private String requestId;
    private String appSessionId;
    private long requestTimestamp;
    private String productGroup;
    private String sourceId;
    private String loanProductCode;
    private String productId;
    private long agentId;
    private ScreenLog startScreenLog;
    private ScreenLog midScreenLog;
    private AIMessageType messageType;
    private String momoLoanAppId;
    private String merchantId;
    private LenderId lenderId;

    public KnockOutRuleRequest() {

    }

    public String encode() throws JsonProcessingException {
        return JsonUtil.toString(this);
    }

    public void setMidScreenLog(ScreenLog screenLog) {
        this.midScreenLog = screenLog;
        this.startScreenLog = null;
    }
}
