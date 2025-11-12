package com.mservice.fs.onboarding.model.telco;

import lombok.Getter;
import lombok.Setter;

/**
 * @author phat.duong
 * on 2/25/2025
 **/
@Getter
@Setter
public class ExternalApiResponse {
    private String data;
    private int statusCode;
    private String latency;
    private String responseMessage;
    private boolean isFromCache;
}
