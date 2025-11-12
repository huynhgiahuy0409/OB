package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.application.submit.ModifyResponseTask;
import com.mservice.fs.onboarding.job.application.submit.SubmitSendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
public class SubmitModifyResponseTask extends ModifyResponseTask<SubmitRequest, SubmitResponse> {

    @Override
    protected SubmitResponse createResponse(OnboardingData<SubmitRequest, SubmitResponse> jobData) {
        SubmitResponse response = new SubmitResponse();
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        OtpInfo otpCacheInfo = applicationData.getOtpInfo();
        response.setMaxGenerateTimes(otpCacheInfo.getMaxGenerateTimes());
        response.setMaxVerifyTimes(otpCacheInfo.getMaxVerifyTimes());
        response.setCurrentGenerateTimes(otpCacheInfo.getCurrentTimesGenerate());
        response.setCurrentVerifyTimes(otpCacheInfo.getCurrentTimesVerify());
        response.setValidOtpInMillis(otpCacheInfo.getValidOtpInMillis());
        updateWebViewLinkResponse(jobData, response);
        response.setApplicationData(applicationData);
        response.setResultCode(CommonErrorCode.SUCCESS);
        return response;
    }

    private void updateWebViewLinkResponse(OnboardingData<SubmitRequest, SubmitResponse> jobData, SubmitResponse response) {
        TaskData submitAdapterTaskData = jobData.getTaskData(SubmitSendAdapterTask.NAME);
        if (Utils.isNotEmpty(submitAdapterTaskData) && Utils.isNotEmpty(submitAdapterTaskData.getContent())) {
            Log.MAIN.info("Updating web view link");
            GenerateOtpAdapterResponse adapterResponse = submitAdapterTaskData.getContent();
            response.setWebViewLink(adapterResponse.getWebViewLink());
        }
    }

}
