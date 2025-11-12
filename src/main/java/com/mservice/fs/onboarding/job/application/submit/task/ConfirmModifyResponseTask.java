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
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Generics;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 12/15/2023
 */
public class ConfirmModifyResponseTask<T extends ConfirmRequest, R extends ConfirmResponse> extends ModifyResponseTask<T, R> {

    @Override
    protected R createResponse(OnboardingData<T, R> jobData) throws ReflectiveOperationException {
        R response = (R) Generics.createObject(this.responseClass);
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        OtpInfo otpCacheInfo = applicationData.getOtpInfo();
        response.setMaxGenerateTimes(otpCacheInfo.getMaxGenerateTimes());
        response.setMaxVerifyTimes(otpCacheInfo.getMaxVerifyTimes());
        response.setCurrentGenerateTimes(otpCacheInfo.getCurrentTimesGenerate());
        response.setCurrentVerifyTimes(otpCacheInfo.getCurrentTimesVerify());
        response.setValidOtpInMillis(otpCacheInfo.getValidOtpInMillis());
        updateWebViewLinkResponse(jobData, response);
        response.setResultCode(CommonErrorCode.SUCCESS);
        response.setApplicationData(applicationData);
        return response;
    }

    private void updateWebViewLinkResponse(OnboardingData<T, R> jobData, R response) {
        TaskData adapterTaskData = jobData.getTaskData(SubmitSendAdapterTask.NAME);
        if (Utils.isNotEmpty(adapterTaskData) && Utils.isNotEmpty(adapterTaskData.getContent())) {
            Log.MAIN.info("Updating web view link");
            GenerateOtpAdapterResponse adapterResponse = adapterTaskData.getContent();
            response.setWebViewLink(adapterResponse.getWebViewLink());
        }
    }
}
