package com.mservice.fs.onboarding.model.disburse;

import com.mservice.fs.json.Json;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreDisburseRequest {
    private long time = System.currentTimeMillis();
    private String user;
    private String cmdId;
    private String msgType;
    private Data data;

    @Getter
    @Setter
    public static class Data {
        private long amount;
        private String serviceId;
        private int disbursementSuccessTimes;
        private long lastDisbursementTime;
    }

    public String encode() {
        return Json.encode(this);
    }
}
