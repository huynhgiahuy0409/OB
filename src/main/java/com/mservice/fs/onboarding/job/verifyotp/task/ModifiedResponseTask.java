package com.mservice.fs.onboarding.job.verifyotp.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.ApplicationStatus;
import com.mservice.fs.onboarding.model.OtpInfo;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.OtpConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.Utils;

public class ModifiedResponseTask extends OnboardingTask<VerifyOtpRequest, VerifyOtpResponse> {

    public static final TaskName NAME = () -> "MODIFY_RESPONSE";
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public ModifiedResponseTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws BaseException, Exception, ValidatorException {
        OtpConfig otpConfigInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId())
                .getPartnerConfig(jobData.getPartnerId()).getOtpConfig();

        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = applicationForm.getApplicationData();
        OtpInfo otpInfo = applicationData.getOtpInfo();

        VerifyOtpResponse response = new VerifyOtpResponse();
        VerifyOtpAdapterResponse adapterResponseTask = jobData.getTaskData(SendAdapterTask.NAME).getContent();

        setDataResponse(response, otpInfo, otpConfigInfo, applicationData, adapterResponseTask);

        jobData.setResponse(response);
        finish(jobData, taskData);
    }

    protected void setDataResponse(VerifyOtpResponse response, OtpInfo otpInfo, OtpConfig otpConfigInfo, ApplicationData applicationData, VerifyOtpAdapterResponse adapterResponseTask) {
        int maxGenerateOtpTimes = otpConfigInfo.getMaxGenerateOtpTimes();
        int maxVerifyOtpTimes = otpConfigInfo.getMaxVerifyOtpTimes();
        int currentVerifyTimes = otpInfo.getCurrentTimesVerify() + 1;
        int remainingVerifyTimes = maxVerifyOtpTimes - currentVerifyTimes;

        otpInfo.setCurrentTimesVerify(currentVerifyTimes);
        otpInfo.setLastModifiedTimeInMillis(System.currentTimeMillis());

        applicationData.setStatus(ApplicationStatus.VERIFIED_OTP_SUCCESS);
        applicationData.setState(ApplicationStatus.VERIFIED_OTP_SUCCESS.getState());
        applicationData.setReasonMessage(adapterResponseTask.getReasonMessage());

        if (Utils.isNotEmpty(adapterResponseTask.getPartnerApplicationId())) {
            Log.MAIN.info("Partner applicationId is not null", adapterResponseTask.getPartnerApplicationId());
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
