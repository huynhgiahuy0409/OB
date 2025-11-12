package com.mservice.fs.onboarding.job;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.grpc.proxy.ProxyGrpcRouting;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.PlatformResponse;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.model.api.otp.verify.PlatformOnboardingRequest;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.PartnerConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.task.SendingPlatformTask;
import com.mservice.fs.utils.Generics;
import com.mservice.fs.utils.JsonUtil;

/**
 * @author phat.duong
 * on 10/3/2025
 **/
public abstract class SendApplicationPlatformTask<T extends OnboardingRequest, R extends OnboardingResponse> extends SendingPlatformTask<OnboardingData<T, R>, T, R, OnboardingConfig> {
    public static final TaskName NAME = () -> "SEND_PLATFORM";
    private final Class<?> responseClass;

    public SendApplicationPlatformTask() {
        super(NAME);
        this.responseClass = Generics.getTypeParameter(getClass(), OnboardingResponse.class);
    }

    @Autowire
    private ProxyGrpcRouting proxyGrpcRouting;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected boolean isActive(OnboardingData<T, R> platformData) {
        try {
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(platformData.getServiceId());
            PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(platformData.getPartnerId());
            return serviceObInfo.isMatchAction(Action.SEND_PLATFORM_TASK, platformData.getProcessName()) && partnerConfig.isApplySendPlatformTask();
        } catch (BaseException | Exception | ValidatorException e) {
            Log.MAIN.error("Can't get service info");
            return false;
        }
    }

    @Override
    protected ProxyGrpcClient getProxyGrpcClient(OnboardingData<T, R> trOnboardingData) {
        return proxyGrpcRouting.get(trOnboardingData.getServiceId());
    }

    @Override
    protected byte[] createCrossPlatformRequest(OnboardingData<T, R> trOnboardingData) {
        PlatformOnboardingRequest platformOnboardingRequest = new PlatformOnboardingRequest();
        platformOnboardingRequest.setRequestId(trOnboardingData.getRequestId());
        platformOnboardingRequest.setApplicationData(getApplicationData(trOnboardingData));

        Log.MAIN.info("Request send to platform [{}]", platformOnboardingRequest);
        return platformOnboardingRequest.toByteArrays();
    }

    @Override
    protected PlatformResponse createCrossPlatformResponse(OnboardingData<T, R> trOnboardingData, String s) throws Exception {
        ApplicationResponse partnerResponse = JsonUtil.fromString(s, ApplicationResponse.class);
        if (!CommonErrorCode.SUCCESS.getCode().equals(partnerResponse.getResultCode())) {
            R response = (R) Generics.createObject(responseClass);
            response.setResultCode(partnerResponse.getResultCode());
            response.setResultMessage(partnerResponse.getResultMessage());
            trOnboardingData.setResponse(response);
        }
        return partnerResponse;
    }

    protected abstract ApplicationData getApplicationData(OnboardingData<T, R> trOnboardingData);
}
