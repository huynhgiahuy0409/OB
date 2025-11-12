package com.mservice.fs.onboarding.job.application.submit.task;

import com.mservice.fs.cache.CacheData;
import com.mservice.fs.generic.Autowire;
import com.mservice.fs.generic.TaskName;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.job.OnboardingData;
import com.mservice.fs.onboarding.job.OnboardingTask;
import com.mservice.fs.onboarding.model.ApplicationData;
import com.mservice.fs.onboarding.model.ApplicationForm;
import com.mservice.fs.onboarding.model.application.ApplicationListWrapper;
import com.mservice.fs.onboarding.model.application.submit.SubmitRequest;
import com.mservice.fs.onboarding.model.application.submit.SubmitResponse;
import com.mservice.fs.processor.TaskData;
import com.mservice.fs.redis.service.RedisCacheStorage;
import com.mservice.fs.utils.JsonUtil;

import java.util.ArrayList;

/**
 * @author hoang.thai
 * on 11/13/2023
 */
public class CreateDataTestTask extends OnboardingTask<SubmitRequest, SubmitResponse> {

    public static final TaskName NAME = () -> "GET_CACHE_APPLICATION";

    @Autowire(name = "CacheStorage")
    private RedisCacheStorage cacheStorage;

    public CreateDataTestTask() {
        super(NAME);
    }

