package com.mservice.fs.onboarding.service;

import com.mservice.fs.generic.service.DataCreator;
import com.mservice.fs.generic.service.InitService;
import com.mservice.fs.jdbc.processor.CallableProcessor;
import com.mservice.fs.model.BaseException;
import com.mservice.fs.onboarding.model.PackageInfo;
import com.mservice.fs.onboarding.model.PeriodUnit;
import com.mservice.fs.onboarding.model.common.config.PackageInfoConfig;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PackageInfoService extends CallableProcessor<PackageInfoConfig> implements DataCreator<PackageInfoConfig>, InitService {

    @Override
    public PackageInfoConfig create() throws Exception, BaseException {
        return run();
    }

    @Override
    protected PackageInfoConfig processWithStatement(CallableStatement cs) throws SQLException, BaseException {
        PackageInfoConfig packageInfoConfig = new PackageInfoConfig();
        ResultSet rsPackage = (ResultSet) cs.getObject("P_RESULT");
        while (rsPackage.next()) {
            PackageInfo packageItem = new PackageInfo();
            packageItem.setProductGroup(rsPackage.getString("PRODUCT_GROUP"));
            packageItem.setPackageGroup(rsPackage.getString("PACKAGE_GROUP"));
            packageItem.setPackageCode(rsPackage.getString("PACKAGE_CODE"));
            packageItem.setTenor(rsPackage.getInt("LOAN_TERM"));
            packageItem.setEmi(rsPackage.getInt("PAYMENT_EACH_PERIOD"));
            packageItem.setLoanAmount(rsPackage.getLong("LOAN_AMOUNT"));
            packageItem.setDisbursedAmount(rsPackage.getLong("DISBURSED_AMOUNT"));
            packageItem.setDisbursedFee(rsPackage.getLong("DISBURSED_FEE"));
            packageItem.setInterestAmount(rsPackage.getLong("INTEREST_AMOUNT"));
            packageItem.setInterestUnit(PeriodUnit.valueOf(rsPackage.getString("INTEREST_DURATION_UNIT")));
            packageItem.setServiceFee(rsPackage.getLong("SERVICE_FEE"));
            packageItem.setCollectionFee(rsPackage.getLong("COLLECTION_FEE"));
            packageItem.setLateFee(rsPackage.getLong("LATE_FEE"));
            packageItem.setInterest(rsPackage.getDouble("INTEREST_RATE"));
            packageItem.setPackageName(rsPackage.getString("PACKAGE_NAME"));
            packageItem.setPaymentAmount(rsPackage.getLong("PAYMENT_AMOUNT"));
            packageItem.setTenorUnit(PeriodUnit.valueOf(rsPackage.getString("LOAN_TERM_DURATION_UNIT")));
            packageItem.setLenderId(rsPackage.getString("LENDER_ID"));
            packageItem.setDueDay(rsPackage.getString("DUE_DAY"));
            packageItem.setPackageMapName(rsPackage.getString("PACKAGE_MAP_NAME"));
            packageItem.setLenderName(rsPackage.getString("LENDER_NAME"));
            packageItem.setPartnerId(rsPackage.getString("PARTNER_ID"));
            packageItem.setMonthlyInterestRate(rsPackage.getString("MONTHLY_INTEREST_RATE"));
            packageItem.setBeforePaymentAmount(rsPackage.getLong("BEFORE_PAYMENT_AMOUNT"));
            packageItem.setBeforeEmi(rsPackage.getLong("BEFORE_EMI"));
            packageItem.setFlatMonthlyInterestRate(rsPackage.getDouble("FLAT_MONTHLY_INTEREST_RATE"));
            packageInfoConfig.addPackage(packageItem);
        }

        return packageInfoConfig;
    }

}
