alter table OB_PARTNER_CONFIG add APPLY_STATUS_AT_FINAL_SUBMIT number(1) default 0;
insert into OB_PARTNER_CONFIG (service_id, partner_id, apply_status_at_final_submit) values ('lending_marketplace', 'cro_vib', 1);
alter table ob_service_config add CONTRACT_LENGTH number default 10;
update ob_service_config set CONTRACT_LENGTH = 15 where service_id = 'lending_marketplace';


alter table ob_partner_config add APPLY_KNOCK_OUT_RULE_LENDER_ID number(1) default 0;
alter table ob_partner_config add LENDER_ID_AI varchar2(20);

insert into ob_partner_config(service_id, partner_id, apply_knock_out_rule_lender_id, lender_id_ai) values ('lending_marketplace', 'clo_fecredit', 1, 'FE_CREDIT');
insert into ob_partner_config(service_id, partner_id, apply_knock_out_rule_lender_id, lender_id_ai) values ('lending_marketplace', 'lending_mp_homecredit', 1, 'HOME_CREDIT');
update ob_action_config set apply_knock_out_rule_lender_id = 1, lender_id_ai = 'VIB' where service_id = 'lending_marketplace' and partner_id = 'cro_vib';

Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','check-status');
Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','init-application-form');
Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','final-submit');
Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','generate-otp');
Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','re-generate-otp');
Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','verify-otp');
Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','update-status');

delete OB_ACTION_CONFIG where ACTION_ID = 'CHECK_USER_PROFILE' and service_id = 'lending_marketplace';
update ob_service_config set AI_ACTION_MAPPING = 1 where service_id = 'lending_marketplace';

Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','IS_KYC_CONFIRM,IS_FACE_MATCH,ID_CARD_TYPE_UNCHECK,ANY_ACTION','RE_KYC',4135,'init-application-form');
Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','IS_KYC_CONFIRM,FACE_NOT_MATCH,ID_CARD_TYPE_UNCHECK,ANY_ACTION','FACE_MATCHING',4122,'init-application-form');
Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','IS_KYC_CONFIRM,FACE_NOT_MATCH,ID_CARD_TYPE_UNCHECK,ANY_ACTION','RE_KYC,FACE_MATCHING',4136,'init-application-form');
Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','IS_KYC_CONFIRM,IS_FACE_MATCH,ID_CARD_TYPE_UNCHECK,C06_UN_VERIFIED','NFC_IDCARD,C06_IDCARD',4438,'init-application-form');
Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','IS_KYC_CONFIRM,IS_FACE_MATCH,ID_CARD_TYPE_UNCHECK,ANY_ACTION','RE_KYC,NFC_IDCARD,C06_IDCARD',4435,'init-application-form');
Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','IS_KYC_CONFIRM,FACE_NOT_MATCH,ID_CARD_TYPE_UNCHECK,C06_UN_VERIFIED','FACE_MATCHING,NFC_IDCARD,C06_IDCARD',4439,'init-application-form');
Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','IS_KYC_CONFIRM,FACE_NOT_MATCH,ID_CARD_TYPE_UNCHECK,ANY_ACTION','RE_KYC,FACE_MATCHING,NFC_IDCARD,C06_IDCARD',4436,'init-application-form');
Insert into OB_AI_LOAN_ACTION (SERVICE_ID,USER_PROFILE_INFO,AI_LOAN_ACTION,RESULT_CODE,REDIRECT_PROCESS_NAME) values ('lending_marketplace','KYC_UN_CONFIRM,ANY_ACTION,ID_CARD_TYPE_UNCHECK,ANY_ACTION','EKYC,FACE_MATCHING,NFC_IDCARD,C06_IDCARD',4432,'init-application-form');

