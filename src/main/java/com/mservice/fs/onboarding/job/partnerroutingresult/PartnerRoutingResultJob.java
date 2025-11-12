package com.mservice.fs.onboarding.job.partnerroutingresult;

import com.google.protobuf.util.JsonFormat;
import com.mservice.fs.generic.Processor;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.json.Json;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.partnerroutingresult.task.BuildResponseTask;
import com.mservice.fs.onboarding.job.partnerroutingresult.task.SendPlatformTask;
import com.mservice.fs.onboarding.job.partnerroutingresult.task.UpdateStatusTask;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingRequest;
import com.mservice.fs.onboarding.model.partnerroutingresult.PartnerRoutingResponse;
import com.mservice.fs.onboarding.model.partnerroutingresult.protomodel.PlutusLoanDeciderProto;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.utils.JsonUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author phat.duong
 * on 7/31/2025
 **/
@Processor(name = OnboardingProcessor.PARTNER_ROUTING_RESULT)
public class PartnerRoutingResultJob extends OnboardingJob<PartnerRoutingRequest, PartnerRoutingResponse> {

    public PartnerRoutingResultJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse>, PartnerRoutingRequest, PartnerRoutingResponse, OnboardingConfig>> getTaskList() throws Exception {
        return Arrays.asList(
                new UpdateStatusTask(),
                new SendPlatformTask(),
                new BuildResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse>, PartnerRoutingRequest, PartnerRoutingResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of();
    }

    @Override
    protected OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse> initData(OnboardingData<PartnerRoutingRequest, PartnerRoutingResponse> data, Base base) throws ReflectiveOperationException, IOException {
        PlutusLoanDeciderProto.GetLoanDeciderResponse getLoanDeciderResponse = PlutusLoanDeciderProto.GetLoanDeciderResponse.parseFrom(base.getRequest());
        String jsonStr = JsonFormat.printer().print(getLoanDeciderResponse);
        PartnerRoutingRequest partnerRoutingRequest = Json.decodeValue(jsonStr, PartnerRoutingRequest.class);
        base.setRequest(JsonUtil.toByteArray(partnerRoutingRequest));
        super.initData(data, base);
        return data;
    }
}
