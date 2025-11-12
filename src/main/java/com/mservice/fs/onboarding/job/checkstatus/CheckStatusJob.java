package com.mservice.fs.onboarding.job.checkstatus;

import com.mservice.fs.generic.Processor;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.onboarding.config.OnboardingConfig;
import com.mservice.fs.onboarding.job.KorRejectScreenLogListener;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingJob;
import com.mservice.fs.onboarding.job.checkstatus.listener.CheckStatusTrackingStatusListener;
import com.mservice.fs.onboarding.job.checkstatus.listener.CheckStatusUpdateStatusListener;
import com.mservice.fs.onboarding.job.checkstatus.listener.UpdateCacheListener;
import com.mservice.fs.onboarding.job.checkstatus.task.CacheTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckDeDupMomoTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusGetDataDeDupDBTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusLendingPackageTask;
import com.mservice.fs.onboarding.job.CheckStatusGetUserProfileTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusKnockOutRuleTask;
import com.mservice.fs.onboarding.job.checkstatus.task.CheckStatusPackageTask;
import com.mservice.fs.onboarding.job.checkstatus.task.ModifyResponseTask;
import com.mservice.fs.onboarding.job.checkstatus.task.SegmentUserTask;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusRequest;
import com.mservice.fs.onboarding.model.api.status.OnboardingStatusResponse;
import com.mservice.fs.onboarding.utils.OnboardingProcessor;
import com.mservice.fs.processor.AbstractListener;
import com.mservice.fs.processor.Task;
import com.mservice.fs.task.LoadDataTask;
import com.mservice.fs.utils.Utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author hoang.thai on 10/30/2023
 */

@Processor(name = {OnboardingProcessor.CHECK_STATUS})
public class CheckStatusJob extends OnboardingJob<OnboardingStatusRequest, OnboardingStatusResponse> {

    public CheckStatusJob(String name) {
        super(name);
    }

    @Override
    protected List<Task<OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse>, OnboardingStatusRequest, OnboardingStatusResponse, OnboardingConfig>> getTaskList() throws Exception {
        return Arrays.asList(
                new LoadDataTask<>(true),
                new CheckStatusGetUserProfileTask<>(),
                new CacheTask(),
                new CheckStatusGetDataDeDupDBTask(),
                new CheckDeDupMomoTask<>(),
                new SegmentUserTask(),
                getKnockOutRuleTask(),
                getCheckStatusPackageTask(),
                new CheckStatusLendingPackageTask(),
                new ModifyResponseTask()
        );
    }

    protected CheckStatusPackageTask getCheckStatusPackageTask() {
        return new CheckStatusPackageTask();
    }

    protected Task<OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse>, OnboardingStatusRequest, OnboardingStatusResponse, OnboardingConfig> getKnockOutRuleTask() {
        return new CheckStatusKnockOutRuleTask();
    }

    @Override
    protected List<AbstractListener<OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse>, OnboardingStatusRequest, OnboardingStatusResponse, OnboardingConfig>> getDefaultListeners() throws Exception {
        return List.of(
                new UpdateCacheListener(),
                new CheckStatusUpdateStatusListener(),
                new CheckStatusTrackingStatusListener(),
                new KorRejectScreenLogListener<>()
        );
    }

    @Override
    protected LinkedHashMap<TaskName, TaskName> getTaskOrders() {
        LinkedHashMap<TaskName, TaskName> taskMap = new LinkedHashMap<>();
        taskMap.put(CacheTask.NAME, CheckDeDupMomoTask.NAME);
        taskMap.put(CheckStatusGetDataDeDupDBTask.NAME, CheckDeDupMomoTask.NAME);
        return taskMap;
    }

    @Override
    protected boolean isReplaceRenderData(OnboardingData<OnboardingStatusRequest, OnboardingStatusResponse> data) {
        return Utils.isNotEmpty(data.getRequest()) && Boolean.TRUE.equals(data.getRequest().getIsNfcAvailable());
    }
}
