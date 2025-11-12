package com.mservice.fs.onboarding.job.application.submit;

import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSubmitTask;
import com.mservice.fs.onboarding.job.SendApplicationPlatformTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.utils.Utils;

/**
 * @author hoang.thai
 * on 11/22/2023
 */

public class SubmitSendAdapterTask<T extends ConfirmRequest, R extends SubmitResponse> extends OnboardingSubmitTask<T, R> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) throws BaseException, ValidatorException, Exception {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        //create otp info
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(jobData.getPartnerId());
        OtpConfig otpConfig = partnerConfig.getOtpConfig();

        OtpInfo otpInfo = new OtpInfo();
        otpInfo.setMaxGenerateTimes(otpConfig.getMaxGenerateOtpTimes());
        otpInfo.setMaxVerifyTimes(otpConfig.getMaxGenerateOtpTimes());
        otpInfo.setCurrentTimesGenerate(DEFAULT_TIME_GENERATE_OTP);
        otpInfo.setCurrentTimesVerify(DEFAULT_CURRENT_TIMES_VERIFY_OTP);
        otpInfo.setValidOtpInMillis(otpConfig.getValidOtpInMillis());
        otpInfo.setOtpLength(otpConfig.getOtpLength());
        applicationForm.getApplicationData().setOtpInfo(otpInfo);

        if (serviceObInfo.isMatchAction(Action.SEND_PLATFORM_TASK, jobData.getProcessName()) && partnerConfig.isApplySendPlatformTask()) {
            ApplicationResponse applicationResponse = jobData.getTaskData(SendApplicationPlatformTask.NAME).getContent();
            applicationForm.getApplicationData().setContractId(applicationResponse.getApplicationData().getContractId());
        }

        return applicationForm;
    }

    @Override
    protected void processInGenerateOtpFlow(OnboardingData<T, R> jobData, ApplicationData applicationData, GenerateOtpAdapterResponse adapterResponse) throws BaseException, ValidatorException, Exception {
        OtpInfo otpInfo = applicationData.getOtpInfo();

        if (Utils.isNotEmpty(adapterResponse)) {
            String adapterPartnerKey = adapterResponse.getOtpPartnerKey();
            String webViewLink = adapterResponse.getWebViewLink();
            boolean otpFlow = adapterResponse.isGenerateOtp();
            Log.MAIN.info("Otp flow partnerKey {} - webViewLink {} - otpFlow {}", adapterPartnerKey, webViewLink, otpFlow);
            otpInfo.setOtpPartnerKey(adapterPartnerKey);// partnerkey for clo
            otpInfo.setWebViewLink(adapterResponse.getWebViewLink());
            String partnerApplicationId = adapterResponse.getPartnerApplicationId();
            if (Utils.isNotEmpty(partnerApplicationId)) {
                applicationData.setPartnerApplicationId(partnerApplicationId);
            }
        }
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
        otpInfo.setCurrentTimesGenerate(otpInfo.getCurrentTimesGenerate() + 1);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
        otpInfo.setLastTimeGenerateOtpInMillis(System.currentTimeMillis());

        setStatusApplication(jobData, applicationData, adapterResponse);
    }

    private void setStatusApplication(OnboardingData<T, R> jobData, ApplicationData applicationData, GenerateOtpAdapterResponse adapterResponse) throws BaseException, Exception, ValidatorException {
        ServiceObInfo serviceObInfo = this.onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        PartnerConfig partnerConfig = serviceObInfo.getPartnerMap().get(jobData.getPartnerId());
        if (partnerConfig != null && partnerConfig.isApplyStatusAtFinalSubmit()) {
            Log.MAIN.info("APPLY PARTNER STATUS AT FINAL SUBMIT FOR PARTNER:{}||APPLICATION:{}", partnerConfig.getId(), applicationData.getApplicationId());
            if (adapterResponse.getPartnerStatus() == null) {
                Log.MAIN.fatal("CRITICAL ADAPTER RESPONSE MISSING PARTNER STATUS FOR PARTNER:{}||APPLICATION:{}", partnerConfig.getId(), applicationData.getApplicationId());
                throw new BaseException(OnboardingErrorCode.ADAPTER_RESPONSE_ERROR);
            }
            applicationData.setStatus(adapterResponse.getPartnerStatus());
            applicationData.setState(adapterResponse.getPartnerStatus().getState());
            return;
        }
        applicationData.setStatus(ApplicationStatus.GENERATED_OTP);
        applicationData.setState(ApplicationStatus.GENERATED_OTP.getState());
    }

    @Override
    protected void processNotInGenerateOtpFlow(ApplicationData applicationData, GenerateOtpAdapterResponse adapterDefaultResponse) {
        applicationData.setStatus(ApplicationStatus.ACCEPTED_BY_LENDER);
        applicationData.setState(ApplicationStatus.ACCEPTED_BY_LENDER.getState());
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
        if (Utils.isNotEmpty(adapterDefaultResponse) && Utils.isNotEmpty(adapterDefaultResponse.getPartnerStatus())) {
            applicationData.setStatus(adapterDefaultResponse.getPartnerStatus());
            applicationData.setState(adapterDefaultResponse.getPartnerStatus().getState());
        }
    }

}
