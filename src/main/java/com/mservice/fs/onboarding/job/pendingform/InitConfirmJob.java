package com.mservice.fs.onboarding.job.pendingform;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.MidKorRejectScreenLogListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.pendingform.listener.PartnerResultAIListener;
import com.mservice.fs.onboarding.job.pendingform.listener.PendingStoreRejectFormListener;
import com.mservice.fs.onboarding.job.pendingform.listener.UpdateApplicationCache;
import com.mservice.fs.onboarding.job.pendingform.task.*;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormRequest;
import com.mservice.fs.onboarding.model.api.pendingform.PendingFormResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;
import com.mservice.fs.utils.Utils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author hoang.thai
 * on 12/28/2023
 */
@Processor(name = {OnboardingProcessor.INIT_CONFIRM, OnboardingProcessor.INIT_CONFIRM_QUICK})
public class InitConfirmJob extends OnboardingJob<PendingFormRequest, PendingFormResponse> {

    public InitConfirmJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<PendingFormRequest, PendingFormResponse>, PendingFormRequest, PendingFormResponse, OnboardingConfig>> getTaskList() throws Exception {
        return List.of(
                new LoadDataTask<>(true),
                new GetCacheTask(),
                new ApplicationCacheTask(),
                new PendingFormGetUserProfileTask(),
                new PendingUpdateProfileTask(),
                new PendingFormKnockOutRule(),
                new PendingFormPackageTask(),
                new PendingFormLendingPackageTask(),
                new PendingDeDupTask(),
//                new PendingFormSubmitTask(),
                new ModifyResponseTask()
        );
    }

    @Override
    protected List<AbstractListener<OnboardingData<PendingFormRequest, PendingFormResponse>, PendingFormRequest, PendingFormResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateApplicationCache(),
                new PendingStoreRejectFormListener(),
                new PartnerResultAIListener(),
                new MidKorRejectScreenLogListener<>()
        );
    }

    @Override
    protected LinkedHashMap<TaskName, TaskName> getTaskOrders() {
        LinkedHashMap<TaskName, TaskName> taskOrders = new LinkedHashMap<>();
        taskOrders.put(PendingFormLendingPackageTask.NAME, ModifyResponseTask.NAME);
        taskOrders.put(PendingDeDupTask.NAME, ModifyResponseTask.NAME);
        return taskOrders;
    }

    @Override
    protected boolean isReplaceRenderData(OnboardingData<PendingFormRequest, PendingFormResponse> data) {
        return Utils.isNotEmpty(data.getRequest()) && Boolean.TRUE.equals(data.getRequest().getIsNfcAvailable());
    }
}
