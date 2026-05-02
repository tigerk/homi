-- 租客签约合同多文档改造。
-- lease 表表示一次租客租约；lease_contract_doc 表表示该租约下的一份可签约合同文档。

RENAME TABLE lease_contract TO lease_contract_doc;

ALTER TABLE lease_contract_doc
  CHANGE COLUMN contract_code doc_no varchar(64) NOT NULL COMMENT '签约合同文档编号',
  ADD COLUMN company_id bigint DEFAULT NULL COMMENT 'SaaS企业ID' AFTER id,
  ADD COLUMN contract_medium varchar(32) DEFAULT 'ELECTRONIC' COMMENT '合同介质：ELECTRONIC-电子合同，PAPER-纸质合同' AFTER sign_status,
  ADD COLUMN doc_status tinyint NOT NULL DEFAULT 1 COMMENT '文档状态：1=有效，-1=已作废' AFTER contract_medium,
  ADD COLUMN void_reason varchar(500) DEFAULT NULL COMMENT '作废原因' AFTER doc_status,
  ADD COLUMN void_by bigint DEFAULT NULL COMMENT '作废操作人ID' AFTER void_reason,
  ADD COLUMN void_at datetime DEFAULT NULL COMMENT '作废时间' AFTER void_by,
  ADD COLUMN create_by bigint DEFAULT NULL COMMENT '创建人ID' AFTER deleted,
  ADD COLUMN create_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER create_by,
  ADD COLUMN update_by bigint DEFAULT NULL COMMENT '更新人ID' AFTER create_at,
  ADD COLUMN update_at datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER update_by;

UPDATE lease_contract_doc lcd
JOIN lease l ON l.id = lcd.lease_id
SET lcd.company_id = l.company_id
WHERE lcd.company_id IS NULL;

UPDATE file_attach
SET biz_type = 'lease_contract_doc'
WHERE biz_type = 'lease_contract';

CREATE INDEX idx_lease_contract_doc_lease_id ON lease_contract_doc (lease_id);
CREATE INDEX idx_lease_contract_doc_doc_status ON lease_contract_doc (doc_status);
