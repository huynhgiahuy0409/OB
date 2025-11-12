package com.mservice.fs.onboarding.job.crm.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.crm.*;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RemoveCacheTask extends GetCacheTask<CrmCancelApplicationRequest, CrmResponse> {


    @Override
    protected void doMoreAction(PlatformData<CrmCancelApplicationRequest, CrmResponse> platformData, LoanListWrapper loanListWrapper) throws BaseException {
        List<LoanInfo> removeList = new ArrayList<>();
        loanListWrapper.setRemoveList(removeList);
        CrmCancelApplicationRequest request = platformData.getRequest();
        UserProfileInfo userProfileInfo = platformData.getTaskData(CrmGetUserProfileTask.NAME).getContent();
        CrmConfig.Config config = platformData.getTaskData(LoadCrmConfigTask.NAME).getContent();
        List<LoanInfo> fetchList = loanListWrapper.getFetchList();
        if(Utils.isEmpty(fetchList)) {
            throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
        }
        Iterator<LoanInfo> iterator = fetchList.iterator();
        while (iterator.hasNext()) {
            LoanInfo loanInfo = iterator.next();
            if (loanInfo.getLoanId().equals(request.getLoanId()) &&
                    config.isAllowDeleted(loanInfo.getStatus().name())) {
                iterator.remove();
                String removeKey = ApplicationListWrapper.createKey(loanInfo.getServiceId(), userProfileInfo.getAgent());
                cacheStorage.remove(removeKey);
                removeList.add(loanInfo);
            }
        }
        if (removeList.isEmpty()) {
            throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
        }
    }



}
