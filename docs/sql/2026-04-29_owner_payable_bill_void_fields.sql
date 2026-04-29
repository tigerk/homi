ALTER TABLE `owner_payable_bill`
  CHANGE COLUMN `cancel_reason` `void_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作废原因',
  CHANGE COLUMN `cancel_by` `void_by` bigint DEFAULT NULL COMMENT '作废操作人ID',
  CHANGE COLUMN `cancel_by_name` `void_by_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作废操作人名称',
  CHANGE COLUMN `cancel_at` `void_at` datetime DEFAULT NULL COMMENT '作废时间';
