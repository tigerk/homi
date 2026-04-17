SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `owner_payable_bill_line`
  RENAME TO `owner_payable_bill_fee`;

ALTER TABLE `owner_payable_bill_fee`
  CHANGE COLUMN `item_name` `fee_name` varchar(128) NOT NULL COMMENT '费用名称快照',
  CHANGE COLUMN `item_type` `fee_type` varchar(64) NOT NULL COMMENT '费用业务类型',
  ADD COLUMN `dict_data_id` bigint DEFAULT NULL COMMENT '费用字典ID' AFTER `fee_type`;

ALTER TABLE `owner_payable_bill_fee`
  RENAME INDEX `idx_owner_payable_bill_line_bill` TO `idx_owner_payable_bill_fee_bill`,
  RENAME INDEX `idx_owner_payable_bill_line_source` TO `idx_owner_payable_bill_fee_source`,
  ADD KEY `idx_owner_payable_bill_fee_dict` (`company_id`, `dict_data_id`);

ALTER TABLE `owner_settlement_bill_line`
  RENAME TO `owner_settlement_bill_fee`;

ALTER TABLE `owner_settlement_bill_fee`
  CHANGE COLUMN `item_name` `fee_name` varchar(128) NOT NULL COMMENT '费用名称快照',
  CHANGE COLUMN `item_type` `fee_type` varchar(64) NOT NULL COMMENT '费用业务类型',
  ADD COLUMN `dict_data_id` bigint DEFAULT NULL COMMENT '费用字典ID' AFTER `fee_type`;

ALTER TABLE `owner_settlement_bill_fee`
  RENAME INDEX `idx_owner_settlement_bill_line_bill` TO `idx_owner_settlement_bill_fee_bill`,
  RENAME INDEX `idx_owner_settlement_bill_line_source` TO `idx_owner_settlement_bill_fee_source`,
  RENAME INDEX `idx_owner_settlement_bill_line_subject` TO `idx_owner_settlement_bill_fee_subject`,
  ADD KEY `idx_owner_settlement_bill_fee_dict` (`company_id`, `dict_data_id`);

ALTER TABLE `lease_bill_fee`
  CHANGE COLUMN `fee_start` `fee_start_date` date DEFAULT NULL COMMENT '费用周期开始',
  CHANGE COLUMN `fee_end` `fee_end_date` date DEFAULT NULL COMMENT '费用周期结束';

SET FOREIGN_KEY_CHECKS = 1;
