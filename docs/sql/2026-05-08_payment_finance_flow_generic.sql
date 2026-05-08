-- payment_flow / finance_flow 通用化。
-- 设计口径：
-- 1. payment_flow 作为通用支付流水，biz_type/biz_id/biz_no 表示业务归属。
-- 2. finance_flow 作为通用财务流水，biz_type/biz_id/biz_no 表示业务归属。
-- 3. finance_flow.payment_flow_id 保留且允许为空，用于关联通用支付流水。
-- 4. finance_flow.source_type/source_id/source_no 删除，不再作为来源字段使用。

-- payment_flow 增加业务单据编号。
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `payment_flow` ADD COLUMN `biz_no` varchar(64) DEFAULT NULL COMMENT ''业务单据编号'' AFTER `biz_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'payment_flow'
    AND COLUMN_NAME = 'biz_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- finance_flow 恢复/保留 payment_flow_id，并允许为空。
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `finance_flow` ADD COLUMN `payment_flow_id` bigint DEFAULT NULL COMMENT ''关联通用支付流水ID'' AFTER `company_id`',
    'ALTER TABLE `finance_flow` MODIFY COLUMN `payment_flow_id` bigint DEFAULT NULL COMMENT ''关联通用支付流水ID'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND COLUMN_NAME = 'payment_flow_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果执行过旧的 source_* 迁移，先把 PAYMENT_FLOW 来源迁回 payment_flow_id。
SET @sql = (
  SELECT IF(
    COUNT(*) = 3,
    'UPDATE `finance_flow` SET `payment_flow_id` = `source_id` WHERE `payment_flow_id` IS NULL AND `source_type` = ''PAYMENT_FLOW''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND COLUMN_NAME IN ('source_type', 'source_id', 'source_no')
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 包租应付付款财务流水按业务归属补回 payment_flow_id。
UPDATE `finance_flow` ff
JOIN `payment_flow` pf
  ON pf.`biz_type` = 'OWNER_PAYABLE_BILL_PAYMENT'
 AND pf.`biz_id` = ff.`biz_id`
SET ff.`payment_flow_id` = pf.`id`
WHERE ff.`payment_flow_id` IS NULL
  AND ff.`biz_type` = 'OWNER_PAYABLE_BILL_PAYMENT';

-- 历史 payment_flow.biz_no 回填。
UPDATE `payment_flow`
SET `biz_no` = CAST(`biz_id` AS CHAR)
WHERE (`biz_no` IS NULL OR `biz_no` = '')
  AND `biz_type` = 'LEASE_BILL';

UPDATE `payment_flow` pf
JOIN `lease_checkout` lc
  ON pf.`biz_type` = 'TENANT_CHECKOUT'
 AND pf.`biz_id` = lc.`id`
SET pf.`biz_no` = lc.`checkout_code`
WHERE pf.`biz_no` IS NULL OR pf.`biz_no` = '';

UPDATE `payment_flow` pf
JOIN `owner_payable_bill_payment` opbp
  ON pf.`biz_type` = 'OWNER_PAYABLE_BILL_PAYMENT'
 AND pf.`biz_id` = opbp.`id`
SET pf.`biz_no` = opbp.`payment_no`
WHERE pf.`biz_no` IS NULL OR pf.`biz_no` = '';

-- 删除旧 source_* 索引和字段。
SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `finance_flow` DROP INDEX `idx_finance_flow_source`',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND INDEX_NAME = 'idx_finance_flow_source'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `finance_flow` DROP COLUMN `source_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND COLUMN_NAME = 'source_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `finance_flow` DROP COLUMN `source_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND COLUMN_NAME = 'source_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `finance_flow` DROP COLUMN `source_no`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND COLUMN_NAME = 'source_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 补通用索引。
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `payment_flow` ADD KEY `idx_payment_flow_biz` (`biz_type`, `biz_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'payment_flow'
    AND INDEX_NAME = 'idx_payment_flow_biz'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `finance_flow` ADD KEY `idx_finance_flow_payment_flow_id` (`payment_flow_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND INDEX_NAME = 'idx_finance_flow_payment_flow_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `finance_flow` ADD KEY `idx_finance_flow_biz_type_status` (`biz_type`, `status`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'finance_flow'
    AND INDEX_NAME = 'idx_finance_flow_biz_type_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
