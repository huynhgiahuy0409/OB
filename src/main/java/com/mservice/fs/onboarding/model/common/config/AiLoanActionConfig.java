package com.mservice.fs.onboarding.model.common.config;


import com.mservice.fs.json.Json;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AiLoanActionConfig {
    private String serviceId;
    private List<String> userProfileInfos = new ArrayList<>();
    private List<String> aiLoanActionNames = new ArrayList<>();
    private Integer resultCode = OnboardingErrorCode.CALL_AI_ERROR.getCode();
    private String redirectProcessName;


    public boolean isMapConfig(List<String> otherUserProfileInfos, List<String> otherAiLoanActionNames) {
        if (Utils.isEmpty(otherUserProfileInfos) || Utils.isEmpty(otherAiLoanActionNames)) {
            return false;
        }
        return compareListConfigs(userProfileInfos, otherUserProfileInfos) &&
                compareListConfigs(aiLoanActionNames, otherAiLoanActionNames);
    }

    private boolean compareListConfigs(List<String> currentConfigs, List<String> otherConfigs) {
        if (currentConfigs.size() != otherConfigs.size()) {
            return false;
        }
        for (String currentConfig : currentConfigs) {
            if (Constant.ANY_ACTION.equals(currentConfig)) {
                continue;
            }
            if (!otherConfigs.contains(currentConfig)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return Json.encode(this);
    }
}