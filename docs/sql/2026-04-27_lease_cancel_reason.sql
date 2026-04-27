ALTER TABLE `lease`
  ADD COLUMN `cancel_reason` varchar(255) DEFAULT NULL COMMENT '作废原因' AFTER `remark`,
  ADD COLUMN `cancel_by` bigint DEFAULT NULL COMMENT '作废人ID' AFTER `cancel_reason`,
  ADD COLUMN `cancel_at` datetime DEFAULT NULL COMMENT '作废时间' AFTER `cancel_by`;
