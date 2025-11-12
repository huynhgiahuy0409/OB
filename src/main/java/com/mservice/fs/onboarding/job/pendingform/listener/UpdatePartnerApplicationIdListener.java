package com.mservice.fs.onboarding.job.pendingform.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.onboarding.connection.jdbc.UpdatePartnerApplicationProcessor;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.pendingform.task.ApplicationCacheTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/28/2023
 */
public class UpdatePartnerApplicationIdListener extends OnboardingListener<PendingFormRequest, PendingFormResponse> {

    private static final String NAME = "UPDATE_PARTNER_APPLICATION_ID";
    @Autowire(name = "UpdatePartnerApplicationId")
    private UpdatePartnerApplicationProcessor updatePartnerApplicationProcessor;

    public UpdatePartnerApplicationIdListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<PendingFormRequest, PendingFormResponse> jobData) throws Throwable {
        TaskData taskData = jobData.getTaskData(ApplicationCacheTask.NAME);
        if (Utils.isEmpty(taskData)) {
            return;
        }
        ApplicationForm applicationForm = taskData.getContent();
        if (Utils.isEmpty(applicationForm)) {
            return;
        }
        OtpInfo otpInfo = applicationForm.getApplicationData().getOtpInfo();
        if (Utils.isEmpty(otpInfo)) {
            return;
        }
        ApplicationData applicationData = applicationForm.getApplicationData();
        String partnerApplicationId = applicationData.getPartnerApplicationId();
        if (Utils.isNotEmpty(otpInfo.getWebViewLink()) && Utils.isNotEmpty(partnerApplicationId)) {
            updatePartnerApplicationProcessor.execute(applicationData); // todo
        }

    }
}
