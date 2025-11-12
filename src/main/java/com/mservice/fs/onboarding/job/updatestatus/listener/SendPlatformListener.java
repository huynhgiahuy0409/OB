package com.mservice.fs.onboarding.job.updatestatus.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.grpc.proxy.ProxyGrpcRouting;
import com.mservice.fs.log.Log;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingSendPlatformListener;
import com.mservice.fs.onboarding.model.api.status.PlatformOnboardingRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.UpdatingStatusResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;

/**
 * @author phat.duong
 * on 7/21/2025
 **/
public class SendPlatformListener extends OnboardingSendPlatformListener<UpdatingStatusRequest, UpdatingStatusResponse> {
    @Autowire
    private ProxyGrpcRouting proxyGrpcRouting;
    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    @Override
    protected ProxyGrpcClient getProxyGrpcClient(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> platformData) {
        return proxyGrpcRouting.get(platformData.getServiceId());
    }

    @Override
    public void execute(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> baseData) throws Throwable {
        ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(baseData.getServiceId());
        if (serviceObInfo.isMatchAction(Action.SEND_ADAPTER_LISTENER, baseData.getProcessName())) {
            super.execute(baseData);
        }
    }

    @Override
    protected byte[] createCrossPlatformRequest(OnboardingData<UpdatingStatusRequest, UpdatingStatusResponse> platformData) {
        UpdatingStatusRequest request = platformData.getRequest();
        PlatformOnboardingRequest platformOnboardingRequest = new PlatformOnboardingRequest();
        platformOnboardingRequest.setRequestId(platformData.getRequestId());
        platformOnboardingRequest.setApplicationId(request.getApplicationId());
        platformOnboardingRequest.setPhoneNumber(request.getPhoneNumber());
        platformOnboardingRequest.setReasonId(request.getReasonId());
        platformOnboardingRequest.setNewStatus(request.getStatus());
        platformOnboardingRequest.setRawPartnerRequest(request.getRawPartnerRequest());
        platformOnboardingRequest.setReasonMessage(request.getReasonMessage());
        platformOnboardingRequest.setOnboardingResponse(platformData.getResponse());
        Log.MAIN.info("Request send to PLATFORM [{}]", platformOnboardingRequest);

        return platformOnboardingRequest.toByteArrays();
    }

    @Override
    protected void createCrossPlatformResponse(String payload) throws Exception {
        Log.MAIN.info("PLATFORM response[{}]", payload);

    }
}
