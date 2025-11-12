package com.mservice.fs.onboarding.job.application.submit;

import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSubmitTask;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.job.application.submit.task.PaymentInfoTask;
import com.mservice.fs.onboarding.job.application.submit.task.SocialSellerDataTask;
import com.mservice.fs.onboarding.model.*;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.application.confirm.ConfirmRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;

/**
 * @author hoang.thai
 * on 11/22/2023
 */
public class FMOBSendAdapterTask<T extends ConfirmRequest, R extends SubmitResponse> extends OnboardingSubmitTask<T, R> {

    @Override
    protected ApplicationForm getApplicationForm(OnboardingData<T, R> jobData) throws BaseException, ValidatorException, Exception {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        //set applicationStatus
        ApplicationData applicationData = applicationForm.getApplicationData();
        //create otp info
        OtpConfig otpConfig = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId())
                .getPartnerConfig(jobData.getPartnerId()).getOtpConfig();

        OtpInfo otpInfo = new OtpInfo();
        otpInfo.setMaxGenerateTimes(otpConfig.getMaxGenerateOtpTimes());
        otpInfo.setMaxVerifyTimes(otpConfig.getMaxGenerateOtpTimes());
        otpInfo.setCurrentTimesGenerate(DEFAULT_TIME_GENERATE_OTP);
        otpInfo.setCurrentTimesVerify(DEFAULT_CURRENT_TIMES_VERIFY_OTP);
        otpInfo.setValidOtpInMillis(otpConfig.getValidOtpInMillis());
        applicationForm.getApplicationData().setOtpInfo(otpInfo);
        return applicationForm;
    }

    @Override
    protected String getSocialSellerData(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(SocialSellerDataTask.NAME).getContent();
    }

    @Override
    protected String getPaymentInfo(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(PaymentInfoTask.NAME).getContent();
    }

    @Override
    protected void processInGenerateOtpFlow(OnboardingData<T, R> jobData, ApplicationData applicationData, GenerateOtpAdapterResponse adapterResponse) {
        String adapterPartnerKey = adapterResponse.getOtpPartnerKey();
        String webViewLink = adapterResponse.getWebViewLink();
        boolean otpFlow = adapterResponse.isGenerateOtp();
        OtpInfo otpInfo = applicationData.getOtpInfo();
        applicationData.setStatus(ApplicationStatus.GENERATED_OTP);
        applicationData.setState(ApplicationStatus.GENERATED_OTP.getState());
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
        Log.MAIN.info("Otp flow partnerKey {} - webViewLink {} - otpFlow {}", adapterPartnerKey, webViewLink, otpFlow);
        otpInfo.setOtpPartnerKey(adapterPartnerKey);// partnerkey for clo
        otpInfo.setCurrentTimesGenerate(otpInfo.getCurrentTimesGenerate() + 1);
        otpInfo.setWebViewLink(adapterResponse.getWebViewLink());
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());
        otpInfo.setLastTimeGenerateOtpInMillis(System.currentTimeMillis());
        applicationData.setPartnerApplicationId(adapterResponse.getPartnerApplicationId());
    }

    @Override
    protected void processNotInGenerateOtpFlow(ApplicationData applicationData, GenerateOtpAdapterResponse adapterDefaultResponse) {
        applicationData.setStatus(ApplicationStatus.ACCEPTED_BY_LENDER);
        applicationData.setState(ApplicationStatus.ACCEPTED_BY_LENDER.getState());
        applicationData.setModifiedDateInMillis(System.currentTimeMillis());
    }
}
