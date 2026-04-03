ALTER TABLE owner_personal
  ADD COLUMN payee_name varchar(64) NULL COMMENT '收款人姓名' AFTER phone,
  ADD COLUMN payee_phone varchar(32) NULL COMMENT '收款人电话' AFTER payee_name,
  ADD COLUMN payee_id_type int NULL COMMENT '收款人证件类型' AFTER payee_phone,
  ADD COLUMN payee_id_no varchar(64) NULL COMMENT '收款人证件号码' AFTER payee_id_type,
  ADD COLUMN bank_account_name varchar(128) NULL COMMENT '银行卡开户名' AFTER payee_id_no,
  ADD COLUMN bank_account_no varchar(64) NULL COMMENT '银行卡号' AFTER bank_account_name,
  ADD COLUMN bank_name varchar(128) NULL COMMENT '开户行名称' AFTER bank_account_no;

ALTER TABLE owner_company
  ADD COLUMN payee_name varchar(64) NULL COMMENT '收款人姓名' AFTER contact_phone,
  ADD COLUMN payee_phone varchar(32) NULL COMMENT '收款人电话' AFTER payee_name,
  ADD COLUMN payee_id_type int NULL COMMENT '收款人证件类型' AFTER payee_phone,
  ADD COLUMN payee_id_no varchar(64) NULL COMMENT '收款人证件号码' AFTER payee_id_type,
  ADD COLUMN bank_account_name varchar(128) NULL COMMENT '银行卡开户名' AFTER payee_id_no,
  ADD COLUMN bank_account_no varchar(64) NULL COMMENT '银行卡号' AFTER bank_account_name,
  ADD COLUMN bank_name varchar(128) NULL COMMENT '开户行名称' AFTER bank_account_no;

ALTER TABLE owner_contract
  ADD COLUMN sign_type varchar(32) NULL COMMENT '签约类型：NEW=新签，RENEW=续签' AFTER sign_status,
  ADD COLUMN contract_medium varchar(32) NULL COMMENT '合同介质：ELECTRONIC=电子合同，PAPER=纸质合同' AFTER sign_type,
  ADD COLUMN notify_owner tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否通知业主' AFTER contract_medium;

ALTER TABLE owner_settlement_rule
  ADD COLUMN has_guaranteed_rent tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否有保底租金' AFTER guaranteed_rent_amount,
  ADD COLUMN management_fee_enabled tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用管理费' AFTER service_fee_value,
  ADD COLUMN management_fee_mode varchar(32) NULL COMMENT '管理费方式：RATIO/FIXED' AFTER management_fee_enabled,
  ADD COLUMN management_fee_value decimal(18,2) NULL COMMENT '管理费值' AFTER management_fee_mode,
  ADD COLUMN payment_fee_bear_type varchar(32) NULL COMMENT '支付手续费承担方式' AFTER bear_tax_type,
  ADD COLUMN settlement_timing varchar(32) NULL COMMENT '分账时间' AFTER payment_fee_bear_type,
  ADD COLUMN rent_free_enabled tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用免租规则' AFTER settlement_timing;

CREATE TABLE owner_settlement_item (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '公司ID',
  contract_id bigint NOT NULL COMMENT '合同ID',
  contract_house_id bigint NOT NULL COMMENT '合同房源ID',
  fee_type varchar(64) NOT NULL COMMENT '费用科目类型',
  item_name varchar(64) NOT NULL COMMENT '费用科目名称',
  transfer_enabled tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否转给业主',
  transfer_ratio decimal(5,2) DEFAULT NULL COMMENT '转给业主比例(0-100)',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  status int NOT NULL DEFAULT 1 COMMENT '状态',
  deleted tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  create_by bigint DEFAULT NULL COMMENT '创建人',
  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by bigint DEFAULT NULL COMMENT '更新人',
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_owner_settlement_item_company (company_id),
  KEY idx_owner_settlement_item_contract (contract_id),
  KEY idx_owner_settlement_item_house (contract_house_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='轻托管分账费用科目规则';

ALTER TABLE owner_rent_free_rule
  ADD COLUMN enabled tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用免租' AFTER contract_house_id;

ALTER TABLE owner_lease_rule
  ADD COLUMN handover_date date NULL COMMENT '交房日期' AFTER first_pay_date,
  ADD COLUMN usage_type varchar(64) NULL COMMENT '承租用途' AFTER handover_date;

CREATE TABLE owner_lease_fee (
  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  company_id bigint NOT NULL COMMENT '公司ID',
  contract_id bigint NOT NULL COMMENT '合同ID',
  fee_type varchar(64) NOT NULL COMMENT '费用科目类型',
  fee_name varchar(64) NOT NULL COMMENT '费用名称',
  fee_direction varchar(16) NOT NULL COMMENT '方向: IN/OUT',
  payment_method int DEFAULT NULL COMMENT '付款方式',
  price_method int DEFAULT NULL COMMENT '金额方式',
  price_input decimal(12,2) DEFAULT NULL COMMENT '金额或比例值',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序',
  remark varchar(255) DEFAULT NULL COMMENT '备注',
  status int NOT NULL DEFAULT 1 COMMENT '状态',
  deleted tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  create_by bigint DEFAULT NULL COMMENT '创建人',
  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by bigint DEFAULT NULL COMMENT '更新人',
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_owner_lease_fee_company (company_id),
  KEY idx_owner_lease_fee_contract (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='包租其他费用配置';
