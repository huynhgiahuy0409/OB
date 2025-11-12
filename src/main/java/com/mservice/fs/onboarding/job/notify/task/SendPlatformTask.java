package com.mservice.fs.onboarding.job.notify.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.grpc.proxy.ProxyGrpcClient;
import com.mservice.fs.grpc.proxy.ProxyGrpcRouting;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.PlatformResponse;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.OnboardingNotifyRequest;
import com.mservice.fs.onboarding.model.OnboardingNotifyResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserRequest;
import com.mservice.fs.onboarding.model.notifyuser.NotifyUserResponse;
import com.mservice.fs.task.SendingPlatformTask;
import com.mservice.fs.utils.IdProvider;
import com.mservice.fs.utils.JsonUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class SendPlatformTask extends SendingPlatformTask<PlatformData<NotifyUserRequest, NotifyUserResponse>, NotifyUserRequest, NotifyUserResponse, OnboardingConfig> {

    public static final TaskName NAME = () -> "SEND_PLATFORM_TASK";

    @Autowire
    private ProxyGrpcRouting proxyGrpcRouting;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;

    public SendPlatformTask() {
        super(NAME);
    }

    @Override
    protected ProxyGrpcClient getProxyGrpcClient(PlatformData<NotifyUserRequest, NotifyUserResponse> platformData) {
        return proxyGrpcRouting.get(platformData.getServiceId());
    }

    @Override
    protected byte[] createCrossPlatformRequest(PlatformData<NotifyUserRequest, NotifyUserResponse> platformData) {
        OnboardingNotifyRequest request = new OnboardingNotifyRequest();
        ApplicationForm applicationForm = platformData.getTaskData(ValidateTask.NAME).getContent();
        request.setRequestId(IdProvider.next());
        request.setApplicationForm(applicationForm);
        request.setDayGap(calculateDayGap(applicationForm));
        Log.MAIN.info("Request to platform: {}", request);
        return request.toByteArrays();
    }

    private long calculateDayGap(ApplicationForm applicationForm) {
        ApplicationData applicationData = applicationForm.getApplicationData();
        long createDate = applicationData.getCreatedDate();
        LocalDate localDate = Instant.ofEpochMilli(createDate).atZone(ZoneId.systemDefault()).toLocalDate();
        return ChronoUnit.DAYS.between(localDate, LocalDate.now());
    }

    @Override
    protected PlatformResponse createCrossPlatformResponse(PlatformData<NotifyUserRequest, NotifyUserResponse> platformData, String payload) throws Exception {
        return JsonUtil.fromString(payload, OnboardingNotifyResponse.class);
    }
}
