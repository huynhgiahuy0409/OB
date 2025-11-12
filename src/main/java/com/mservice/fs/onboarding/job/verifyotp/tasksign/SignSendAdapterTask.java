package com.mservice.fs.onboarding.job.verifyotp.tasksign;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.verifyotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationState;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.utils.Utils;

import java.util.Set;

public class SignSendAdapterTask extends SendAdapterTask {

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    private static final Set<ApplicationStatus> PARTNER_STATUS = Set.of(ApplicationStatus.REJECTED_BY_LENDER, ApplicationStatus.CANCELED_BY_LENDER, ApplicationStatus.REJECTED_BY_MOMO);

    @Override
    protected void setDataResponse(VerifyOtpResponse response, OtpInfo otpInfo, OtpConfig otpConfigInfo, ApplicationData applicationData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData, VerifyOtpAdapterResponse adapterResponse) throws BaseException, ValidatorException, Exception {

        if (adapterResponse.isClearPendingForm()) {
            applicationData.setStatus(ApplicationStatus.APPROVED_BY_LENDER);
            applicationData.setState(ApplicationStatus.APPROVED_BY_LENDER.getState());
            response.setApplicationData(applicationData);
            response.setApplicationId(applicationData.getApplicationId());
            return;
        }

        int maxGenerateOtpTimes = otpConfigInfo.getMaxGenerateOtpTimes();
        int maxVerifyOtpTimes = otpConfigInfo.getMaxVerifyOtpTimes();
        int currentVerifyTimes = otpInfo.getCurrentTimesVerify() + 1;
        int remainingVerifyTimes = maxVerifyOtpTimes - currentVerifyTimes;

        otpInfo.setCurrentTimesVerify(currentVerifyTimes);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
        applicationData.setStatus(ApplicationStatus.VERIFIED_OTP_SIGN_FAILED);
        applicationData.setState(ApplicationStatus.VERIFIED_OTP_SIGN_FAILED.getState());
        applicationData.setReasonId(adapterResponse.getReasonId());
        applicationData.setReasonMessage(adapterResponse.getReasonMessage());
        if (Utils.isNotEmpty(adapterResponse.getPartnerApplicationId())) {
            Log.MAIN.info("Partner applicationId is not null", adapterResponse.getPartnerApplicationId());
            applicationData.setPartnerApplicationId(adapterResponse.getPartnerApplicationId());
        }

        ApplicationStatus partnerStatus = adapterResponse.getPartnerStatus();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (partnerStatus != null) {
            if (!PARTNER_STATUS.contains(partnerStatus)) {
                Log.MAIN.error("Error when partner status adapter not in [{}]", PARTNER_STATUS);
                throw new BaseException(OnboardingErrorCode.INVALID_STATUS);
            }
            Log.MAIN.info("Update status by Partner");
            applicationData.setStatus(partnerStatus);
            if (serviceObInfo.isMatchAction(Action.BANNED_BY_PARTNER, jobData.getProcessName()) && partnerStatus == ApplicationStatus.REJECTED_BY_LENDER) {
                Log.MAIN.info("Banked application by partner !!!");
                applicationData.setState(ApplicationState.BANNED);
            }
        }
        response.setMaxGenerateTimes(maxGenerateOtpTimes);
        response.setMaxVerifyTimes(maxVerifyOtpTimes);
        response.setValidOtpInMillis(otpConfigInfo.getValidOtpInMillis());
        response.setRemainingVerifyTimes(remainingVerifyTimes);
        response.setCurrentVerifyTimes(currentVerifyTimes);
        response.setApplicationData(applicationData);
        response.setApplicationId(applicationData.getApplicationId());
    }
}
