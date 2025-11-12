package com.mservice.fs.onboarding.utils;

import com.mservice.fs.http.client.ApiClient;
import com.mservice.fs.onboarding.config.GChatConfig;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.http.client.HttpData;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import io.micrometer.core.instrument.util.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;


/**
 * @author tuan.tran6
 * on 9/13/2024
 */
public class GChatSevice extends ApiClient<GChatConfig> {
    public void send(Base base, String message) {
        if (StringUtils.isEmpty(message)) {
            Log.MAIN.info("Empty message, terminating process.");
            return;
        }

        GChatConfig gChatConfig = getConfig();

        if (!gChatConfig.getEnable()) {
            Log.MAIN.info("Chat bot is already disabled, terminating process.");
            return;
        }

        this.workers.executeJob("SEND_ALERT", () -> {
                    try {
                        Log.MAIN.info("Alert sending...");
                        HttpData httpData = HttpData.of(() -> generatePayload(message).getBytes(StandardCharsets.UTF_8), gChatConfig.getExtraParams());
                        invoke(base, getConfig(), httpData);
                        Log.MAIN.info("Alert sent successfully.");
                    } catch (BaseException e) {
                        Log.MAIN.info("[GChatService] An error occurred while sending the notification", e);
                    }
                }
        );

    }

    public String generatePayload(String message) {
        return "{\"text\": \"" + StringEscapeUtils.escapeJson(message) + "\"}";
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
