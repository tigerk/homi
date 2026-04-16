SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 当前 homi-todo.sql 与目标 homi.sql 的实际差异
-- 不包含 create_time / update_time 迁移，因为当前库结构已是 create_at / update_at
-- 本脚本只处理事件时间字段 _time -> _at 以及相关索引名调整

ALTER TABLE `approval_action`
  RENAME COLUMN `operate_time` TO `operate_at`;

ALTER TABLE `approval_instance`
  RENAME COLUMN `finish_time` TO `finish_at`;

ALTER TABLE `company`
  RENAME COLUMN `dict_sync_time` TO `dict_sync_at`;

ALTER TABLE `company_order`
  RENAME COLUMN `cancel_time` TO `cancel_at`,
  RENAME COLUMN `refund_time` TO `refund_at`,
  RENAME COLUMN `pay_time` TO `pay_at`,
  RENAME COLUMN `notify_time` TO `notify_at`;

ALTER TABLE `company_package_order`
  RENAME COLUMN `pay_time` TO `pay_at`,
  RENAME COLUMN `cancel_time` TO `cancel_at`,
  RENAME COLUMN `refund_time` TO `refund_at`;

ALTER TABLE `contract_seal_provider`
  RENAME COLUMN `auth_time` TO `auth_at`,
  RENAME COLUMN `expire_time` TO `expire_at`;

ALTER TABLE `dict`
  RENAME COLUMN `sync_time` TO `sync_at`;

ALTER TABLE `dict_data`
  RENAME COLUMN `sync_time` TO `sync_at`;

ALTER TABLE `finance_flow`
  RENAME COLUMN `flow_time` TO `flow_at`;

ALTER TABLE `finance_flow`
  RENAME INDEX `idx_company_flow_time` TO `idx_company_flow_at`;

ALTER TABLE `lease`
  RENAME COLUMN `check_in_time` TO `check_in_at`,
  RENAME COLUMN `check_out_time` TO `check_out_at`;

ALTER TABLE `lease_bill`
  RENAME COLUMN `void_time` TO `void_at`;

ALTER TABLE `lease_checkout`
  RENAME COLUMN `settlement_time` TO `settlement_at`;

ALTER TABLE `login_log`
  RENAME COLUMN `login_time` TO `login_at`;

ALTER TABLE `operation_log`
  RENAME COLUMN `request_time` TO `request_at`;

ALTER TABLE `payment_flow`
  RENAME COLUMN `pay_time` TO `pay_at`,
  RENAME COLUMN `expire_time` TO `expire_at`;

ALTER TABLE `payment_flow`
  RENAME INDEX `idx_company_pay_time` TO `idx_company_pay_at`;

ALTER TABLE `room`
  RENAME COLUMN `vacancy_start_time` TO `vacancy_start_at`;

ALTER TABLE `sys_message`
  RENAME COLUMN `read_time` TO `read_at`;

ALTER TABLE `sys_notice`
  RENAME COLUMN `publish_time` TO `publish_at`;

ALTER TABLE `sys_notice_read`
  RENAME COLUMN `read_time` TO `read_at`;

ALTER TABLE `sys_todo`
  RENAME COLUMN `handle_time` TO `handle_at`,
  RENAME COLUMN `read_time` TO `read_at`;

ALTER TABLE `tenant_backup`
  RENAME COLUMN `check_in_time` TO `check_in_at`,
  RENAME COLUMN `check_out_time` TO `check_out_at`;

SET FOREIGN_KEY_CHECKS = 1;