delete OB_RENDER_DATA where service_id = 'lending_marketplace' and result_code = 4131;
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4122',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_face.png','Xác thực khuôn mặt để tiếp tục','Bạn cần xác thực khuôn mặt để tiếp tục tạo khoản vay và hoàn thiện hồ sơ vay.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"kyc"},"data":{"params":{"purpose":"EKYC_IDENTIFY","forService":"loan_offers","option":"FACE_ONLY"},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationId}","partnerId":"${base.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Xác thực','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4135',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_ocr.png','Xác thực CCCD để tiếp tục','Bạn cần chụp hai mặt CCCD gắn chip để tiếp tục tạo khoản vay và hoàn thiện hồ sơ vay.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Chụp CCCD","trackingParams":{"name":"update"},"data":{"params":{"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"OCR_ONLY","extraData":{"forceToCCCDChip":true}},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationData.applicationId}","partnerId":"${applicationData.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Chụp CCCD','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4136',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_ocr.png','Xác thực CCCD để tiếp tục','Bạn cần chụp hai mặt CCCD gắn chip và xác thực khuôn mặt để tiếp tục tạo khoản vay và hoàn thiện hồ sơ vay.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Chụp CCCD","trackingParams":{"name":"update"},"data":{"params":{"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"FULL","extraData":{"forceToCCCDChip":true}},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationId}","partnerId":"${base.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Xác thực','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4432',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_nfc.png','Xác thực sinh trắc học để tiếp tục','Bạn cần chụp hai mặt CCCD gắn chip, quét NFC và xác thực khuôn mặt để tiếp tục tạo khoản vay. Thông tin sẽ được đối chiếu với dữ liệu của Bộ công an để đảm bảo chính xác.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"kyc"},"data":{"params":{"purpose":"EKYC_IDENTIFY","forService":"loan_offers","option":"OCR_ONLY","nfcOption":"NFC_FACE"},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationId}","partnerId":"${base.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Xác thực','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4435',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_nfc.png','Xác thực sinh trắc học để tiếp tục','Bạn cần chụp hai mặt CCCD gắn chip và quét NFC để tiếp tục tạo khoản vay. Thông tin sẽ được đối chiếu với dữ liệu của Bộ công an để đảm bảo chính xác.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"update"},"data":{"params":{"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"OCR_ONLY","nfcOption":"NFC_ONLY"},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationData.applicationId}","partnerId":"${applicationData.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Xác thực','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4436',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_nfc.png','Xác thực sinh trắc học để tiếp tục','Bạn cần chụp hai mặt CCCD gắn chip, xác thực sinh trắc học NFC và xác thực khuôn mặt để tiếp tục tạo khoản vay. Thông tin sẽ được đối chiếu với dữ liệu của Bộ công an để đảm bảo chính xác.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"kyc"},"data":{"params":{"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"OCR_ONLY","nfcOption":"NFC_FACE"},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationId}","partnerId":"${base.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Xác thực','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4438',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_nfc.png','Xác thực sinh trắc học để tiếp tục','Bạn cần xác thực sinh trắc học bằng NFC để tiếp tục tạo khoản vay. Thông tin sẽ được đối chiếu với dữ liệu của Bộ công an để đảm bảo chính xác.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"update"},"data":{"params":{"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"NFC_ONLY","extraData":{"nfcUnlockMethod":"OCR_BACK","forceNfcMethod":true}},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationData.applicationId}","partnerId":"${applicationData.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Xác thực','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4439',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_nfc.png','Xác thực sinh trắc học để tiếp tục','Bạn cần xác thực sinh trắc học NFC và xác thực khuôn mặt để tiếp tục tạo khoản vay. Thông tin sẽ được đối chiếu với dữ liệu của Bộ công an để đảm bảo chính xác.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"update"},"data":{"params":{"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"NFC_FACE","extraData":{"nfcUnlockMethod":"OCR_BACK","forceNfcMethod":true}},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationData.applicationId}","partnerId":"${applicationData.partnerId}"}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Đóng","trackingParams":{"name":"close"}}','ROW','Xác thực','Đóng','init-application-form',null,null,null);
Insert into OB_RENDER_DATA (SERVICE_ID,PARTNER_ID,RESULT_CODE,RESULT_MESSAGE,IMAGE,TITLE,MESSAGE,RENDER_TYPE,PRIMARY,SECONDARY,BUTTON_DIRECTION,PRIMARY_CTA,SECONDARY_CTA,PROCESS_NAME,PARTNER_MAP_ID,TRACKING_PARAMS,NAVIGATION_TYPE) values ('lending_marketplace','','4131',null,'/files/bGVuZGluZ19wbGF0Zm9ybQ==/image/nfc/banner_s_bank.png','Liên kết ngân hàng để tiếp tục','Bạn cần liên kết ngân hàng để tiếp tục tạo khoản vay.','POPUP','{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Liên kết","trackingParams":{"name":"confirm"},"data":{"params":{"forwardBankData":{"fromService":"backToEntryPoint"}},"featureCode":"bank_link_list","dataCallBack":{"params":{}}}}','{"code":"CLOSE_POPUP","initCountDownInSeconds":0,"name":"Bỏ qua","trackingParams":{"name":"close"}}','ROW','Liên kết','Bỏ qua','init-application-form',null,null,null);

Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','SEND_PLATFORM_LISTENER','verify-otp');
Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','SEND_PLATFORM_LISTENER','final-submit');

