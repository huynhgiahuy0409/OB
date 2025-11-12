# **FINANCIAL ONBOARDING PLATFORM**

FMOB get contract knock out rule reject, loan decider reject:
``` sql
select contract_id, agent_id, new_status, create_time from fi_application_info
where
    new_status in ('REJECTED_BY_KNOCKOUT_RULE', 'REJECTED_BY_LOAN_DECIDER')
    and service_code = 'finance_lending_fmob'
    and create_time >= to_date('18/07/2024','dd/mm/yyyy')
    and create_time <= to_date('19/07/2024','dd/mm/yyyy')
order by create_time asc
;
```
"# OB" 
