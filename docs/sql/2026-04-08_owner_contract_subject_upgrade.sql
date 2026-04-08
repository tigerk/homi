-- 业主合同从 owner_contract_house 升级为 owner_contract_subject
-- 当前无历史数据迁移要求，直接按结构升级执行即可。

-- 1. 合同房源表
RENAME TABLE owner_contract_house TO owner_contract_subject;

ALTER TABLE owner_contract_subject
  ADD COLUMN subject_type varchar(32) NOT NULL DEFAULT 'HOUSE' COMMENT '合同房源类型: HOUSE/BUILDING/COMMUNITY' AFTER contract_id,
  CHANGE COLUMN house_id subject_id bigint NOT NULL COMMENT '合同房源ID',
  CHANGE COLUMN house_name_snapshot subject_name_snapshot varchar(255) DEFAULT NULL COMMENT '合同房源名称快照',
  RENAME INDEX uk_owner_contract_house TO uk_owner_contract_subject,
  RENAME INDEX idx_owner_contract_house_company TO idx_owner_contract_subject_company,
  RENAME INDEX idx_owner_contract_house_house TO idx_owner_contract_subject_ref;

ALTER TABLE owner_contract_subject
  DROP INDEX uk_owner_contract_subject,
  ADD UNIQUE KEY uk_owner_contract_subject (contract_id, subject_type, subject_id, deleted),
  DROP INDEX idx_owner_contract_subject_ref,
  ADD KEY idx_owner_contract_subject_ref (subject_type, subject_id);

-- 2. 轻托管规则统一挂在 contract_subject_id
ALTER TABLE owner_settlement_rule
  CHANGE COLUMN contract_house_id contract_subject_id bigint NOT NULL COMMENT '业主合同房源ID',
  RENAME INDEX idx_owner_settlement_rule_house TO idx_owner_settlement_rule_subject;

ALTER TABLE owner_settlement_item
  CHANGE COLUMN contract_house_id contract_subject_id bigint NOT NULL COMMENT '合同房源ID',
  RENAME INDEX idx_owner_settlement_item_house TO idx_owner_settlement_item_subject;

ALTER TABLE owner_rent_free_rule
  CHANGE COLUMN contract_house_id contract_subject_id bigint NOT NULL COMMENT '业主合同房源ID',
  RENAME INDEX idx_owner_rent_free_rule_house TO idx_owner_rent_free_rule_subject;

-- 3. 账单主表增加合同房源维度
ALTER TABLE owner_bill
  ADD COLUMN subject_type varchar(32) DEFAULT NULL COMMENT '合同房源类型: HOUSE/BUILDING/COMMUNITY' AFTER contract_id,
  ADD COLUMN subject_id bigint DEFAULT NULL COMMENT '合同房源ID' AFTER subject_type,
  ADD COLUMN subject_name_snapshot varchar(255) DEFAULT NULL COMMENT '合同房源名称快照' AFTER subject_id,
  ADD UNIQUE KEY uk_owner_bill_subject_period (contract_id, subject_type, subject_id, bill_start, bill_end, deleted),
  ADD KEY idx_owner_bill_subject (subject_type, subject_id);

-- 4. 账单明细增加合同房源维度
ALTER TABLE owner_bill_line
  ADD COLUMN subject_type varchar(32) DEFAULT NULL COMMENT '合同房源类型: HOUSE/BUILDING/COMMUNITY' AFTER source_id,
  ADD COLUMN subject_id bigint DEFAULT NULL COMMENT '合同房源ID' AFTER subject_type,
  ADD COLUMN subject_name_snapshot varchar(255) DEFAULT NULL COMMENT '合同房源名称快照' AFTER subject_id,
  ADD KEY idx_owner_bill_line_subject (subject_type, subject_id);

-- 5. 如需全新建表，可直接参考下面的新表结构
-- CREATE TABLE IF NOT EXISTS `owner_contract_subject` (
--   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
--   `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
--   `contract_id` bigint NOT NULL COMMENT '业主合同ID',
--   `subject_type` varchar(32) NOT NULL DEFAULT 'HOUSE' COMMENT '合同房源类型: HOUSE/BUILDING/COMMUNITY',
--   `subject_id` bigint NOT NULL COMMENT '合同房源ID',
--   `subject_name_snapshot` varchar(255) DEFAULT NULL COMMENT '合同房源名称快照',
--   `remark` varchar(500) DEFAULT NULL COMMENT '备注',
--   `status` int NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
--   `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是',
--   `create_by` bigint DEFAULT NULL COMMENT '创建人',
--   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--   `update_by` bigint DEFAULT NULL COMMENT '更新人',
--   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
--   PRIMARY KEY (`id`),
--   UNIQUE KEY `uk_owner_contract_subject` (`contract_id`,`subject_type`,`subject_id`,`deleted`),
--   KEY `idx_owner_contract_subject_company` (`company_id`),
--   KEY `idx_owner_contract_subject_ref` (`subject_type`,`subject_id`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主合同房源表';