ALTER TABLE FI_PACKAGE_CONTRACT ADD AVAILABLE_AMOUNTS VARCHAR(500);
ALTER TABLE FI_RELATIVE_INFO ADD NATIONAL_ID VARCHAR(50);
INSERT INTO OB_FLOW (SERVICE_ID, PROCESS_NAME, NEXT_PROCESS_NAME) VALUES ('lending_marketplace', 'generate-otp-sign', 're-generate-otp-sign')

procedure update_package(
P_APPLICATION_ID IN VARCHAR2,
P_LOAN_AMOUNT IN NUMBER,
P_PACKAGE_CODE IN VARCHAR2,
P_DISBURSED_AMOUNT IN NUMBER,
P_MIN_LOAN_AMOUNT IN NUMBER,
P_MAX_LOAN_AMOUNT IN NUMBER,
P_AVAILABLE_AMOUNTS IN VARCHAR2
) AS p_count NUMBER := 0;
BEGIN
SELECT COUNT(*) INTO p_count FROM FI_PACKAGE_CONTRACT WHERE CONTRACT_ID = P_APPLICATION_ID;
IF (p_count = 0 ) THEN
            insert into fi_package_contract(contract_id, loan_amount, package_code, disbursed_amount, min_loan_amount, max_loan_amount, AVAILABLE_AMOUNTS)
            values(P_APPLICATION_ID, P_LOAN_AMOUNT, P_PACKAGE_CODE, P_DISBURSED_AMOUNT, P_MIN_LOAN_AMOUNT, P_MAX_LOAN_AMOUNT, P_AVAILABLE_AMOUNTS);
COMMIT;
ELSE
UPDATE FI_PACKAGE_CONTRACT
SET loan_amount = (CASE WHEN P_LOAN_AMOUNT = 0 or P_LOAN_AMOUNT = null THEN LOAN_AMOUNT ELSE P_LOAN_AMOUNT END)
  , package_code = coalesce(P_PACKAGE_CODE, PACKAGE_CODE)
  , disbursed_amount = (CASE WHEN P_DISBURSED_AMOUNT = 0 or P_DISBURSED_AMOUNT is null THEN disbursed_amount ELSE P_DISBURSED_AMOUNT END)
  , min_loan_amount = (CASE WHEN P_MIN_LOAN_AMOUNT = 0 or P_MIN_LOAN_AMOUNT is null THEN min_loan_amount ELSE P_MIN_LOAN_AMOUNT END)
  , max_loan_amount = (CASE WHEN P_MAX_LOAN_AMOUNT = 0 or P_MAX_LOAN_AMOUNT is null THEN max_loan_amount ELSE P_MAX_LOAN_AMOUNT END)
  , AVAILABLE_AMOUNTS = coalesce(P_AVAILABLE_AMOUNTS, AVAILABLE_AMOUNTS)
WHERE contract_id = P_APPLICATION_ID;
COMMIT;
END IF;
END update_package;

procedure get_submitted_form(
        P_SERVICE_ID IN VARCHAR2,
		P_AGENT_ID   IN VARCHAR2,
        p_personal_id   IN VARCHAR2,
        p_service_group   IN VARCHAR2,
		P_RESULT OUT SYS_REFCURSOR,
        p_application_by_agent   		OUT SYS_REFCURSOR
    ) AS
