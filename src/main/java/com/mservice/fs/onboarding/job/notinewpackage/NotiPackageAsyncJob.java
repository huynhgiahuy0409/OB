package com.mservice.fs.onboarding.job.notinewpackage;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.notinewpackage.listener.NotifyChangedStatusListener;
import com.mservice.fs.onboarding.job.notinewpackage.task.BuildResponseTask;
import com.mservice.fs.onboarding.job.notinewpackage.task.NotiGetUserProfile;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.notipackage.OnboardingNotiRequest;
import com.mservice.fs.onboarding.model.notipackage.protomodel.GetOfferPackageResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * @author muoi.nong
 */
//@Processor(name = OnboardingProcessor.NOTI_PACKAGE)
public class NotiPackageAsyncJob extends OnboardingJob<OnboardingNotiRequest, OnboardingResponse> {

    public NotiPackageAsyncJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<OnboardingNotiRequest, OnboardingResponse>, OnboardingNotiRequest, OnboardingResponse, OnboardingConfig>> getTaskList() {
        return Arrays.asList(
                new NotiGetUserProfile(),
                new BuildResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<OnboardingNotiRequest, OnboardingResponse>, OnboardingNotiRequest, OnboardingResponse, OnboardingConfig>> getDefaultListeners() {
        return List.of(
                new NotifyChangedStatusListener()
        );
    }

    @Override
    protected OnboardingData<OnboardingNotiRequest, OnboardingResponse> createData(Base base) {
        return new OnboardingData<>(base, this);
    }

    @Override
    protected OnboardingData<OnboardingNotiRequest, OnboardingResponse> initData(OnboardingData<OnboardingNotiRequest, OnboardingResponse> data, Base base) throws ReflectiveOperationException, IOException {
        GetOfferPackageResponse dataRequest = GetOfferPackageResponse.parseFrom(base.getRequest());

        OnboardingNotiRequest request = new OnboardingNotiRequest();
        request.setRequestId(dataRequest.getRequestId());
        request.setAgentId(Utils.isEmpty(dataRequest.getAgentId()) ? null : String.valueOf(dataRequest.getAgentId()));

        base.setInitiatorId(request.getAgentId());
        base.setRequest(JsonUtil.toByteArray(request));

        super.initData(data, base);
        return data;
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<OnboardingNotiRequest, OnboardingResponse> data, OnboardingResponse response) {
        // not set render data for noti-package
    }
}