    @Override
    protected void perform(TaskData taskData, OnboardingData<SubmitRequest, SubmitResponse> jobData) throws BaseException, Exception {
//        SubmitRequest request = jobData.getRequest();
//        String serviceId = request.getServiceId();
//        CacheData cacheData = getCacheStorage.get(Constant.REDIS_STORAGE_APPLICATION_FORM, createKey(serviceId, jobData.getInitiatorId()));
//        taskData.setContent(cacheData);
//        finish(jobData, taskData);
        ArrayList<ApplicationForm> arrayList = new ArrayList();
        ApplicationForm applicationCache = new ApplicationForm();
        ApplicationForm applicationCache2 = new ApplicationForm();
        ApplicationForm applicationCache3 = new ApplicationForm();
        ApplicationForm applicationCache4 = new ApplicationForm();
        applicationCache.setApplicationData(JsonUtil.fromString("{\"partnerId\":\"finance_creditcard_marketplace\",\"applicationId\":\"hoangtest1\",\"status\":null,\"taxId\":\"\",\"fullName\":\"NGUYỄN THỊ NGỌC PHƯƠNG\",\"dob\":\"20/03/1997\",\"idNumber\":\"079197027952\",\"phoneNumber\":\"0703456789\",\"initiator\":\"01203456789\",\"idType\":\"CCCD\",\"email\":\"\",\"gender\":\"FEMALE\",\"income\":20000000,\"createdDate\":\"01/01/2023\",\"agentId\":\"12345\",\"placeOfBirth\":{\"ward\":null,\"district\":null,\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":null,\"fullAddress\":null},\"companyAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"currentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN aNĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"shippingAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"permanentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"referencePeople\":[{\"phoneNumber\":\"0366255647\",\"fullName\":\"Nguyễn Thị Trúc Linh\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}},{\"phoneNumber\":\"0813968869\",\"fullName\":\"Trần Thị Minh Thư\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}}],\"applicationAdditionalData\":{},\"chosenPackage\":{\"packageGroup\":\"MMAAD\",\"packageName\":\"fastmoney_205\",\"packageCode\":\"MM205\",\"lenderId\":\"AMBER\",\"rank\":1,\"productGroup\":\"\",\"tenor\":6,\"loanAmount\":6000000,\"disbursedAmount\":6000000,\"interestAmount\":535799,\"interestUnit\":\"YEAR\",\"serviceFee\":756000,\"collectionFee\":120000,\"disbursedFee\":10000,\"lateInterest\":0.45,\"lateFee\":0,\"interest\":0.3,\"paymentAmount\":7411799,\"emi\":1235300,\"tenorUnit\":\"MONTH\",\"segmentUser\":\"NEW\",\"lenderLogic\":\"MOMO_LENDER\",\"dueDay\":\"20/11/2023\"}}", ApplicationData.class));
        applicationCache2.setApplicationData(JsonUtil.fromString("{\"partnerId\":\"finance_creditcard_marketplace\",\"applicationId\":\"hoangtest12\",\"status\":null,\"taxId\":\"\",\"fullName\":\"NGUYỄN THỊ NGỌC PHƯƠNG\",\"dob\":\"20/03/1997\",\"idNumber\":\"079197027952\",\"phoneNumber\":\"0703456789\",\"initiator\":\"01203456789\",\"idType\":\"CCCD\",\"email\":\"\",\"gender\":\"FEMALE\",\"income\":20000000,\"createdDate\":\"01/01/2023\",\"agentId\":\"12345\",\"placeOfBirth\":{\"ward\":null,\"district\":null,\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":null,\"fullAddress\":null},\"companyAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"currentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN aNĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"shippingAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"permanentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"referencePeople\":[{\"phoneNumber\":\"0366255647\",\"fullName\":\"Nguyễn Thị Trúc Linh\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}},{\"phoneNumber\":\"0813968869\",\"fullName\":\"Trần Thị Minh Thư\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}}],\"applicationAdditionalData\":{},\"chosenPackage\":{\"packageGroup\":\"MMAAD\",\"packageName\":\"fastmoney_205\",\"packageCode\":\"MM205\",\"lenderId\":\"AMBER\",\"rank\":1,\"productGroup\":\"\",\"tenor\":6,\"loanAmount\":6000000,\"disbursedAmount\":6000000,\"interestAmount\":535799,\"interestUnit\":\"YEAR\",\"serviceFee\":756000,\"collectionFee\":120000,\"disbursedFee\":10000,\"lateInterest\":0.45,\"lateFee\":0,\"interest\":0.3,\"paymentAmount\":7411799,\"emi\":1235300,\"tenorUnit\":\"MONTH\",\"segmentUser\":\"NEW\",\"lenderLogic\":\"MOMO_LENDER\",\"dueDay\":\"20/11/2023\"}}", ApplicationData.class));
        applicationCache3.setApplicationData(JsonUtil.fromString("{\"partnerId\":\"finance_creditcard_marketplace\",\"applicationId\":\"hoangtest123456\",\"status\":null,\"taxId\":\"\",\"fullName\":\"NGUYỄN THỊ NGỌC PHƯƠNG\",\"dob\":\"20/03/1997\",\"idNumber\":\"079197027952\",\"phoneNumber\":\"0703456789\",\"initiator\":\"01203456789\",\"idType\":\"CCCD\",\"email\":\"\",\"gender\":\"FEMALE\",\"income\":20000000,\"createdDate\":\"01/01/2023\",\"agentId\":\"12345\",\"placeOfBirth\":{\"ward\":null,\"district\":null,\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":null,\"fullAddress\":null},\"companyAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"currentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN aNĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"shippingAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"permanentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"referencePeople\":[{\"phoneNumber\":\"0366255647\",\"fullName\":\"Nguyễn Thị Trúc Linh\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}},{\"phoneNumber\":\"0813968869\",\"fullName\":\"Trần Thị Minh Thư\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}}],\"applicationAdditionalData\":{},\"chosenPackage\":{\"packageGroup\":\"MMAAD\",\"packageName\":\"fastmoney_205\",\"packageCode\":\"MM205\",\"lenderId\":\"AMBER\",\"rank\":1,\"productGroup\":\"\",\"tenor\":6,\"loanAmount\":6000000,\"disbursedAmount\":6000000,\"interestAmount\":535799,\"interestUnit\":\"YEAR\",\"serviceFee\":756000,\"collectionFee\":120000,\"disbursedFee\":10000,\"lateInterest\":0.45,\"lateFee\":0,\"interest\":0.3,\"paymentAmount\":7411799,\"emi\":1235300,\"tenorUnit\":\"MONTH\",\"segmentUser\":\"NEW\",\"lenderLogic\":\"MOMO_LENDER\",\"dueDay\":\"20/11/2023\"}}", ApplicationData.class));
        applicationCache4.setApplicationData(JsonUtil.fromString("{\"partnerId\":\"finance_creditcard_marketplace\",\"applicationId\":\"hoangtest123\",\"status\":null,\"taxId\":\"\",\"fullName\":\"THAI HUY HOANG\",\"dob\":\"20/03/1997\",\"idNumber\":\"1321456465\",\"phoneNumber\":\"0703456789\",\"initiator\":\"01203456789\",\"idType\":\"CCCD\",\"email\":\"\",\"gender\":\"MALE\",\"income\":20000000,\"createdDate\":\"01/01/2023\",\"agentId\":\"12345\",\"placeOfBirth\":{\"ward\":null,\"district\":null,\"province\":{\"id\":79,\"name\":\"TP vUNG TAU\"},\"street\":null,\"fullAddress\":\"ÁHIABAS\"},\"companyAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"currentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN aNĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"shippingAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"permanentAddress\":{\"ward\":{\"id\":27028,\"name\":\"Phường Phú Thạnh\"},\"district\":{\"id\":767,\"name\":\"Tân Phú\"},\"province\":{\"id\":79,\"name\":\"TP Hồ Chí Minh\"},\"street\":\"44A PHAN VĂN NĂM\",\"fullAddress\":\"44A PHAN VĂN NĂM, PHÚ THẠNH, TÂN PHÚ, HỒ CHÍ MINH\"},\"referencePeople\":[{\"phoneNumber\":\"0366255647\",\"fullName\":\"Nguyễn Thị Trúc Linh\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}},{\"phoneNumber\":\"0813968869\",\"fullName\":\"Trần Thị Minh Thư\",\"relationship\":{\"id\":\"1\",\"name\":\"Ba/Mẹ\"}}],\"applicationAdditionalData\":{},\"chosenPackage\":{\"packageGroup\":\"MMAAD\",\"packageName\":\"fastmoney_205\",\"packageCode\":\"MM205\",\"lenderId\":\"AMBER\",\"rank\":1,\"productGroup\":\"\",\"tenor\":6,\"loanAmount\":6000000,\"disbursedAmount\":6000000,\"interestAmount\":535799,\"interestUnit\":\"YEAR\",\"serviceFee\":756000,\"collectionFee\":120000,\"disbursedFee\":10000,\"lateInterest\":0.45,\"lateFee\":0,\"interest\":0.3,\"paymentAmount\":123456,\"emi\":1235300,\"tenorUnit\":\"MONTH\",\"segmentUser\":\"NEW\",\"lenderLogic\":\"MOMO_LENDER\",\"dueDay\":\"20/11/2023\"}}", ApplicationData.class));
        arrayList.add(applicationCache);
        arrayList.add(applicationCache2);
        arrayList.add(applicationCache3);
        arrayList.add(applicationCache4);
        ApplicationListWrapper listCacheApplication = new ApplicationListWrapper();
        listCacheApplication.getApplicationForms().add(applicationCache);
        listCacheApplication.getApplicationForms().add(applicationCache2);
        listCacheApplication.getApplicationForms().add(applicationCache3);
        listCacheApplication.getApplicationForms().add(applicationCache4);
        CacheData cacheData = new CacheData(jobData.getTraceId(), 86400000, listCacheApplication);
        cacheStorage.put(ApplicationListWrapper.createKey(jobData.getServiceId(), jobData.getInitiatorId()), cacheData);

    }

    private String createKey(String serviceId, String agentId) {
        return serviceId + ":" + agentId;
    }

}
