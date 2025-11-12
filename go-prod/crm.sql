
-- table config không xoá data
create table OB_CRM_PRODUCT_CONFIG (
    PRODUCT_NAME varchar2 (255),
    CALLER_ID    varchar2 (255)
);


-- table config không xoá data
create table OB_CRM_PRODUCT_SERVICE_MAP (
    PRODUCT_NAME varchar2 (255),
    SERVICE_ID varchar2 (255)
);


-- table config không xoá data
create table OB_CRM_STATUS_MAP (
    PRODUCT_NAME varchar2 (255),
    STATUS    varchar2 (255),
    DESCRIPTION  varchar2(500),
    ALLOW_DELETED number(1)
);


-- table tracking lưu data 6 tháng
create table OB_CRM_TRACKING_DELETED_CACHE_RECORDS (
    APPLICATION_ID    varchar2 (100),
    SERVICE_ID  varchar2(100),
    PHONE_NUMBER varchar2(20),
    FORM varchar2(4000),
    CANCELED_BY varchar2(100),
    reason varchar2(500)
);

-- fix:OML-601
PROCEDURE get_list_application_crm (
p_phone_number   IN VARCHAR2,
p_personal_id IN VARCHAR2,
p_begin_date IN VARCHAR2,
p_end_date   IN VARCHAR2,
p_result     OUT SYS_REFCURSOR
) AS
BEGIN
        -- Update logic for OML-601 by huy.huynh4
OPEN P_RESULT FOR
        WITH base AS (
            SELECT
                appinfo.reference_id      AS loan_id,
                appinfo.service_code,
                appinfo.partner_code,
                appinfo.agent_id,
                appinfo.phone_number,
                appinfo.create_time,
                appinfo.last_modified,
                userinfo.full_name,
                userinfo.personal_id,
                contractinfo.loan_amount,
                appinfo.new_status,
                'DATABASE'                AS type
            FROM
                fi_application_info appinfo
                LEFT JOIN fi_user_info userinfo
                    ON appinfo.reference_id = userinfo.reference_id
                LEFT JOIN fi_package_contract contractinfo
                    ON appinfo.reference_id = contractinfo.contract_id
            WHERE
                appinfo.service_code IN (
                    SELECT service_id
                    FROM ob_crm_product_service_map
                )
--                AND appinfo.create_time BETWEEN TO_DATE(p_begin_date, 'DD/MM/YYYY HH24:MI:SS')
--                                            AND TO_DATE(p_end_date,   'DD/MM/YYYY HH24:MI:SS')
        )
-- Case 1: p_personal_id is empty & p_phone_number is not empty
SELECT *
FROM base
WHERE (p_personal_id IS NULL OR TRIM(p_personal_id) = '') AND p_phone_number IS NOT NULL
  AND base.phone_number = p_phone_number

UNION ALL

-- Case 2: p_personal_id is not empty & p_phone_number is empty
SELECT
    *
FROM
    base
WHERE
    agent_id IN (
        SELECT DISTINCT
            agent_id
        FROM
            base
        WHERE
            personal_id = p_personal_id
          AND (p_phone_number IS NULL OR TRIM(p_phone_number) = '')
    );


