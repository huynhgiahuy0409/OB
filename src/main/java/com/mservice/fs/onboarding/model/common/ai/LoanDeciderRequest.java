package com.mservice.fs.onboarding.model.common.ai;

import com.mservice.fs.json.Json;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
@Getter
@Setter
public class LoanDeciderRequest {

    private String requestId;
    private String appSessionId;
    private long requestTimestamp;
    private String productGroup;
    private String merchantId;
    private String sourceId;
    private String loanProductCode;
    private String productId;
    private long agentId;
    private SummaryScreenLog summaryScreenLog;
    private String messageType;
    private int screenOrder;
    private String momoLoanAppId;
    private String lenderId;
    private String segmentUser;
    private String experimentTag;

    public String encode() {
        return Json.encode(this);
    }
}
