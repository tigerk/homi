ALTER TABLE owner_contract_doc
  ADD COLUMN doc_status tinyint NOT NULL DEFAULT 1 COMMENT '文档状态：1=有效，-1=已作废' AFTER contract_medium,
  ADD COLUMN void_reason varchar(500) DEFAULT NULL COMMENT '作废原因' AFTER doc_status,
  ADD COLUMN void_by bigint DEFAULT NULL COMMENT '作废操作人ID' AFTER void_reason,
  ADD COLUMN void_at datetime DEFAULT NULL COMMENT '作废时间' AFTER void_by;
