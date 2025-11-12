package com.mservice.fs.onboarding.connection.http;

import com.mservice.fs.generic.http.SimpleHttpResponse;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.http.client.ApiClient;
import com.mservice.fs.http.client.ApiConfig;
import com.mservice.fs.http.client.HttpData;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.common.ai.LoanDeciderResponse;
import com.mservice.fs.onboarding.model.common.config.AiConfig;

import java.nio.charset.StandardCharsets;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class LoanDeciderService extends ApiClient<ApiConfig> {

    private static final String LOAN_DECIDER_PATH = "loanDeciderPath";

    public LoanDeciderResponse callApi(String rawRequest, Base base, AiConfig aiConfig) throws BaseException {
        HttpData httpData = new HttpData();
        httpData.putPathVariable(LOAN_DECIDER_PATH, aiConfig.getLoanDeciderPath());
        httpData.setBody(() -> rawRequest.getBytes(StandardCharsets.UTF_8));
        Log.MAIN.info("Request Loan Decider: {}", rawRequest);
        SimpleHttpResponse response = this.invoke(base, getConfig(), httpData);
        Log.MAIN.info("Response Loan Decider: {}", response);
        return Json.decodeValue(response.getContentStr(), LoanDeciderResponse.class);
    }


    @Override
    public byte[] encrypt(Base base, byte[] bytes) throws Exception {
        return bytes;
    }

    @Override
    public byte[] decrypt(Base base, byte[] bytes) throws Exception {
        return bytes;
    }
}
