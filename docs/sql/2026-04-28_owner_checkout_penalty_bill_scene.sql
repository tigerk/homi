ALTER TABLE `owner_contract_checkout`
  ADD COLUMN `breach_penalty_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '业主退房违约金' AFTER `settlement_remark`;

ALTER TABLE `owner_settlement_bill`
  ADD COLUMN `bill_scene` varchar(64) NOT NULL DEFAULT 'REGULAR' COMMENT '账单业务场景: REGULAR-正常账单,REALTIME_SETTLEMENT-实时分账,CHECKOUT_PENALTY-业主退房违约金' AFTER `bill_no`,
  ADD KEY `idx_owner_settlement_bill_scene` (`bill_scene`);

ALTER TABLE `owner_payable_bill`
  ADD COLUMN `bill_scene` varchar(64) NOT NULL DEFAULT 'REGULAR' COMMENT '账单业务场景: REGULAR-正常账单,REALTIME_SETTLEMENT-实时分账,CHECKOUT_PENALTY-业主退房违约金' AFTER `bill_no`,
  ADD KEY `idx_owner_payable_bill_scene` (`bill_scene`);
