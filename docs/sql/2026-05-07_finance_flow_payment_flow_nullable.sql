-- 统一财务流水兼容非租客收款业务。
-- 租客收款流水会关联 payment_flow；包租应付付款、业主提现等业务不一定存在 payment_flow。
ALTER TABLE `finance_flow`
  MODIFY COLUMN `payment_flow_id` bigint DEFAULT NULL COMMENT '关联支付流水ID（租客收款场景）';
