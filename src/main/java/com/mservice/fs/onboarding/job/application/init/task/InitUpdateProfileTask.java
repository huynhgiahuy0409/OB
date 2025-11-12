package com.mservice.fs.onboarding.job.application.init.task;

import com.mservice.fs.onboarding.job.AbsGetUserProfileTask;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.Address;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.init.InitFormRequest;
import com.mservice.fs.onboarding.model.api.init.InitFormResponse;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;

/**
 * @author hoang.thai
 * on 12/25/2023
 */
public class InitUpdateProfileTask extends UpdateProfilePendingFormTask<InitFormRequest, InitFormResponse> {

    public ApplicationForm getApplicationForm(OnboardingData<InitFormRequest, InitFormResponse> onboardingData) {
        return onboardingData.getTaskData(HandlePendingFormTask.NAME).getContent();
    }

    public UserProfileInfo getUserProfile(OnboardingData<InitFormRequest, InitFormResponse> onboardingData) {
        return onboardingData.getTaskData(AbsGetUserProfileTask.NAME).getContent();
    }

    @Override
    protected void updateProfileForm(OnboardingData<InitFormRequest, InitFormResponse> onboardingData, ApplicationForm applicationForm, UserProfileInfo userProfileInfo) {
        super.updateProfileForm(onboardingData, applicationForm, userProfileInfo);

        if (getConfig().getInitCurrentAddressService().contains(onboardingData.getServiceId().toLowerCase())) {
            Address currentAddress = new Address();
            currentAddress.setFullAddress(userProfileInfo.getCurrentAddress());
            applicationForm.getApplicationData().setCurrentAddress(currentAddress);
        }
    }
}
