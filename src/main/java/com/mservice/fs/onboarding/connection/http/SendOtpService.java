package com.mservice.fs.onboarding.connection.http;

import com.mservice.fs.generic.http.SimpleHttpResponse;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.http.client.ApiClient;
import com.mservice.fs.http.client.ApiConfig;
import com.mservice.fs.http.client.HttpData;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.common.config.TelcoConfig;
import com.mservice.fs.onboarding.model.telco.OtpRequest;

public class SendOtpService extends ApiClient<ApiConfig> {

    public SimpleHttpResponse sendOtp(Base base, String telcoCourier, TelcoConfig telcoConfig) throws BaseException {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setRequestId(base.getTraceId());
        otpRequest.setPhoneNumber(base.getInitiator());
        otpRequest.setAppSessionId(base.getSessionId());
        otpRequest.setRequestSource(telcoConfig.getSourceId());
        otpRequest.setTelcoCourier(telcoCourier);
        Log.MAIN.info("SEND OTP TELCO REQUEST:{}", Json.encode(otpRequest));
        HttpData httpData = new HttpData();
        httpData.setBody(() -> Json.encodeToByteArrays(otpRequest));
        return invoke(base, getConfig(), httpData);
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
