package com.mservice.fs.onboarding.model.application.init;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hoang.thai
 * on 11/17/2023
 */
@Getter
@Setter
@Builder
public class InitDataDB {
    private List<ApplicationDataInit> applicationByAgentId;
    private List<ApplicationDataInit> applicationByPersonalId;
}