END get_list_application_crm;
-- fix:OML-601
PROCEDURE get_list_application_crm (
p_agent_id   IN VARCHAR2,
p_personal_id IN VARCHAR2,
p_begin_date IN VARCHAR2,
p_end_date   IN VARCHAR2,
p_result     OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN P_RESULT FOR
            WITH base AS (
                SELECT
                    appinfo.reference_id      AS loan_id,
                    appinfo.service_code,
                    appinfo.partner_code,
                    appinfo.agent_id,
                    appinfo.phone_number,
                    appinfo.create_time,
                    appinfo.last_modified,
                    userinfo.full_name,
                    userinfo.personal_id,
                    contractinfo.loan_amount,
                    appinfo.new_status,
                    'DATABASE'                AS type
                FROM
                    fi_application_info appinfo
                    LEFT JOIN fi_user_info userinfo
                        ON appinfo.reference_id = userinfo.reference_id
                    LEFT JOIN fi_package_contract contractinfo
                        ON appinfo.reference_id = contractinfo.contract_id
                WHERE
                    appinfo.service_code IN (
                        SELECT service_id
                        FROM ob_crm_product_service_map
                    )
    --                AND appinfo.create_time BETWEEN TO_DATE(p_begin_date, 'DD/MM/YYYY HH24:MI:SS')
    --                                            AND TO_DATE(p_end_date,   'DD/MM/YYYY HH24:MI:SS')
            )
    -- Case 1: Không truyền personal_id
    SELECT *
    FROM base
    WHERE (p_personal_id IS NULL OR TRIM(p_personal_id) = '')
      AND agent_id = p_agent_id

    UNION ALL

    -- Case 2: Có truyền personal_id
    SELECT
        *
    FROM
        base
    WHERE
        personal_id = p_personal_id
    UNION ALL
    SELECT
        *
    FROM
        base
    WHERE
        agent_id IN (
            SELECT DISTINCT
                agent_id
            FROM
                base
            WHERE
                personal_id = p_personal_id
        );
END get_list_application_crm;

    procedure store_deleted_cache (
        p_application_id    in varchar2,
        p_service_id        in varchar2,
        p_phone_number      in VARCHAR2,
        p_form              in VARCHAR2,
        p_canceled_by       in VARCHAR2,
        p_reason            in VARCHAR2
    )
as begin
        insert into OB_CRM_TRACKING_DELETED_CACHE_RECORDS
        (APPLICATION_ID, SERVICE_ID, PHONE_NUMBER, FORM, CANCELED_BY, reason)
        values (p_application_id, p_service_id, p_phone_number, p_form, p_canceled_by, p_reason);
end store_deleted_cache;

END PKG_ONBOARDING_NEW_PLATFORM_CRM;

insert into OB_CRM_PRODUCT_CONFIG(PRODUCT_NAME, CALLER_ID) values ('crm_fastmoney', 'onboarding_fastmoney');

insert into OB_CRM_PRODUCT_SERVICE_MAP(PRODUCT_NAME, SERVICE_ID) values ('crm_fastmoney', 'finance_lending_amber');
insert into OB_CRM_PRODUCT_SERVICE_MAP(PRODUCT_NAME, SERVICE_ID) values ('crm_fastmoney', 'finance_lending_fmob');


Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','CANCELED_BY_MOMO','Bị hủy bởi MoMo',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','CANCELED_BY_LENDER','Bị hủy bởi đối tác',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','FIRST_SUBMIT','Xem thông tin tóm tắt hồ sơ (hoàn thành step 2)',1);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','GENERATED_OTP','Đã gửi OTP nhưng khách hàng chưa xác thực',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','INIT_APPLICATION_FORM','Đã khởi tạo hồ sơ và chọn package (hoàn thành step 1)',1);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','LIQUIDATION','Đã tất toán khoản vay',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','REJECTED_BY_KNOCKOUT_RULE','Bị từ chối bởi AI',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','REJECTED_BY_LENDER','Bị từ chối bởi đối tác',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','REJECTED_BY_LIMITED_GENERATED_OTP_EXCEED','Bị khóa OTP do tạo lại OTP quá số lần quy định',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','REJECTED_BY_LIMITED_VERIFIED_OTP_EXCEED','Bị khóa OTP do xác thực OTP quá số lần quy định',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','REJECTED_BY_LOAN_DECIDER','Bị từ chối bởi AI',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','VERIFIED_OTP_SUCCESS','Đã xác thực OTP thành công',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','ACTIVATED_BY_LENDER','Đã giải ngân thành công',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','REVIEW_BY_LENDER','Đang chờ bên cho vay xem xét hợp đồng',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','ACCEPTED_BY_MOMO','Người dùng gửi đơn đăng ký thành công trên phía MOMO (trước khi gửi dữ liệu cho bên cho vay)',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','VERIFIED_OTP_FAILED','Verify OTP Fail',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','APPROVED_BY_LENDER','Hợp đồng đã được đối tác chấp thuận',0);
Insert into OB_CRM_STATUS_MAP (PRODUCT_NAME,STATUS,DESCRIPTION,ALLOW_DELETED) values ('crm_fastmoney','ACCEPTED_BY_LENDER','Người dùng gửi đơn đăng ký thành công và được chấp nhận',0);

