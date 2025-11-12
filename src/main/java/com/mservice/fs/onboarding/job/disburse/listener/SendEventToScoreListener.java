package com.mservice.fs.onboarding.job.disburse.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.kafka.producer.impl.ProducerService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseRequest;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.model.disburse.ApplicationDisbursedData;
import com.mservice.fs.onboarding.model.disburse.ScoreDisburseRequest;
import com.mservice.fs.onboarding.service.GetDisbursedApplicationProcessor;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SendEventToScoreListener extends OnboardingListener<OnboardingDisburseRequest, OnboardingDisburseResponse> {

    @Autowire
    private ProducerService kafkaScoreService;

    @Autowire
    private GetDisbursedApplicationProcessor disbursedApplicationProcessor;

    @Autowire(name = "ServiceConfigInfo")
    private DataService<ServiceObConfig> onboardingDataInfo;


    public SendEventToScoreListener() {
        super("SEND_EVENT_TO_SCORE");
    }

    @Override
    public void execute(OnboardingData<OnboardingDisburseRequest, OnboardingDisburseResponse> onboardingData) throws Throwable {
        if (onboardingData.getResult().getCode().equals(CommonErrorCode.SUCCESS.getCode())) {
            ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(onboardingData.getServiceId());
            OnboardingDisburseRequest request = onboardingData.getRequest();
            List<ApplicationDisbursedData> applicationDisbursedData = disbursedApplicationProcessor.execute(request.getPhoneNumber());

            ScoreDisburseRequest scoreDisburseRequest = new ScoreDisburseRequest();
            scoreDisburseRequest.setUser(request.getPhoneNumber());
            scoreDisburseRequest.setCmdId(Utils.getAutoGenerateID());
            scoreDisburseRequest.setMsgType(serviceObInfo.getScoreMsgTypeRisk());

            ScoreDisburseRequest.Data data = getData(applicationDisbursedData, serviceObInfo);
            scoreDisburseRequest.setData(data);
            String dataReq = scoreDisburseRequest.encode();
            Log.MAIN.info("REQUEST SEND SCORE: {}", dataReq);
            kafkaScoreService.produce(CommonConstant.STRING_EMPTY, dataReq.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static ScoreDisburseRequest.Data getData(List<ApplicationDisbursedData> applicationDisbursedData, ServiceObInfo serviceObInfo) {
        ApplicationDisbursedData currentApp = applicationDisbursedData.getLast();
        ScoreDisburseRequest.Data data = new ScoreDisburseRequest.Data();
        data.setAmount(currentApp.getLoanAmount());
        data.setServiceId(serviceObInfo.getScoreServiceIdRisk());
        data.setDisbursementSuccessTimes(applicationDisbursedData.size());
        if (applicationDisbursedData.size() > 1) {
            ApplicationDisbursedData lastApp = applicationDisbursedData.get(applicationDisbursedData.size() - 2);
            data.setLastDisbursementTime(lastApp.getCreateTime().getTime());
        }
        return data;
    }
}
