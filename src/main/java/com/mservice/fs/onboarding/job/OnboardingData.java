package com.mservice.fs.onboarding.job;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.processor.Callback;
import com.mservice.fs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

/**
 * @author hoang.thai on 7/27/2023
 */
@Getter
@Setter
public class OnboardingData<T extends OnboardingRequest, R extends OnboardingResponse> extends PlatformData<T, R> {

    private static final String PROCESS_NAME_KEY = "processName";

    public OnboardingData(Base base, Callback callback) {
        super(base, callback);
    }

    public void putProcessNameToTemPlateModel(String processName) {
        getTemplateModel().put(PROCESS_NAME_KEY, processName);
    }

    public void putDataToTemPlateModel(String key, String value) {
        if (Utils.isEmpty(key)) {
            Log.MAIN.error("Key require not null");
            return;
        }
        getTemplateModel().put(key, value);
    }

}
