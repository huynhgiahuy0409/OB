package com.mservice.fs.onboarding.config;

import com.mservice.fs.http.client.ApiConfig;
import lombok.Getter;

import java.util.Map;

/**
 * @author tuan.tran6
 * on 9/13/2024
 */
@Getter
public class GChatConfig extends ApiConfig {
    private Boolean enable;
    private Map<String, String> extraParams;
}
