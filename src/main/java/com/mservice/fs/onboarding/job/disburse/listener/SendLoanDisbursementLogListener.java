package com.mservice.fs.onboarding.job.disburse.listener;

import com.google.protobuf.ByteString;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.grpc.google.AIService;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.updatestatus.task.UpdatingStatusTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.ai.*;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseRequest;
import com.mservice.fs.onboarding.model.api.disburse.OnboardingDisburseResponse;
import com.mservice.fs.onboarding.model.common.config.Action;
import com.mservice.fs.onboarding.model.common.config.AiConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.utils.Utils;

public class SendLoanDisbursementLogListener extends PushEventChangeStatusToAIListener {
    // topic aiServiceDisburse
    private static final String NAME = "SEND_LOAN_DISBURSEMENT_LOG";

    public SendLoanDisbursementLogListener() {
        super(NAME);
    }

    @Autowire
    private AIService aiServiceDisburse;

    @Override
    protected LoanInfoMessageType getMessageType() {
        return LoanInfoMessageType.LOAN_DISBURSEMENT_LOG;
    }

    @Override
    protected AIService getAiService() {
        return aiServiceDisburse;
    }

}
