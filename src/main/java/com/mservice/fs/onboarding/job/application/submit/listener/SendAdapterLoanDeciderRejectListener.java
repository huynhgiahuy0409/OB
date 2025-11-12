package com.mservice.fs.onboarding.job.application.submit.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSendAdapterListener;
import com.mservice.fs.onboarding.job.application.submit.task.ApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.api.submit.SubmitAdapterRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.utils.JsonUtil;

/**
 * @author muoi.nong
 */

public class SendAdapterLoanDeciderRejectListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingSendAdapterListener<T, R, GenerateOtpAdapterResponse> {

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected String createAdapterRequest(OnboardingData<T, R> jobData) throws Exception {
        ApplicationForm applicationForm = jobData.getTaskData(ApplicationTask.NAME).getContent();
        SubmitAdapterRequest submitAdapterRequest = new SubmitAdapterRequest();
        submitAdapterRequest.setRequestId(jobData.getRequestId());
        submitAdapterRequest.setApplicationData(applicationForm.getApplicationData());

        Log.MAIN.info("Request send to ADAPTER LISTENER [{}]", submitAdapterRequest);
        return JsonUtil.toString(submitAdapterRequest);
    }

    @Override
    protected void processAdapterResponse(OnboardingData<T, R> trOnboardingData, GenerateOtpAdapterResponse generateOtpAdapterResponse) {

    }

    @Override
    protected boolean isAllowedSendAdapter(OnboardingData<T, R> jobData) throws BaseException, ValidatorException, Exception {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        return serviceObInfo.isMatchAction(Action.SEND_ADAPTER_LISTENER, jobData.getProcessName())
                && OnboardingErrorCode.LOAN_DECIDER_REJECT.getCode().equals(jobData.getResponse().getResultCode());
    }
}
