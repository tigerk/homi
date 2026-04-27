ALTER TABLE `lease_checkout`
  ADD COLUMN `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因' AFTER `remark`,
  ADD COLUMN `cancel_by` bigint DEFAULT NULL COMMENT '取消人ID' AFTER `cancel_reason`,
  ADD COLUMN `cancel_by_name` varchar(64) DEFAULT NULL COMMENT '取消人姓名' AFTER `cancel_by`,
  ADD COLUMN `cancel_at` datetime DEFAULT NULL COMMENT '取消时间' AFTER `cancel_by_name`;
