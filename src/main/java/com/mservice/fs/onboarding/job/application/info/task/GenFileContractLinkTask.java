package com.mservice.fs.onboarding.job.application.info.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.AttachFileConfig;
import com.mservice.fs.onboarding.config.ContractConfig;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ContractType;
import com.mservice.fs.onboarding.model.FileContractLink;
import com.mservice.fs.onboarding.model.FileVersion;
import com.mservice.fs.onboarding.model.api.application.ApplicationRequest;
import com.mservice.fs.onboarding.model.api.application.ApplicationResponse;
import com.mservice.fs.onboarding.model.verifyotp.AttachFile;
import com.mservice.fs.onboarding.model.verifyotp.QueueContractRequest;
import com.mservice.fs.onboarding.model.verifyotp.QueueGenLinkResponse;
import com.mservice.fs.processor.JobData;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.rabbit.sending.QueueTask;
import com.mservice.fs.rabbit.sending.RabbitSendingService;
import com.mservice.fs.utils.JsonUtil;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenFileContractLinkTask extends QueueTask<OnboardingData<ApplicationRequest, ApplicationResponse>, ApplicationRequest, ApplicationResponse, OnboardingConfig> {

    public static final TaskName NAME = () -> "GENERATE_LINK_CONTRACT";

    @Autowire(name = "GetContract")
    private RabbitSendingService genContractLinkService;

    public GenFileContractLinkTask() {
        super(NAME);
    }

    @Override
    protected void processWithResponse(OnboardingData<ApplicationRequest, ApplicationResponse> onboardingData, TaskData taskData, byte[] bytes) throws ValidatorException, BaseException, Exception {
        QueueGenLinkResponse queueResponse = JsonUtil.fromByteArray(bytes, QueueGenLinkResponse.class);
        Log.MAIN.info("Receive response Genlink from queue: {}", Json.encode(queueResponse));
        List<QueueGenLinkResponse.LinkFileData> linkFileDatas = queueResponse.getAttachFiles();
        Integer resultCode = queueResponse.getResultCode();
        if (CommonErrorCode.SUCCESS.getCode().equals(resultCode)) {
            Log.MAIN.info("Gen link contract success with resultCode {} or attachFiles {}", resultCode, queueResponse.getAttachFiles());

            ApplicationData applicationData = onboardingData.getTaskData(GetApplicationTask.NAME).getContent();
            Map<ContractType, FileContractLink> applicationContractMap = applicationData.getFileContractData();

            for (QueueGenLinkResponse.LinkFileData linkFileData : linkFileDatas) {
                for (Map.Entry<ContractType, FileContractLink> entry : applicationContractMap.entrySet()) {
                    if (linkFileData.getPath().equals(entry.getValue().getPath())) {
                        FileContractLink fileContractLink = entry.getValue();
                        fileContractLink.setLink(linkFileData.getLink());
                        fileContractLink.setExpiredTime(linkFileData.getExpiredTime());
                    }
                }
            }

            taskData.setContent(linkFileDatas);
            ApplicationResponse response = new ApplicationResponse();
            response.setApplicationData(applicationData);
            response.setResultCode(CommonErrorCode.SUCCESS);
            onboardingData.setResponse(response);
        } else {
            Log.MAIN.fatal("Gen link contract fail with resultCode {} or attachFiles is empty {}", resultCode, queueResponse.getAttachFiles());
            throw new BaseException(OnboardingErrorCode.STORE_CONTRACT_QUEUE_FAIL);
        }
        finish(onboardingData, taskData);
    }

    @Override
    protected void processWithLateResponse(OnboardingData<ApplicationRequest, ApplicationResponse> applicationRequestApplicationResponseOnboardingData, TaskData taskData, byte[] bytes) throws ValidatorException, BaseException, Exception {
        Log.MAIN.info("[StoreContractQueueTask] Received late response: [{}]", new String(bytes));
    }

    @Override
    protected void processTimeout(OnboardingData<ApplicationRequest, ApplicationResponse> onboardingData, TaskData taskData) throws ValidatorException, BaseException, Exception {
        Log.MAIN.error("[StoreContractQueueTask] Timeout when execute call store queue contract do finished !!!");
        this.timeout(onboardingData, taskData);
    }

    @Override
    protected void error(OnboardingData<ApplicationRequest, ApplicationResponse> onboardingData, TaskData taskData, Throwable throwable) throws ValidatorException, BaseException, Exception {
        Log.MAIN.error("[StoreContractQueueTask] Error when execute call store queue contract do finished:", throwable);
        this.error(onboardingData, taskData);
    }

    @Override
    protected String createQueueRequest(OnboardingData<ApplicationRequest, ApplicationResponse> onboardingData, ApplicationRequest applicationRequest) throws ValidatorException, BaseException, Exception {
        QueueContractRequest request = new QueueContractRequest();
        request.setRequestId(onboardingData.getRequestId());
        ContractConfig contractConfig = getConfig().getContractConfig();
        AttachFileConfig attachFileGenLinkConfig = contractConfig.getGenLinkPDF();
        request.setPartnerId(attachFileGenLinkConfig.getPartnerId());
        request.setModuleName(getResource().getModuleName());
        request.setWalletId(onboardingData.getInitiator());
        ApplicationData applicationData = onboardingData.getTaskData(GetApplicationTask.NAME).getContent();
        Map<ContractType, FileContractLink> fileContractLinkMap = applicationData.getFileContractData();

        List<AttachFile> attachFileList = new ArrayList<>();
        for (FileContractLink fileContractLink : fileContractLinkMap.values()) {

            AttachFile attachFile = new AttachFile();
            attachFile.setPath(fileContractLink.getPath());
            attachFile.setExt(attachFileGenLinkConfig.getExt());
            attachFile.setContentType(attachFileGenLinkConfig.getContentType());
            attachFileList.add(attachFile);

            if (FileVersion.OLD.equals(fileContractLink.getVersion())) {
                request.setFromOldServer(Boolean.TRUE);
            }
        }
        request.setAttachFiles(attachFileList);
        return Json.encode(request);
    }

    @Override
    protected Map<String, Object> createRequestHeaders(JobData<ApplicationRequest, ApplicationResponse> jobData) {
        Map<String, Object> headers = super.createRequestHeaders(jobData);
        ContractConfig contractConfig = getConfig().getContractConfig();
        AttachFileConfig attachFileGenLinkConfig = contractConfig.getGenLinkPDF();
        headers.put(Base.PROCESS_NAME_FIELD, attachFileGenLinkConfig.getProcessName());
        headers.put(Base.SERVICE_ID_FIELD_NAME, jobData.getServiceId());
        headers.put(Base.PARTNER_ID_FIELD_NAME, jobData.getPartnerId());
        return headers;
    }

    @Override
    protected boolean isValidCondition(OnboardingData<ApplicationRequest, ApplicationResponse> jobData, TaskData taskData) throws Exception, BaseException, ValidatorException {
        ContractConfig contractConfig = getConfig().getContractConfig();
        if (Boolean.FALSE.equals(contractConfig.isActive())) {
            Log.MAIN.info("Gen link is not Active with config isActive {}", contractConfig.isActive());
            return Boolean.FALSE;
        }

        ApplicationData applicationData = jobData.getTaskData(GetApplicationTask.NAME).getContent();
        Map<ContractType, FileContractLink> fileContractLinkMap = applicationData.getFileContractData();
        if (Utils.isEmpty(fileContractLinkMap)) {
            Log.MAIN.info("ApplicationId {} does not have contract => not gen link", applicationData.getApplicationId());
            return Boolean.FALSE;
        }

        long currentTimeMillis = System.currentTimeMillis();
        long expirationPeriod = contractConfig.getExpirationPeriod();
        long validTimeInMillis = currentTimeMillis + expirationPeriod;
        for (FileContractLink fileContractLink : fileContractLinkMap.values()) {
            long expiredTimeContract = fileContractLink.getExpiredTime();
            if (validTimeInMillis < expiredTimeContract) {
                Log.MAIN.info("ApplicationId {} - validTimeInMillis < expiredTimeContract ({} + {} = {} < {}) have link contract still valid time => not gen link ", applicationData.getApplicationId(), currentTimeMillis, expirationPeriod, validTimeInMillis, expiredTimeContract);
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }

    @Override
    protected RabbitSendingService getRabbitSendingService() {
        return genContractLinkService;
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.TASK_TIMEOUT;
    }

    @Override
    protected ErrorCode getRuntimeErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }
}