BEGIN
OPEN P_RESULT FOR
select info.reference_id, info.status, -- info.new_status,
    decode(info.new_status, null,(decode(info.status,'CREATE','INIT_APPLICATION_FORM','CANCEL','INIT_APPLICATION_FORM',info.status)), info.new_status) new_status, info.partner_code, info.service_code, info.create_time, info.last_modified, info.state, coalesce(info.contract_id, contract.contract_id) contract_id
     , PACKAGE.PACKAGE_GROUP, PACKAGE.PACKAGE_NAME, PACKAGE.PACKAGE_CODE, PACKAGE.LENDER_ID
     , PACKAGE.RANK, PACKAGE.TENOR, PACKAGE.LOAN_AMOUNT, PACKAGE.DISBURSED_AMOUNT, PACKAGE.INTEREST_AMOUNT
     , PACKAGE.INTEREST_UNIT, PACKAGE.SERVICE_FEE, PACKAGE.COLLECTION_FEE, PACKAGE.DISBURSED_FEE
     , PACKAGE.LATE_INTEREST, PACKAGE.LATE_FEE, PACKAGE.INTEREST, PACKAGE.PAYMENT_AMOUNT
     , PACKAGE.EMI, PACKAGE.TENOR_UNIT, PACKAGE.SEGMENT_USER, PACKAGE.LENDER_LOGIC, PACKAGE.DUE_DAY, PRODUCT_GROUP, PACKAGE.MIN_LOAN_AMOUNT, PACKAGE.MAX_LOAN_AMOUNT
     , PACKAGE.AVAILABLE_AMOUNTS
     , userinfo.FULL_NAME, userinfo.modified_name
     , info.reason_id
     , info.reason_message
     , info.full_package_raw
from fi_application_info info
         left join fi_package_contract package on info.reference_id = package.contract_id
         left join fi_user_info userinfo on info.reference_id = userinfo.reference_id
         LEFT JOIN fs_credit.fi_contract contract ON contract.reference_id = info.reference_id
where info.service_code IN (
    SELECT REGEXP_SUBSTR(P_SERVICE_ID, '[^|]+', 1, LEVEL)
    FROM dual
CONNECT BY REGEXP_SUBSTR(P_SERVICE_ID, '[^|]+', 1, LEVEL) IS NOT NULL
    )
       and info.agent_id = P_AGENT_ID
order by info.last_modified desc;

get_contract_by_id(P_AGENT_ID, p_personal_id, p_service_group, p_application_by_agent);
end get_submitted_form;

procedure update_package_user_info(
P_APPLICATION_ID IN VARCHAR2,
P_TENOR IN NUMBER,
P_LOAN_AMOUNT IN NUMBER,
P_EMI IN NUMBER,
P_INCOME IN NUMBER,
P_MONTHLY_INTEREST_RATE IN VARCHAR2
) AS p_count NUMBER := 0;
BEGIN
SELECT COUNT(*) INTO p_count FROM FI_PACKAGE_CONTRACT WHERE CONTRACT_ID = P_APPLICATION_ID;
IF (p_count = 0 ) THEN
            insert into fi_package_contract(contract_id, tenor, loan_amount, emi, monthly_interest_rate)
            values(P_APPLICATION_ID, P_TENOR, P_LOAN_AMOUNT, P_EMI, P_MONTHLY_INTEREST_RATE);
COMMIT;
ELSE
UPDATE FI_PACKAGE_CONTRACT
SET loan_amount = (CASE WHEN P_LOAN_AMOUNT = 0 or P_LOAN_AMOUNT = null THEN LOAN_AMOUNT ELSE P_LOAN_AMOUNT END)
  , tenor = (CASE WHEN P_TENOR = 0 or P_TENOR = null THEN TENOR ELSE P_TENOR END)
  , emi = (CASE WHEN P_EMI = 0 or P_EMI is null THEN emi ELSE P_EMI END)
  , monthly_interest_rate = (CASE WHEN P_MONTHLY_INTEREST_RATE = 0 or P_MONTHLY_INTEREST_RATE is null THEN monthly_interest_rate ELSE P_MONTHLY_INTEREST_RATE END)
WHERE contract_id = P_APPLICATION_ID;
COMMIT;
END IF;
UPDATE FI_USER_INFO SET MONTHLY_INCOME = P_INCOME
WHERE REFERENCE_ID = P_APPLICATION_ID;
COMMIT;
END update_package_user_info;

