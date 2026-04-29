ALTER TABLE `owner_contract`
  ADD COLUMN `void_reason` varchar(500) DEFAULT NULL COMMENT '作废原因' AFTER `checkout_at`,
  ADD COLUMN `void_by` bigint DEFAULT NULL COMMENT '作废操作人ID' AFTER `void_reason`,
  ADD COLUMN `void_at` datetime DEFAULT NULL COMMENT '作废时间' AFTER `void_by`,
  ADD KEY `idx_owner_contract_void_at` (`void_at`);
