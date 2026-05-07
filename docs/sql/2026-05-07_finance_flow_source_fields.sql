-- 财务流水改为通用来源结构。
-- payment_flow_id 只作为本次迁移的旧字段来源，后续统一使用 source_type/source_id/source_no。

ALTER TABLE `finance_flow`
  MODIFY COLUMN `payment_flow_id` bigint DEFAULT NULL COMMENT '关联支付流水ID（租客收款场景）',
  ADD COLUMN `source_type` varchar(64) DEFAULT NULL COMMENT '来源类型' AFTER `payment_flow_id`,
  ADD COLUMN `source_id` bigint DEFAULT NULL COMMENT '来源单据ID' AFTER `source_type`,
  ADD COLUMN `source_no` varchar(64) DEFAULT NULL COMMENT '来源单据编号' AFTER `source_id`;

UPDATE `finance_flow`
SET `source_type` = 'PAYMENT_FLOW',
    `source_id` = `payment_flow_id`
WHERE `payment_flow_id` IS NOT NULL
  AND (`source_type` IS NULL OR `source_type` = '');

UPDATE `finance_flow` ff
JOIN `payment_flow` pf ON pf.`id` = ff.`payment_flow_id`
SET ff.`source_no` = pf.`payment_no`
WHERE ff.`source_type` = 'PAYMENT_FLOW'
  AND (ff.`source_no` IS NULL OR ff.`source_no` = '');

ALTER TABLE `finance_flow`
  ADD KEY `idx_finance_flow_source` (`source_type`, `source_id`),
  ADD KEY `idx_finance_flow_biz_type_status` (`biz_type`, `status`);

ALTER TABLE `finance_flow`
  DROP COLUMN `payment_flow_id`;
