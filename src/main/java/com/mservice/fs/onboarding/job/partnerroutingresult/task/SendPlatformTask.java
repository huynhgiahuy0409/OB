package com.mservice.fs.onboarding.job.partnerroutingresult.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.grpc.proxy.ProxyGrpcRouting;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.api.partnerrouting.NewPackageInfo;
import com.mservice.fs.onboarding.model.api.partnerrouting.PlatformPartnerRoutingRequest;
import com.mservice.fs.onboarding.model.api.partnerrouting.PlatformPartnerRoutingResponse;
import com.mservice.fs.onboarding.model.partnerroutingresult.Decision;
import com.mservice.fs.onboarding.model.partnerroutingresult.LoanDeciderRecord;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingRequest;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingResponse;
import com.mservice.fs.task.SendingPlatformTask;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

/**
 * @author phat.duong
 * on 7/31/2025
 **/
public class SendPlatformTask extends SendingPlatformTask<OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse>, PartnerRoutingRequest, PartnerRoutingResponse, OnboardingConfig> {
    public static final TaskName NAME = () -> "SEND-PLATFORM";
    @Autowire
    private ProxyGrpcRouting proxyGrpcRouting;

    public SendPlatformTask() {
        super(NAME);
    }

    @Override
    protected ProxyGrpcClient getProxyGrpcClient(OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse> onboardingData) {
        return proxyGrpcRouting.get(onboardingData.getServiceId());
    }

    @Override
    protected byte[] createCrossPlatformRequest(OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse> onboardingData) {
        PlatformPartnerRoutingRequest platformRequest = new PlatformPartnerRoutingRequest();
        platformRequest.setRequestId(onboardingData.getRequestId());

        LoanDeciderRecord loanDeciderRecord = onboardingData.getRequest().getLoanDeciderRecord();
        boolean result = onboardingData.getTaskData(UpdateStatusTask.NAME).getContent();
        platformRequest.setDecision(result ? Decision.REAPPLY.name() : Decision.REJECT.name());
        if (Utils.isNotEmpty(loanDeciderRecord) && Utils.isNotEmpty(loanDeciderRecord.getLenderId())) {
            NewPackageInfo newPackageInfo = new NewPackageInfo();
            newPackageInfo.setLenderId(loanDeciderRecord.getLenderId().name());
            platformRequest.setNewPackageInfo(newPackageInfo);
        }

        return platformRequest.toByteArrays();
    }

    @Override
    protected PlatformPartnerRoutingResponse createCrossPlatformResponse(OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse> onboardingData, String s) throws Exception {
        return JsonUtil.fromString(s, PlatformPartnerRoutingResponse.class);
    }
}
