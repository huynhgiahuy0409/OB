package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.grpc.proxy.ProxyGrpcRouting;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.otp.generate.GenerateOtpAdapterResponse;
import com.mservice.fs.onboarding.model.api.otp.verify.PlatformOnboardingRequest;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

public abstract class SendApplicationDataPlatformListener<T extends OnboardingRequest, R extends OnboardingResponse> extends OnboardingSendPlatformListener<T, R> {

    @Autowire
    private ProxyGrpcRouting proxyGrpcRouting;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected ProxyGrpcClient getProxyGrpcClient(OnboardingData<T, R> jobData) {
        return proxyGrpcRouting.get(jobData.getServiceId());
    }

    @Override
    public void execute(OnboardingData<T, R> jobData) throws Throwable {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(jobData.getServiceId());
        if (serviceObInfo.isMatchAction(Action.SEND_PLATFORM_LISTENER, jobData.getProcessName())
                || isValidateAction(jobData)) {
            super.execute(jobData);
        }

    }

    protected boolean isValidateAction(OnboardingData<T, R> jobData) throws BaseException, ValidatorException, Exception {
        return false;
    }

    @Override
    protected byte[] createCrossPlatformRequest(OnboardingData<T, R> jobData) throws Exception {
        ApplicationData applicationData = getApplicationData(jobData);

        TaskData taskData = getTaskDataSendAdapter(jobData);

        PlatformOnboardingRequest platformOnboardingRequest = new PlatformOnboardingRequest();
        platformOnboardingRequest.setRequestId(jobData.getRequestId());
        platformOnboardingRequest.setApplicationData(applicationData);
        platformOnboardingRequest.setPartnerApplicationId(Utils.isNotEmpty(applicationData) ? applicationData.getPartnerApplicationId() : CommonConstant.STRING_EMPTY);

        Log.MAIN.info("Task content [{}]", Json.encode(taskData));
        if (Utils.isNotEmpty(taskData) && Utils.isNotEmpty(taskData.getContent())) {

            GenerateOtpAdapterResponse adapterResponse = taskData.getContent();
            platformOnboardingRequest.setRawPartnerRequest(adapterResponse.getRawPartnerRequest());
            platformOnboardingRequest.setRawPartnerResponse(adapterResponse.getRawPartnerResponse());
            platformOnboardingRequest.setPartnerResultCode(adapterResponse.getPartnerResultCode());
            platformOnboardingRequest.setRawAdapterResponse(Json.encode(adapterResponse));
        }

        platformOnboardingRequest.setResultCode(jobData.getResponse().getResultCode());
        Log.MAIN.info("Request send to platform [{}]", platformOnboardingRequest);
        return platformOnboardingRequest.toByteArrays();
    }

    @Override
    protected void createCrossPlatformResponse(String s) throws Exception {
        Log.MAIN.info("Platform response: [{}]", s);
    }

    protected abstract ApplicationData getApplicationData(OnboardingData<T, R> jobData) throws Exception;

    protected TaskData getTaskDataSendAdapter(OnboardingData<T, R> jobData) {
        return jobData.getTaskData(OnboardingSendAdapterTask.NAME);
    }
}
