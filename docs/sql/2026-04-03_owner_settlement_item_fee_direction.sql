ALTER TABLE owner_settlement_item
ADD COLUMN fee_direction varchar(16) NOT NULL DEFAULT 'IN' COMMENT '收支方向: IN/OUT' AFTER contract_house_id;
