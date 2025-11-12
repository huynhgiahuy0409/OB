package com.mservice.fs.onboarding.job.verifyotp.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.grpc.proxy.ProxyGrpcRouting;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSendPlatformListener;
import com.mservice.fs.onboarding.job.generateotp.task.ModifiedCacheDataTask;
import com.mservice.fs.onboarding.job.verifyotp.task.SendAdapterTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.api.otp.verify.PlatformOnboardingRequest;
import com.mservice.fs.onboarding.model.api.otp.verify.VerifyOtpAdapterResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpRequest;
import com.mservice.fs.onboarding.model.verifyotp.VerifyOtpResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

public class SendPlatformListener extends OnboardingSendPlatformListener<VerifyOtpRequest, VerifyOtpResponse> {

    @Autowire
    private ProxyGrpcRouting proxyGrpcRouting;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected ProxyGrpcClient getProxyGrpcClient(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        return proxyGrpcRouting.get(jobData.getServiceId());
    }

    @Override
    public void execute(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) throws Throwable {
        if (jobData.isLoadFromCache()) {
            Log.MAIN.info("Response from cache - skip listener");
            return;
        }
        VerifyOtpResponse response = jobData.getResponse();
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if ((response.getResultCode().equals(CommonErrorCode.SUCCESS.getCode())
                && serviceObInfo.isMatchAction(Action.SEND_ADAPTER_LISTENER, jobData.getProcessName())) ||
                serviceObInfo.isMatchAction(Action.SEND_PLATFORM_LISTENER, jobData.getProcessName())
        ) {
            super.execute(jobData);
        }

    }

    @Override
    protected byte[] createCrossPlatformRequest(OnboardingData<VerifyOtpRequest, VerifyOtpResponse> jobData) {
        ApplicationForm applicationForm = jobData.getTaskData(ModifiedCacheDataTask.NAME).getContent();
        ApplicationData applicationData = Utils.isNotEmpty(applicationForm) ? applicationForm.getApplicationData() : null;

        TaskData adapterResponseTask = jobData.getTaskData(SendAdapterTask.NAME);

        PlatformOnboardingRequest adapterRequest = new PlatformOnboardingRequest();
        adapterRequest.setRequestId(jobData.getRequestId());
        adapterRequest.setApplicationData(applicationData);
        adapterRequest.setPartnerApplicationId(Utils.isNotEmpty(applicationData) ? applicationData.getPartnerApplicationId() : CommonConstant.STRING_EMPTY);
        if (Utils.isNotEmpty(adapterResponseTask) && Utils.isNotEmpty(adapterResponseTask.getContent())) {

            VerifyOtpAdapterResponse adapterResponse = adapterResponseTask.getContent();
            adapterRequest.setRawPartnerRequest(adapterResponse.getRawPartnerRequest());
            adapterRequest.setRawPartnerResponse(adapterResponse.getRawPartnerResponse());
            adapterRequest.setPartnerResultCode(adapterResponse.getPartnerResultCode());
            adapterRequest.setRawAdapterResponse(Json.encode(adapterResponse));
        }
        adapterRequest.setResultCode(jobData.getResponse().getResultCode());

        Log.MAIN.info("Request send to ADAPTER [{}]", adapterRequest);
        return adapterRequest.toByteArrays();
    }

    @Override
    protected void createCrossPlatformResponse(String s) throws Exception {
        Log.MAIN.info("Adapter response: [{}]", s);
    }
}
