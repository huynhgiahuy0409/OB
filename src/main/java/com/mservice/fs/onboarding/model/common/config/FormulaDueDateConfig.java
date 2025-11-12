package com.mservice.fs.onboarding.model.common.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author muoi.nong
 */
@Getter
@Setter
public class FormulaDueDateConfig {
    private int startDay;
    private int endDay;
    private String lenderId;
    private String serviceId;
    private int plusMonth;
    private int plusDay;
    private int withDay;
}
