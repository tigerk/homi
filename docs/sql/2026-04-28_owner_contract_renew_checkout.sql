ALTER TABLE `owner_contract`
  ADD COLUMN `parent_contract_id` bigint DEFAULT NULL COMMENT '原业主合同ID' AFTER `remark`,
  ADD COLUMN `contract_nature` tinyint NOT NULL DEFAULT 1 COMMENT '合同性质: 1-新签,2-续约' AFTER `parent_contract_id`,
  ADD COLUMN `renew_from_contract_no` varchar(64) DEFAULT NULL COMMENT '续约来源合同编号快照' AFTER `contract_nature`,
  ADD COLUMN `checkout_status` tinyint NOT NULL DEFAULT 0 COMMENT '退房状态: 0-未退房,1-已退房' AFTER `renew_from_contract_no`,
  ADD COLUMN `checkout_date` date DEFAULT NULL COMMENT '退房日期' AFTER `checkout_status`,
  ADD COLUMN `checkout_reason` varchar(500) DEFAULT NULL COMMENT '退房原因' AFTER `checkout_date`,
  ADD COLUMN `checkout_by` bigint DEFAULT NULL COMMENT '退房操作人ID' AFTER `checkout_reason`,
  ADD COLUMN `checkout_by_name` varchar(64) DEFAULT NULL COMMENT '退房操作人名称' AFTER `checkout_by`,
  ADD COLUMN `checkout_at` datetime DEFAULT NULL COMMENT '退房操作时间' AFTER `checkout_by_name`,
  ADD KEY `idx_owner_contract_parent_contract_id` (`parent_contract_id`),
  ADD KEY `idx_owner_contract_checkout_status` (`checkout_status`);

CREATE TABLE `owner_contract_checkout` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `owner_contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `cooperation_mode` varchar(32) NOT NULL COMMENT '合作模式',
  `checkout_date` date NOT NULL COMMENT '退房/解约日期',
  `checkout_reason` varchar(500) NOT NULL COMMENT '退房原因',
  `settlement_remark` varchar(1000) DEFAULT NULL COMMENT '结算说明',
  `breach_penalty_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '业主退房违约金',
  `release_subject` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否释放房源: 0否,1是',
  `void_unpaid_future_bills` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否作废退房日之后未付款账单: 0否,1是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1已提交,2已完成,3已取消',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_contract_checkout_contract_id` (`owner_contract_id`),
  KEY `idx_owner_contract_checkout_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主合同退房单';

ALTER TABLE `owner_settlement_bill`
  ADD COLUMN `bill_scene` varchar(64) NOT NULL DEFAULT 'REGULAR' COMMENT '账单业务场景: REGULAR-正常账单,REALTIME_SETTLEMENT-实时分账,CHECKOUT_PENALTY-业主退房违约金' AFTER `bill_no`,
  ADD KEY `idx_owner_settlement_bill_scene` (`bill_scene`);

ALTER TABLE `owner_payable_bill`
  ADD COLUMN `bill_scene` varchar(64) NOT NULL DEFAULT 'REGULAR' COMMENT '账单业务场景: REGULAR-正常账单,REALTIME_SETTLEMENT-实时分账,CHECKOUT_PENALTY-业主退房违约金' AFTER `bill_no`,
  ADD KEY `idx_owner_payable_bill_scene` (`bill_scene`);
