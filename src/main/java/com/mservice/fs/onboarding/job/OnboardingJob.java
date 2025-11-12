package com.mservice.fs.onboarding.job;

import com.mservice.fs.app.component.RenderData;
import com.mservice.fs.app.component.SimplePage;
import com.mservice.fs.app.component.cta.CtaFeature;
import com.mservice.fs.common.Entry;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.processor.Base;
import com.mservice.fs.generic.service.DataService;
import com.mservice.fs.generic.validate.ValidatorException;
import com.mservice.fs.job.PlatformJob;
import com.mservice.fs.json.Json;
import com.mservice.fs.log.Log;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.model.ErrorCode;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.model.OnboardingRequest;
import com.mservice.fs.onboarding.model.OnboardingResponse;
import com.mservice.fs.onboarding.model.common.config.ServiceObConfig;
import com.mservice.fs.onboarding.model.common.config.ServiceObInfo;
import com.mservice.fs.onboarding.utils.OnboardingUtils;
import com.mservice.fs.onboarding.utils.constant.Constant;
import com.mservice.fs.template.TemplateMessage;
import com.mservice.fs.utils.CommonConstant;
import com.mservice.fs.utils.Utils;

import java.io.IOException;
import java.util.Map;

public abstract class OnboardingJob<T extends OnboardingRequest, R extends OnboardingResponse> extends PlatformJob<OnboardingData<T, R>, T, R, OnboardingConfig> {

    @Autowire
    private TemplateMessage templateMessage;

    @Autowire(name = "ServiceConfigInfo")
    protected DataService<ServiceObConfig> onboardingDataInfo;


    public OnboardingJob(String name) {
        super(name);
    }

    @Override
    public ErrorCode getSystemBugErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }

    @Override
    public ErrorCode getInvalidRequestErrorCode() {
        return CommonErrorCode.SYSTEM_BUG;
    }

    @Override
    public ErrorCode getTimeoutErrorCode() {
        return CommonErrorCode.JOB_TIMEOUT;
    }

    @Override
    public ErrorCode getNoResponseErrorCode() {
        return CommonErrorCode.NO_RESPONSE;
    }

    @Override
    protected boolean executiveWhenTimeout() {
        return false;
    }

    @Override
    protected boolean parseResultMessage() {
        return true;
    }

    @Override
    protected OnboardingData<T, R> initData(OnboardingData<T, R> data, Base base) throws ReflectiveOperationException, IOException {
        OnboardingData<T, R> jobData = super.initData(data, base);
        T request = data.getRequest();
        String partnerId = request.getPartnerId();
        if (Utils.isEmpty(jobData.getPartnerId()) && Utils.isNotEmpty(partnerId)) {
            base.setPartnerId(partnerId);
        }
        jobData.addTagData(Entry.of(Constant.PARTNER_ID_KEY, Utils.nullToEmpty(base.getPartnerId())));
        jobData.addTagData(Entry.of(Constant.TAG_NAME, CommonConstant.STRING_EMPTY));
        jobData.addTagData(Entry.of(Constant.LOAN_DECIDER_TAG_NAME, CommonConstant.STRING_EMPTY));
        jobData.addTagData(Entry.of(Constant.PACKAGE_TAG_NAME, CommonConstant.STRING_EMPTY));
        return jobData;
    }

    @Override
    protected void addDataBeforeReply(OnboardingData<T, R> data, R response) {
        if (Utils.isEmpty(response.getRenderData())) {
            try {
                ServiceObInfo serviceObInfo = onboardingDataInfo.getData().getServiceObInfo(data.getServiceId());
                RenderData renderData = serviceObInfo.getRenderData(response.getResultCode(), data.getProcessName());
                if (Utils.isEmpty(renderData)) {
                    Log.MAIN.info("Empty render data, do nothing !!!");
                    super.addDataBeforeReply(data, response);
                    return;
                }
                if (isEnableReplaceRenderData()
                        && isReplaceRenderData(data)
                        && Utils.isNotEmpty(renderData.getInformationPage())
                        && Utils.isNotEmpty(renderData.getInformationPage().getButtons())
                        && Utils.isNotEmpty(renderData.getInformationPage().getButtons().getPrimary())
                        && Utils.isNotEmpty(renderData.getInformationPage().getButtons().getPrimary().getData())
                        && CtaFeature.Cta.OPEN_FEATURE.equals(renderData.getInformationPage().getButtons().getPrimary().getCode())
                        && renderData.getInformationPage().getButtons().getPrimary().getData() instanceof CtaFeature.FeatureCodeData) {

                    RenderData renderDataClone = OnboardingUtils.deepCopy(renderData);
                    if (Utils.isEmpty(renderDataClone)) {
                        Log.MAIN.info("Empty renderDataClone !!");
                        buildRenderData(data, response, renderData);
                        super.addDataBeforeReply(data, response);
                        return;
                    }

                    Map<String, Object> params = ((CtaFeature.FeatureCodeData) renderDataClone.getInformationPage().getButtons().getPrimary().getData()).getParams();
                    String option = String.valueOf(params.getOrDefault(Constant.OPTION, CommonConstant.STRING_EMPTY));
                    String nfcOption = String.valueOf(params.getOrDefault(Constant.NFC_OPTION, CommonConstant.STRING_EMPTY));

                    String key = String.join("-", data.getServiceId(), option, nfcOption);
                    Object extraRenderData = getConfig().getReplaceExtraRenderDataMap().get(key);
                    Object extraData = params.get(Constant.EXTRA_DATA);

                    if (Utils.isNotEmpty(extraRenderData)) {
                        Log.MAIN.info("Replace extra old {} by new {} - id {}", String.valueOf(extraData), String.valueOf(extraRenderData), key);
                        params.put(Constant.EXTRA_DATA, extraRenderData);
                        params.put(Constant.OPTION, "OCR_ONLY");
                        params.remove(Constant.NFC_OPTION);
                        ((CtaFeature.FeatureCodeData) renderDataClone.getInformationPage().getButtons().getPrimary().getData()).setParams(params);
                        Log.MAIN.info("SimplePage after modify: {}", Json.encode(renderDataClone.getInformationPage()));
                        renderDataClone.getInformationPage().setId(renderDataClone.getInformationPage().id() + "_modify");
                        buildRenderData(data, response, renderDataClone);
                        super.addDataBeforeReply(data, response);
                        return;
                    }
                }
                buildRenderData(data, response, renderData);
            } catch (BaseException | Exception | ValidatorException e) {
                Log.MAIN.error("Error when set renderData: ", e);
            }
        }
        super.addDataBeforeReply(data, response);
    }

    private void buildRenderData(OnboardingData<T, R> data, R response, RenderData renderData) throws Exception {
        RenderData renderDataResponse = new RenderData();
        Log.MAIN.info("SimplePage before process template: {}", Json.encode(renderData.getInformationPage()));
        SimplePage simplePage = templateMessage.process(renderData.getInformationPage(), data.getTemplateModel());
        Log.MAIN.info("Render data for job {} : {}", data.getProcessName(), Json.encode(simplePage));
        renderDataResponse.setInformationPage(simplePage);
        response.setRenderData(renderDataResponse);
    }

    protected boolean isReplaceRenderData(OnboardingData<T, R> data) {
        return false;
    }

    private boolean isEnableReplaceRenderData() {
        return getConfig().isEnableReplaceRenderData();
    }
}
