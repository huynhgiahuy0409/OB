alter table ob_ai_config add SOURCE_ID_QUICK varchar2(100);
chu y update package ONBOARDING LOAD_CONFIG;
INSERT INTO ob_action_config (service_id, action_id, process_name) values
('credit_paylater_new','GET_PACKAGE','check-white-list-package');
update ob_ai_config set SOURCE_ID_QUICK = 'BE_SOF' where service_id = 'credit_paylater_new';

INSERT INTO OB_RENDER_DATA (service_id, partner_id, result_code, result_message, image, title, message, render_type, primary, secondary, button_direction, primary_cta, secondary_cta, process_name, partner_map_id, tracking_params, navigation_type)
SELECT service_id, partner_id, result_code, result_message, image, title, message, render_type, primary, secondary, button_direction, primary_cta, secondary_cta, process_name||'-quick', partner_map_id, tracking_params, navigation_type
FROM OB_RENDER_DATA
WHERE service_id = 'credit_paylater_new'
and process_name in ('init-application-form','final-submit','init-confirm');

INSERT INTO ob_action_config (service_id, action_id, process_name)
SELECT service_id, action_id, process_name||'-quick'
FROM ob_action_config
WHERE service_id = 'credit_paylater_new'
and process_name in ('init-application-form','final-submit','init-confirm','check-status','verify-otp','generate-otp','re-generate-otp');

INSERT INTO ob_action_field (name, act_config_id, action_name, process_name, service_id, type)
SELECT name, (select ID from ob_action_config ac where ac.service_id = 'credit_paylater_new' and ac.process_name = 'final-submit' and ac.action_id = 'CHECK_USER_PROFILE'), action_name, process_name||'-quick', service_id, type
FROM ob_action_field
WHERE service_id = 'credit_paylater_new'
and process_name = 'final-submit';

insert into FS_CORE_MASTER_TEMPLATE(service_id, result_code, message, type, lang)
select temp.service_id, temp.result_code, temp.message, temp.type || '-quick', temp.lang
from FS_CORE_MASTER_TEMPLATE temp where temp.service_id = 'credit_paylater_new' and temp.type in ('check-status', 'generate-otp', 'verify-otp', 'regen-otp');

insert into FS_CORE_MASTER_PARTNER_RESULT_CODE_MAP (module_name, partner_id, process_name, partner_result_code, momo_result_code, failure_reason)
select config.module_name, config.partner_id, config.process_name || '-quick', config.partner_result_code,  config.momo_result_code, config.failure_reason
from FS_CORE_MASTER_PARTNER_RESULT_CODE_MAP config where config.module_name = 'onboarding-platform' and config.partner_id in ('onboarding_paylater_tpfico', 'credit_paylater_oceanbank');

alter table OB_PARTNER_CONFIG add APPLY_SEND_PLATFORM_TASK NUMBER(1,0) DEFAULT 0;
update OB_PARTNER_CONFIG set APPLY_SEND_PLATFORM_TASK = 1 where service_id = 'credit_paylater_new' and partner_id = 'credit_paylater_vcb';
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','final-submit');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','confirm-face-matching');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','generate-otp');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','re-generate-otp');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','verify-otp');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','final-submit-quick');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','confirm-face-matching-quick');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','generate-otp-quick');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','re-generate-otp-quick');
Insert into OB_ACTION_CONFIG (SERVICE_ID, ACTION_ID, PROCESS_NAME) values ('credit_paylater_new','SEND_PLATFORM_TASK','verify-otp-quick');





