ALTER TABLE `lease_checkout`
  CHANGE COLUMN `expected_payment_date` `due_date` date DEFAULT NULL COMMENT '退租结算应完成日期',
  MODIFY COLUMN `payee_id_type` tinyint DEFAULT NULL COMMENT '收款人证件类型',
  MODIFY COLUMN `bank_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收款银行类型',
  MODIFY COLUMN `bank_card_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行卡类型',
  ADD COLUMN `payment_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '支付状态' AFTER `settlement_method`,
  ADD COLUMN `pay_at` datetime DEFAULT NULL COMMENT '支付完成时间' AFTER `payment_status`;

ALTER TABLE `lease_checkout_fee`
  CHANGE COLUMN `fee_sub_name` `fee_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '费用名称快照',
  CHANGE COLUMN `fee_period_start` `fee_start_date` date DEFAULT NULL COMMENT '费用开始日期',
  CHANGE COLUMN `fee_period_end` `fee_end_date` date DEFAULT NULL COMMENT '费用结束日期',
  CHANGE COLUMN `bill_id` `lease_bill_id` bigint DEFAULT NULL COMMENT '关联租客账单ID',
  ADD COLUMN `dict_data_id` bigint DEFAULT NULL COMMENT '费用字典数据项ID' AFTER `fee_type`;

CREATE INDEX `idx_lease_checkout_payment_status` ON `lease_checkout` (`payment_status`);
CREATE INDEX `idx_lease_checkout_due_date` ON `lease_checkout` (`due_date`);
CREATE INDEX `idx_lease_checkout_fee_dict_data_id` ON `lease_checkout_fee` (`dict_data_id`);
CREATE INDEX `idx_lease_checkout_fee_lease_bill_id` ON `lease_checkout_fee` (`lease_bill_id`);
