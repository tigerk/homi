SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 命名规范
-- 1. 仅表示“日期”的字段统一使用 _date
-- 2. 表示“事件发生时刻”的字段统一使用 _at
-- 3. 通用审计字段暂时保持项目既有风格：create_time / update_time
-- =========================================================

DROP TABLE IF EXISTS `owner_settlement_bill_reduction`;
DROP TABLE IF EXISTS `owner_settlement_bill_line`;
DROP TABLE IF EXISTS `owner_settlement_bill`;
DROP TABLE IF EXISTS `owner_payable_bill_payment`;
DROP TABLE IF EXISTS `owner_payable_bill_line`;
DROP TABLE IF EXISTS `owner_payable_bill`;
DROP TABLE IF EXISTS `biz_operate_log`;

-- =========================================================
-- 1. 轻托管业主结算单主表
-- =========================================================
CREATE TABLE `owner_settlement_bill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_no` varchar(64) NOT NULL COMMENT '结算单号',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `subject_type` varchar(32) NOT NULL COMMENT '合同房源类型: HOUSE/FOCUS/FOCUS_BUILDING',
  `subject_id` bigint NOT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) DEFAULT NULL COMMENT '合同房源名称快照',
  `bill_start_date` date NOT NULL COMMENT '账期开始日期',
  `bill_end_date` date NOT NULL COMMENT '账期结束日期',
  `income_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '收入金额',
  `expense_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '费用金额',
  `reduction_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '减免金额',
  `adjust_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '调账金额',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '应结金额',
  `settled_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已结金额',
  `withdrawable_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '可提现金额',
  `withdrawn_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已提现金额',
  `freeze_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
  `bill_status` tinyint NOT NULL DEFAULT 1 COMMENT '单据状态: 1正常',
  `approval_status` tinyint NOT NULL DEFAULT 1 COMMENT '审批状态: 1审批中 2已通过 3已驳回 4已撤回',
  `settlement_status` tinyint NOT NULL DEFAULT 0 COMMENT '结算状态: 0未结算 1部分结算 2已结算',
  `generated_at` datetime DEFAULT NULL COMMENT '生成时间',
  `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_settlement_bill_no` (`company_id`,`bill_no`,`deleted`),
  UNIQUE KEY `uk_owner_settlement_bill_period` (`company_id`,`contract_id`,`subject_type`,`subject_id`,`bill_start_date`,`bill_end_date`,`deleted`),
  KEY `idx_owner_settlement_bill_owner` (`company_id`,`owner_id`),
  KEY `idx_owner_settlement_bill_contract` (`company_id`,`contract_id`),
  KEY `idx_owner_settlement_bill_subject` (`company_id`,`subject_type`,`subject_id`),
  KEY `idx_owner_settlement_bill_status` (`company_id`,`approval_status`,`settlement_status`,`bill_status`),
  KEY `idx_owner_settlement_bill_generated_at` (`company_id`,`generated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轻托管业主结算单';

-- =========================================================
-- 2. 轻托管业主结算单明细
-- =========================================================
CREATE TABLE `owner_settlement_bill_line` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '结算单ID',
  `source_type` varchar(64) DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `subject_type` varchar(32) DEFAULT NULL COMMENT '合同房源类型',
  `subject_id` bigint DEFAULT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) DEFAULT NULL COMMENT '合同房源名称快照',
  `item_name` varchar(128) NOT NULL COMMENT '项目名称',
  `item_type` varchar(64) NOT NULL COMMENT '项目类型',
  `direction` varchar(16) NOT NULL COMMENT '方向: IN/OUT',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `biz_date` date DEFAULT NULL COMMENT '业务日期',
  `formula_snapshot` varchar(1000) DEFAULT NULL COMMENT '计算说明快照',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_settlement_bill_line_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_settlement_bill_line_source` (`company_id`,`source_type`,`source_id`),
  KEY `idx_owner_settlement_bill_line_subject` (`company_id`,`subject_type`,`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轻托管业主结算单明细';

-- =========================================================
-- 3. 轻托管业主结算单减免
-- =========================================================
CREATE TABLE `owner_settlement_bill_reduction` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '结算单ID',
  `source_type` varchar(64) DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `reduction_name` varchar(128) NOT NULL COMMENT '减免项名称',
  `reduction_type` varchar(64) NOT NULL COMMENT '减免类型',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '减免金额',
  `biz_date` date DEFAULT NULL COMMENT '业务日期',
  `rule_snapshot` varchar(1000) DEFAULT NULL COMMENT '规则快照',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_settlement_bill_reduction_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_settlement_bill_reduction_source` (`company_id`,`source_type`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轻托管业主结算单减免';

-- =========================================================
-- 4. 包租业主应付单主表
-- =========================================================
CREATE TABLE `owner_payable_bill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_no` varchar(64) NOT NULL COMMENT '应付单号',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `subject_type` varchar(32) NOT NULL COMMENT '合同房源类型: HOUSE/FOCUS/FOCUS_BUILDING',
  `subject_id` bigint NOT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) DEFAULT NULL COMMENT '合同房源名称快照',
  `bill_start_date` date NOT NULL COMMENT '账期开始日期',
  `bill_end_date` date NOT NULL COMMENT '账期结束日期',
  `due_date` date DEFAULT NULL COMMENT '应付日期',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已付金额',
  `unpaid_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '未付金额',
  `adjust_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '调整金额',
  `payment_status` tinyint NOT NULL DEFAULT 0 COMMENT '付款状态: 0未付款 1部分付款 2已付款',
  `bill_status` tinyint NOT NULL DEFAULT 1 COMMENT '单据状态: 1正常 2已作废',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '作废原因',
  `cancel_by` bigint DEFAULT NULL COMMENT '作废操作人ID',
  `cancel_by_name` varchar(64) DEFAULT NULL COMMENT '作废操作人名称',
  `cancel_at` datetime DEFAULT NULL COMMENT '作废时间',
  `generated_at` datetime DEFAULT NULL COMMENT '生成时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_payable_bill_no` (`company_id`,`bill_no`,`deleted`),
  UNIQUE KEY `uk_owner_payable_bill_period` (`company_id`,`contract_id`,`subject_type`,`subject_id`,`bill_start_date`,`bill_end_date`,`deleted`),
  KEY `idx_owner_payable_bill_owner` (`company_id`,`owner_id`),
  KEY `idx_owner_payable_bill_contract` (`company_id`,`contract_id`),
  KEY `idx_owner_payable_bill_subject` (`company_id`,`subject_type`,`subject_id`),
  KEY `idx_owner_payable_bill_status` (`company_id`,`payment_status`,`bill_status`),
  KEY `idx_owner_payable_bill_due_date` (`company_id`,`due_date`),
  KEY `idx_owner_payable_bill_generated_at` (`company_id`,`generated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包租业主应付单';

-- =========================================================
-- 5. 包租业主应付单明细
-- =========================================================
CREATE TABLE `owner_payable_bill_line` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '应付单ID',
  `source_type` varchar(64) DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `subject_type` varchar(32) DEFAULT NULL COMMENT '合同房源类型',
  `subject_id` bigint DEFAULT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) DEFAULT NULL COMMENT '合同房源名称快照',
  `item_name` varchar(128) NOT NULL COMMENT '项目名称',
  `item_type` varchar(64) NOT NULL COMMENT '项目类型',
  `direction` varchar(16) NOT NULL DEFAULT 'OUT' COMMENT '方向: IN/OUT',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `biz_date` date DEFAULT NULL COMMENT '业务日期',
  `formula_snapshot` varchar(1000) DEFAULT NULL COMMENT '计算说明快照',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_payable_bill_line_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_payable_bill_line_source` (`company_id`,`source_type`,`source_id`),
  KEY `idx_owner_payable_bill_line_subject` (`company_id`,`subject_type`,`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包租业主应付单明细';

-- =========================================================
-- 6. 包租业主应付单付款记录
-- =========================================================
CREATE TABLE `owner_payable_bill_payment` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '应付单ID',
  `payment_no` varchar(64) NOT NULL COMMENT '付款单号',
  `pay_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '付款金额',
  `pay_at` datetime NOT NULL COMMENT '付款时间',
  `pay_channel` varchar(32) DEFAULT NULL COMMENT '付款渠道',
  `third_trade_no` varchar(128) DEFAULT NULL COMMENT '第三方流水号',
  `voucher_urls` text DEFAULT NULL COMMENT '支付凭证URL列表(JSON)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_payable_bill_payment_no` (`company_id`,`payment_no`,`deleted`),
  KEY `idx_owner_payable_bill_payment_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_payable_bill_payment_pay_at` (`company_id`,`pay_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包租业主应付单付款记录';

-- =========================================================
-- 7. 通用业务日志表
-- =========================================================
CREATE TABLE `biz_operate_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `biz_type` varchar(64) NOT NULL COMMENT '业务类型: OWNER_PAYABLE_BILL/OWNER_SETTLEMENT_BILL/OWNER_CONTRACT/LEASE_BILL等',
  `biz_id` bigint NOT NULL COMMENT '业务主键ID',
  `operate_type` varchar(64) NOT NULL COMMENT '操作类型: CREATE/UPDATE/CANCEL/PAY/APPROVE/REJECT等',
  `operate_desc` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `before_snapshot` longtext DEFAULT NULL COMMENT '操作前快照(JSON)',
  `after_snapshot` longtext DEFAULT NULL COMMENT '操作后快照(JSON)',
  `extra_data` longtext DEFAULT NULL COMMENT '扩展数据(JSON)',
  `source_type` varchar(64) DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0否 1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz_operate_log_biz` (`company_id`,`biz_type`,`biz_id`),
  KEY `idx_biz_operate_log_source` (`company_id`,`source_type`,`source_id`),
  KEY `idx_biz_operate_log_operator` (`company_id`,`operator_id`),
  KEY `idx_biz_operate_log_create_time` (`company_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用业务操作日志';

SET FOREIGN_KEY_CHECKS = 1;
