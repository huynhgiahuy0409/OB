package com.mservice.fs.onboarding.job.contract.store.task;

import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.config.AttachFileConfig;
import com.mservice.fs.onboarding.config.ContractConfig;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.common.config.*;
import com.mservice.fs.onboarding.model.contract.StoreContractRequest;
import com.mservice.fs.onboarding.model.contract.StoreContractResponse;
import com.mservice.fs.onboarding.model.verifyotp.AttachFile;
import com.mservice.fs.onboarding.model.verifyotp.ContractData;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.template.TemplateMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerateContractTask extends OnboardingTask<StoreContractRequest, StoreContractResponse> {

    public static final TaskName NAME = () -> "GENERATE_CONTRACT";
    private static final String CONTRACT_TEMPLATE_PREFIX = "CONTRACT_TEMPLATE";
    private static final String ZERO_INTEREST = "ZERO_INTEREST";
    private static final String CONTRACT_DATA = "contractData";

    @Autowire(name = "ServiceConfigInfo")
    DataService<ServiceObConfig> onboardingDataInfo;

    @Autowire(name = "PackageDataService")
    private DataService<PackageInfoConfig> packageDataCreator;

    @Autowire
    private TemplateMessage templateMessage;


    public GenerateContractTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        String serviceId = onboardingData.getServiceId();
        String partnerId = onboardingData.getPartnerId();

        ServiceObConfig serviceObConfig = onboardingDataInfo.getData();
        ServiceObInfo serviceObInfo = serviceObConfig.getServiceObInfo(serviceId);
        PartnerConfig partnerConfig = serviceObInfo.getPartnerConfig(partnerId);
        if (partnerConfig == null) {
            Log.MAIN.error("ServiceId {} partnerId {} partnerConfig is null", serviceId, partnerId);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }

        List<AttachFile> attachFiles = createListAttachFile(serviceObInfo, partnerConfig, onboardingData);

        taskData.setContent(attachFiles);
        finish(onboardingData, taskData);
    }

    private List<AttachFile> createListAttachFile(ServiceObInfo serviceObInfo, PartnerConfig partnerConfig, OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws BaseException, Exception, ValidatorException {
        String partnerId = onboardingData.getPartnerId();

        Set<ContractType> contractTypes = partnerConfig.getTypeContracts();
        Map<String, Object> templateMap = createTemplateMap(onboardingData);

        ApplicationData applicationData = onboardingData.getRequest().getApplicationData();
        String applicationId = applicationData.getApplicationId();

        ContractConfig contractConfig = getConfig().getContractConfig();
        AttachFileConfig attachFileConfig = contractConfig.getStoreFilePDF();

        List<AttachFile> attachFiles = new ArrayList<>();
        for (ContractType type : contractTypes) {
            String typeName = type.getName();
            String templateName = getTemplateName(partnerId, type, serviceObInfo, applicationData);
            Log.MAIN.info("Template name : [{}]", templateName);
            String content = loadContent(templateName, templateMap, onboardingData);
            AttachFile attachFile = new AttachFile();
            attachFile.setBase64(OnboardingUtils.createPDFBased64(content, typeName, partnerId));
            attachFile.setName(String.format("%s%s%s", applicationId, Constant.STRING_JOIN_NAME_AND_TYPE_CONTRACT, typeName));
            attachFile.setExt(attachFileConfig.getExt());
            attachFile.setContentType(attachFileConfig.getContentType());
            attachFile.setForceDownload(Boolean.TRUE); //download when click link

            attachFiles.add(attachFile);
        }

        return attachFiles;
    }

    private Map<String, Object> createTemplateMap(OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) {
        ApplicationData applicationData = onboardingData.getRequest().getApplicationData();
        ContractData contractData = new ContractData();
        LocalDateTime currentDate = LocalDateTime.now();
        contractData.setDay(String.valueOf(currentDate.getDayOfMonth()));
        contractData.setMonth(String.valueOf(currentDate.getMonth().getValue()));
        contractData.setYear(String.valueOf(currentDate.getYear()));
        contractData.setHour(String.valueOf(currentDate.getHour()));
        contractData.setMinute(String.valueOf(currentDate.getMinute()));
        contractData.setSecond(String.valueOf(currentDate.getSecond()));
        contractData.setOtp(onboardingData.getRequest().getOtp());
        applicationData.getCurrentAddress().setFullAddress(escapeXml(applicationData.getCurrentAddress().getFullAddress()));
        contractData.setApplicationData(applicationData);
        contractData.setInterestRate(OnboardingUtils.convertInterestToString(applicationData.getChosenPackage().getInterest()));
        onboardingData.getTemplateModel().put(CONTRACT_DATA, contractData);
        return onboardingData.getTemplateModel();
    }

    public String loadContent(String templateName, Map<String, Object> templateMap, OnboardingData<StoreContractRequest, StoreContractResponse> onboardingData) throws Exception, BaseException {
        try {
            return templateMessage.process(templateName, templateMap);
        } catch (Exception e) {
            Log.MAIN.error("Error when load content with serviceId {} partnerId {} templateName {}", onboardingData.getServiceId(), onboardingData.getPartnerId(), templateName, e);
            throw new BaseException(CommonErrorCode.SYSTEM_BUG);
        }
    }

    public String getTemplateName(String partnerId, ContractType contractType, ServiceObInfo serviceObInfo, ApplicationData applicationData) throws BaseException, ValidatorException, Exception {
        String typeContract = contractType.getName();
        PackageInfo chosenPackage = applicationData.getChosenPackage();
        if (serviceObInfo.isApplyZeroInterest() && OnboardingUtils.isZeroInterestPackage(chosenPackage) && contractType.isApplyZeroInterest()) {
            Log.MAIN.info("Apply zero interest for contract [{}] packageCode [{}]", applicationData.getApplicationId(), chosenPackage.getPackageCode());
            return String.format("%s_%s_%s_%s", CONTRACT_TEMPLATE_PREFIX, partnerId, typeContract, ZERO_INTEREST);
        }
        return String.format("%s_%s_%s", CONTRACT_TEMPLATE_PREFIX, partnerId, typeContract);
    }

    private static String escapeXml(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
