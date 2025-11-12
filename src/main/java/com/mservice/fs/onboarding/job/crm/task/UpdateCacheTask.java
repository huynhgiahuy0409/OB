package com.mservice.fs.onboarding.job.crm.task;

import com.mservice.fs.base.PlatformData;
import com.mservice.fs.cache.CacheData;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.model.CommonErrorCode;
import com.mservice.fs.onboarding.enums.OnboardingErrorCode;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.ScamStatus;
import com.mservice.fs.onboarding.model.crm.CrmResponse;
import com.mservice.fs.onboarding.model.crm.CrmUpdateScamRequest;
import com.mservice.fs.onboarding.model.crm.LoanListWrapper;
import com.mservice.fs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author phat.duong
 * on 9/8/2025
 **/
public class UpdateCacheTask extends GetCacheTask<CrmUpdateScamRequest, CrmResponse> {

    @Override
    protected void doMoreAction(PlatformData<CrmUpdateScamRequest, CrmResponse> platformData, LoanListWrapper loanListWrapper) throws BaseException {

        if (Utils.isEmpty(loanListWrapper.getUpdateList())) {
            throw new BaseException(OnboardingErrorCode.CACHE_NOT_FOUND);
        }

        CrmResponse crmResponse = new CrmResponse();
        crmResponse.setResultCode(CommonErrorCode.SUCCESS);
        platformData.setResponse(crmResponse);
    }

    @Override
    protected void updateCache(PlatformData<CrmUpdateScamRequest, CrmResponse> platformData, String key, CacheData cacheData, LoanListWrapper loanListWrapper) {
        ApplicationListWrapper cacheObject = (ApplicationListWrapper) cacheData.getObject();
        CrmUpdateScamRequest request = platformData.getRequest();
        List<ApplicationData> updateList = new ArrayList<>();
        List<ApplicationForm> applicationFormList = cacheObject.getApplicationForms();
        applicationFormList.forEach(applicationForm -> {
            ApplicationData applicationData = applicationForm.getApplicationData();
            if ((Utils.isEmpty(request.getLoanId())
                    || request.getLoanId().equals(applicationData.getApplicationId()))
                    && ScamStatus.PENDING.name().equals(applicationData.getScamStatus())) {
                applicationData.setScamStatus(ScamStatus.VERIFIED.name());
                cacheStorage.update(key, cacheData);
                updateList.add(applicationData);
            }
        });

        loanListWrapper.setUpdateList(updateList);

    }
}
