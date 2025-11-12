package com.mservice.fs.onboarding.model.common.ai;

import com.mservice.fs.json.CacheObject;
import lombok.Getter;

/**
 * @author tuan.tran6
 * on 9/5/2023
 */
@Getter
public class KnockOutRuleResponse implements CacheObject {

    private static final String NAME = "KNOCK_OUT_RULE";
    public static final long TIME_CACHE = 3600000l;

    private String requestId;
    private LoanDeciderRecord loanDeciderRecord;
    private String responseTimestamp;

    public static String createKeyCache(String serviceId, String agentId) {
        return String.format("%s:%s_%s", NAME, serviceId, agentId);
    }

}
