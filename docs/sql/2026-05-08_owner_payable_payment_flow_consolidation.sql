-- 包租应付付款收敛到 payment_flow。
-- 新口径：
-- 1. 不再使用 owner_payable_bill_payment 表。
-- 2. payment_flow 作为包租应付付款申请和实际支付流水，审批 biz_id 指向 payment_flow.id。
-- 3. payment_flow.biz_type = OWNER_PAYABLE_BILL_PAYMENT，biz_id = owner_payable_bill.id，biz_no = owner_payable_bill.bill_no。
-- 4. finance_flow 仍通过 payment_flow_id 关联支付流水，费用项级流水使用 OWNER_PAYABLE_BILL_FEE。

-- 旧付款记录没有对应 payment_flow 时，先补一条 payment_flow。补入记录使用旧付款记录 ID 作为 payment_flow.id，
-- 以便历史 approval_instance.biz_id 可以无缝指向 payment_flow.id。
SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'INSERT INTO `payment_flow` (`id`, `payment_no`, `company_id`, `biz_type`, `biz_id`, `biz_no`, `channel`, `third_trade_no`, `payment_voucher_url`, `amount`, `currency`, `refunded_amount`, `flow_direction`, `status`, `approval_status`, `pay_at`, `payer_name`, `receiver_name`, `operator_id`, `operator_name`, `remark`, `ext_json`, `create_by`, `create_at`, `update_by`, `update_at`) SELECT opbp.`id`, COALESCE(NULLIF(opbp.`payment_no`, ''''), CONCAT(''PAY'', opbp.`id`)), opbp.`company_id`, ''OWNER_PAYABLE_BILL_PAYMENT'', opbp.`bill_id`, opb.`bill_no`, CASE opbp.`pay_channel` WHEN 1 THEN ''CASH'' WHEN 2 THEN ''TRANSFER'' WHEN 3 THEN ''ALIPAY'' WHEN 4 THEN ''WECHAT'' ELSE ''OTHER'' END, opbp.`third_trade_no`, (SELECT fa.`file_url` FROM `file_attach` fa WHERE fa.`biz_type` = ''owner_payable_bill_payment_voucher'' AND fa.`biz_id` = opbp.`id` AND fa.`deleted` = 0 ORDER BY fa.`sort_order`, fa.`id` LIMIT 1), ABS(COALESCE(opbp.`pay_amount`, 0)), ''CNY'', 0, ''OUT'', CASE opbp.`payment_status` WHEN 1 THEN 2 WHEN 2 THEN 4 ELSE 1 END, opbp.`approval_status`, opbp.`pay_at`, ''平台'', o.`owner_name`, opbp.`create_by`, NULL, opbp.`remark`, JSON_OBJECT(''legacyOwnerPayableBillPaymentId'', opbp.`id`, ''billId'', opbp.`bill_id`, ''billNo'', opb.`bill_no`, ''ownerId'', opb.`owner_id`, ''contractId'', opb.`contract_id`), opbp.`create_by`, opbp.`create_at`, opbp.`update_by`, opbp.`update_at` FROM `owner_payable_bill_payment` opbp JOIN `owner_payable_bill` opb ON opb.`id` = opbp.`bill_id` LEFT JOIN `owner` o ON o.`id` = opb.`owner_id` LEFT JOIN `payment_flow` existed ON existed.`biz_type` = ''OWNER_PAYABLE_BILL_PAYMENT'' AND existed.`biz_id` = opbp.`id` WHERE existed.`id` IS NULL AND NOT EXISTS (SELECT 1 FROM `payment_flow` id_used WHERE id_used.`id` = opbp.`id`)',
    'SELECT 1'
  )
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'owner_payable_bill_payment'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 历史单条包租付款财务流水先补 payment_flow_id，避免后续 payment_flow.biz_id 改口径后失联。
SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'UPDATE `finance_flow` ff JOIN `payment_flow` pf ON pf.`biz_type` = ''OWNER_PAYABLE_BILL_PAYMENT'' JOIN `owner_payable_bill_payment` opbp ON pf.`biz_id` = opbp.`id` SET ff.`payment_flow_id` = pf.`id` WHERE ff.`payment_flow_id` IS NULL AND ff.`biz_type` = ''OWNER_PAYABLE_BILL_PAYMENT'' AND ff.`biz_id` = opbp.`id`',
    'SELECT 1'
  )
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'owner_payable_bill_payment'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 附件从旧付款记录 ID 迁到 payment_flow.id。
SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'UPDATE `file_attach` fa JOIN `owner_payable_bill_payment` opbp ON fa.`biz_type` = ''owner_payable_bill_payment_voucher'' AND fa.`biz_id` = opbp.`id` JOIN `payment_flow` pf ON pf.`biz_type` = ''OWNER_PAYABLE_BILL_PAYMENT'' AND pf.`biz_id` = opbp.`id` SET fa.`biz_id` = pf.`id`, fa.`biz_type` = ''payment_flow_voucher''',
    'SELECT 1'
  )
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'owner_payable_bill_payment'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 历史审批实例从旧付款记录 ID 迁到 payment_flow.id。
SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'UPDATE `approval_instance` ai JOIN `owner_payable_bill_payment` opbp ON ai.`biz_type` = ''OWNER_PAYABLE_BILL_PAYMENT'' AND ai.`biz_id` = opbp.`id` JOIN `payment_flow` pf ON pf.`biz_type` = ''OWNER_PAYABLE_BILL_PAYMENT'' AND pf.`biz_id` = opbp.`id` SET ai.`biz_id` = pf.`id`',
    'SELECT 1'
  )
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'owner_payable_bill_payment'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 补 payment_flow 首张凭证。
UPDATE `payment_flow` pf
SET pf.`payment_voucher_url` = (
  SELECT fa.`file_url`
  FROM `file_attach` fa
  WHERE fa.`biz_type` = 'payment_flow_voucher'
    AND fa.`biz_id` = pf.`id`
    AND fa.`deleted` = 0
  ORDER BY fa.`sort_order`, fa.`id`
  LIMIT 1
)
WHERE pf.`biz_type` = 'OWNER_PAYABLE_BILL_PAYMENT'
  AND (pf.`payment_voucher_url` IS NULL OR pf.`payment_voucher_url` = '');

-- payment_flow 业务归属从旧付款记录改为包租应付单。
SET @sql = (
  SELECT IF(
    COUNT(*) > 0,
    'UPDATE `payment_flow` pf JOIN `owner_payable_bill_payment` opbp ON pf.`biz_type` = ''OWNER_PAYABLE_BILL_PAYMENT'' AND pf.`biz_id` = opbp.`id` JOIN `owner_payable_bill` opb ON opb.`id` = opbp.`bill_id` SET pf.`biz_id` = opb.`id`, pf.`biz_no` = opb.`bill_no`, pf.`ext_json` = JSON_SET(COALESCE(NULLIF(pf.`ext_json`, ''''), JSON_OBJECT()), ''$.legacyOwnerPayableBillPaymentId'', opbp.`id`, ''$.billId'', opb.`id`, ''$.billNo'', opb.`bill_no`, ''$.ownerId'', opb.`owner_id`, ''$.contractId'', opb.`contract_id`)',
    'SELECT 1'
  )
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'owner_payable_bill_payment'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除旧表。
DROP TABLE IF EXISTS `owner_payable_bill_payment`;
