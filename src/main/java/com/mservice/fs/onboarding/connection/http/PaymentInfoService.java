package com.mservice.fs.onboarding.connection.http;

import com.mservice.fs.generic.http.SimpleHttpResponse;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.http.client.ApiClient;
import com.mservice.fs.http.client.ApiConfig;
import com.mservice.fs.http.client.HttpData;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author hoang.thai
 * on 11/22/2023
 */
public class PaymentInfoService extends ApiClient<ApiConfig> {

    public String callApi(String rawRequest, Base base) throws BaseException, IOException {
        HttpData httpData = new HttpData();
        httpData.setBody(() -> rawRequest.getBytes(StandardCharsets.UTF_8));
        Log.MAIN.info("PaymentInfo request: {}", rawRequest);
        SimpleHttpResponse response = this.invoke(base, getConfig(), httpData);
        Log.MAIN.info("PaymentInfo response: {}", rawRequest);
        return response.getContentStr();
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
