package com.mservice.fs.onboarding.model.notifyuser;

import com.mservice.fs.json.CacheObject;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import lombok.Getter;

import java.util.List;

@Getter
public class NotifyUserRequest extends OnboardingRequest implements CacheObject {

    private List<ApplicationForm> applicationForms;
}