procedure STORE_RELATIVE_INFO(
        P_REFERENCE_ID  IN VARCHAR2,
        P_FULL_NAME     IN VARCHAR2,
        P_PHONE_NUMBER  IN VARCHAR2,
        P_CODE  IN NUMBER,
        P_RELATIVE      IN VARCHAR2,
        P_PARTNER_CODE  IN VARCHAR2,
        P_CONTRACT_ID   IN VARCHAR2,
        P_NATIONAL_ID   IN VARCHAR2
    )AS p_count NUMBER := 0;
BEGIN
INSERT INTO FI_RELATIVE_INFO(
    REFERENCE_ID, CONTRACT_ID, FULL_NAME, PHONE_NUMBER, CODE, RELATIVE, PARTNER_CODE, NATIONAL_ID
)
VALUES(
          P_REFERENCE_ID, P_CONTRACT_ID, P_FULL_NAME, P_PHONE_NUMBER, P_CODE, P_RELATIVE, P_PARTNER_CODE, P_NATIONAL_ID
      );
COMMIT;
END STORE_RELATIVE_INFO;

PROCEDURE GET_APPLICATION_BY_ID (
        p_application_id       IN VARCHAR2,
        p_phone_number       IN VARCHAR2,
        p_application_info  OUT SYS_REFCURSOR,
        p_reference_people OUT SYS_REFCURSOR,
        P_PACKAGE OUT SYS_REFCURSOR,
        P_CONTRACT_FILE_LINK OUT SYS_REFCURSOR,
        P_ADDITIONAL_DATA OUT SYS_REFCURSOR
    ) AS
BEGIN
        p_application_info := get_application_contract_id(p_application_id, p_phone_number);
        p_reference_people := get_reference_people_by_contract_id(p_application_id, p_phone_number);
        P_PACKAGE := get_package_by_contract_id(p_application_id);
        P_CONTRACT_FILE_LINK := get_contract_file_link(p_application_id, p_phone_number);
        P_ADDITIONAL_DATA := get_additional_data_by_id(p_application_id)
END GET_APPLICATION_BY_ID;

function get_additional_data_by_id(
        P_CONTRACT_ID in varchar2
    ) return sys_refcursor
    is p_result sys_refcursor;
begin
open p_result FOR
SELECT
    *
FROM
    FI_ADDITIONAL_DATA
WHERE
        CONTRACT_ID = p_contract_id
return p_result;
end get_additional_data_by_id;

ALTER TABLE OB_PARTNER_CONFIG ADD APPLY_SEND_PLATFORM_LISTENER NUMBER(1,0) DEFAULT 0;

Insert into OB_PARTNER_CONFIG (SERVICE_ID,PARTNER_ID,MINI_APP_TRACK_VER_PACKAGE,APPLY_STATUS_AT_FINAL_SUBMIT,APPLY_KNOCK_OUT_RULE_LENDER_ID,LENDER_ID_AI,APPLY_SCORE_AT_LOAN_DECIDER,CALLER_ID,APPLY_SEND_PLATFORM_LISTENER) values ('lending_marketplace','clo_mcredit',null,0,1,'MCREDIT',0,'onboarding_clo_mcredit',1);

Insert into OB_OTP_CONFIG (SERVICE_ID,MAX_GENERATE_OTP_TIMES,MAX_VERIFY_OTP_TIMES,RESET_OTP_IN_MILLIS,VALID_OTP_IN_MILLIS,CREATE_TIME,LAST_MODIFIED,APPLICATION_TIMEOUT,ID,PARTNER_ID,OTP_LENGTH) values ('lending_marketplace',5,5,86400000,180000,null,null,3600000,141,'clo_mcredit',6);
update ob_render_data set primary = '{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"update"},"data":{"params":{"configs":{"EKYCNFCOption":{"method":["nfc","nfc_by_friend"]}},"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"OCR_ONLY","nfcOption":"NFC_ONLY","extraData":{"nfcUnlockMethod":"OCR_BACK","forceNfcMethod":true}},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationData.applicationId}","partnerId":"${applicationData.partnerId}"}}}}' where service_id = 'lending_marketplace' and result_code = '4435';

