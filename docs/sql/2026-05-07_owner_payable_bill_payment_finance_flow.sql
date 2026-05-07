-- 包租应付单付款记录接入审批流和统一财务流水。
-- payment_status: 0=待审核, 1=付款成功, 2=已关闭
-- approval_status: 1=审批中, 2=已通过, 3=已驳回, 4=已撤回

ALTER TABLE `owner_payable_bill_payment`
  ADD COLUMN `payment_status` tinyint NOT NULL DEFAULT 0 COMMENT '付款记录状态: 0=待审核,1=付款成功,2=已关闭' AFTER `remark`,
  ADD COLUMN `approval_status` tinyint NOT NULL DEFAULT 1 COMMENT '审批状态: 1=审批中,2=已通过,3=已驳回,4=已撤回' AFTER `payment_status`,
  ADD COLUMN `finance_flow_id` bigint DEFAULT NULL COMMENT '财务流水ID' AFTER `approval_status`;

-- 旧付款记录在改造前即表示已完成付款，补齐新状态字段。
UPDATE `owner_payable_bill_payment`
SET `payment_status` = 1,
    `approval_status` = 2
WHERE `deleted` = 0;

ALTER TABLE `owner_payable_bill_payment`
  ADD KEY `idx_owner_payable_bill_payment_finance_flow_id` (`finance_flow_id`);
