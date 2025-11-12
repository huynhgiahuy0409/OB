package com.mservice.fs.onboarding.job.application.info.listener;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.connection.jdbc.UpdateFileContractLinkProcessor;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingListener;
import com.mservice.fs.onboarding.job.application.info.task.GenFileContractLinkTask;
import com.mservice.fs.onboarding.job.application.info.task.GetApplicationTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ContractType;
import com.mservice.fs.onboarding.model.FileContractLink;
import com.mservice.fs.onboarding.model.api.application.ApplicationRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.model.verifyotp.QueueGenLinkResponse;
import com.mservice.fs.utils.Utils;

import java.util.List;
import java.util.Map;

public class UpdateLinkContractListener extends OnboardingListener<ApplicationRequest, ApplicationResponse> {

    private static final String NAME = "UPDATE_LINK_CONTRACT";

    @Autowire(name = "UpdateContractLink")
    private UpdateFileContractLinkProcessor updateFileContractLinkProcessor;

    public UpdateLinkContractListener() {
        super(NAME);
    }

    @Override
    public void execute(OnboardingData<ApplicationRequest, ApplicationResponse> onboardingData) throws Throwable {
        List<QueueGenLinkResponse.LinkFileData> linkFileDatas = onboardingData.getTaskData(GenFileContractLinkTask.NAME).getContent();
        Integer resultCode = onboardingData.getResponse().getResultCode();
        if (CommonErrorCode.SUCCESS.getCode().equals(resultCode)
                && Utils.isNotEmpty(linkFileDatas)
        ) {
            Log.MAIN.info("Update file contract link with resultCode {} and link FileData is not empty {}", resultCode, linkFileDatas);
            ApplicationData applicationData = onboardingData.getTaskData(GetApplicationTask.NAME).getContent();
            Map<ContractType, FileContractLink> fileContractLinkMap = applicationData.getFileContractData();
            for (QueueGenLinkResponse.LinkFileData linkFileData : linkFileDatas) {
                updateFileContractLinkProcessor.execute(applicationData);
            }
        }
    }
}
