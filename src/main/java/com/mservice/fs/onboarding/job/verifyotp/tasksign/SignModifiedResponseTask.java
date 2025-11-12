package com.mservice.fs.onboarding.job.verifyotp.tasksign;

import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.verifyotp.task.ModifiedResponseTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.utils.Utils;

public class SignModifiedResponseTask extends ModifiedResponseTask {

    @Override
    protected void setDataResponse(VerifyOtpResponse response, OtpInfo otpInfo, OtpConfig otpConfigInfo, ApplicationData applicationData, VerifyOtpAdapterResponse adapterResponseTask) {
        int maxGenerateOtpTimes = otpConfigInfo.getMaxGenerateOtpTimes();
        int maxVerifyOtpTimes = otpConfigInfo.getMaxVerifyOtpTimes();
        int currentVerifyTimes = otpInfo.getCurrentTimesVerify() + 1;
        int remainingVerifyTimes = maxVerifyOtpTimes - currentVerifyTimes;

        otpInfo.setCurrentTimesVerify(currentVerifyTimes);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());

        applicationData.setStatus(ApplicationStatus.VERIFIED_OTP_SIGN_SUCCESS);
        applicationData.setState(ApplicationStatus.VERIFIED_OTP_SIGN_SUCCESS.getState());
        applicationData.setReasonMessage(adapterResponseTask.getReasonMessage());

        if (Utils.isNotEmpty(adapterResponseTask.getPartnerApplicationId())) {
            Log.MAIN.info("Partner applicationId is not null : [{}]", adapterResponseTask.getPartnerApplicationId());
            applicationData.setPartnerApplicationId(adapterResponseTask.getPartnerApplicationId());
        }

        response.setMaxGenerateTimes(maxGenerateOtpTimes);
        response.setMaxVerifyTimes(maxVerifyOtpTimes);
        response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
        response.setRemainingVerifyTimes(remainingVerifyTimes);
        response.setCurrentVerifyTimes(currentVerifyTimes);
        response.setApplicationData(applicationData);
        response.setApplicationId(applicationData.getApplicationId());

        response.setResultCode(CommonErrorCode.SUCCESS);
    }
}
