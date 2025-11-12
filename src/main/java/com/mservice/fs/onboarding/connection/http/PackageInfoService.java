package com.mservice.fs.onboarding.connection.http;

import com.mservice.fs.generic.http.SimpleHttpResponse;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.http.client.ApiClient;
import com.mservice.fs.http.client.ApiConfig;
import com.mservice.fs.http.client.HttpData;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.common.ai.GetPackageResponse;
import com.mservice.fs.utils.JsonUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author hoang.thai
 * on 8/15/2023
 */
public class PackageInfoService extends ApiClient<ApiConfig> {

    public GetPackageResponse callApi(String rawRequest, Base base) throws BaseException, Exception {
        HttpData httpData = new HttpData();
        httpData.setBody(() -> rawRequest.getBytes(StandardCharsets.UTF_8));
        Log.MAIN.info("Request get Package for Ai: {}", rawRequest);
        SimpleHttpResponse response = this.invoke(base, getConfig(), httpData);
        Log.MAIN.info("Response package from Ai: {}", response);
        return JsonUtil.fromByteArray(response.getContent(), GetPackageResponse.class);
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