update ob_render_data set primary = '{"code":"OPEN_FEATURE","initCountDownInSeconds":0,"name":"Xác thực","trackingParams":{"name":"kyc"},"data":{"params":{"configs":{"EKYCNFCOption":{"method":["nfc","nfc_by_friend"]}},"purpose":"EKYC_SERVICE_UPDATE","forService":"loan_offers","option":"OCR_ONLY","nfcOption":"NFC_FACE","extraData":{"nfcUnlockMethod":"OCR_BACK","forceNfcMethod":true}},"featureCode":"ekyc_module","dataCallBack":{"processName":"${processName}","params":{"applicationId":"${applicationId}","partnerId":"${base.partnerId}"}}}}' where service_id = 'lending_marketplace' and result_code = '4436';

INSERT INTO ob_action_config (service_id, action_id, process_name)
VALUES ('lending_marketplace', 'SEND_PLATFORM_LISTENER', 'final-submit');

INSERT INTO ob_action_config (service_id, action_id, process_name)
VALUES ('lending_marketplace', 'SEND_PLATFORM_LISTENER', 'generate-otp-sign');

FUNCTION get_application_contract_id(
        p_contract_id IN VARCHAR2,
        p_phone_number IN VARCHAR2
    ) return sys_refcursor
    IS p_result sys_refcursor;
begin
open p_result for
select
    appinfo.reference_id,
    appinfo.agent_id,
    appinfo.phone_number,
    appinfo.partner_code,
    appinfo.service_code,
    appinfo.recheck_loan_decider,
    appinfo.tax_code,
    appinfo.status_id,
    appinfo.status,
    appinfo.step_id,
    appinfo.extras,
    appinfo.package_code,
    appinfo.full_package_raw,
    appinfo.momo_credit_score,
    appinfo.social_seller_profile,
    appinfo.loan_decider_approve_time,
    appinfo.contract_id contractIdNew,
    appinfo.is_momo_loan,
    appinfo.new_status,
    appinfo.lender_logic,
    appinfo.active,
    appinfo.create_time,
    appinfo.last_modified,
    appinfo.initiator,
    addinfo.full_address,
    addinfo.ward_code,
    addinfo.ward_name,
    addinfo.district_code,
    addinfo.district_name,
    addinfo.province_code,
    addinfo.province_name,
    userinfo.full_name,
    userinfo.new_gender,
    userinfo.id_type,
    userinfo.dob,
    userinfo.email,
    userinfo.personal_id,
    userinfo.monthly_income,
    userinfo.front_image_path,
    userinfo.back_image_path,
    userinfo.face_matching_image_path,
    userinfo.front_image_url,
    userinfo.back_image_url,
    userinfo.face_matching_image_url,
    userinfo.new_issue_date,
    userinfo.id_issue_date,
    userinfo.id_issue_place,
    appinfo.contract_id,
    appinfo.reason_id,
    appinfo.reason_message,
    appinfo.partner_reference_id
FROM
    fs_credit.fi_application_info appinfo
        LEFT JOIN fs_credit.fi_address_info     addinfo ON addinfo.reference_id = appinfo.reference_id
        AND addinfo.partner_code = appinfo.partner_code
        LEFT JOIN fs_credit.fi_user_info        userinfo ON userinfo.reference_id = appinfo.reference_id
        AND userinfo.partner_code = appinfo.partner_code
        LEFT JOIN fs_credit.fi_contract         contract ON contract.reference_id = appinfo.reference_id
        AND contract.partner_code = appinfo.partner_code
WHERE
        appinfo.contract_id = p_contract_id
  and ( appinfo.phone_number = p_phone_number or appinfo.initiator = p_phone_number)

;
return p_result;
END get_application_contract_id;

Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','UPDATE_WHEN_FAIL','re-generate-otp-sign');

get_application_contract_id -> select thêm 2 field  userinfo.expiry_date, userinfo.new_expiry_date
    update_package_user_info -> thêm 2 param p_reason_message, p_partner_application_id

Insert into OB_ACTION_CONFIG (SERVICE_ID,ACTION_ID,PROCESS_NAME) values ('lending_marketplace','EVENT_TO_AI','update-application');
