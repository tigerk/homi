ALTER TABLE owner_bill
  ADD COLUMN bill_biz_type varchar(64) DEFAULT NULL COMMENT '账单业务类型' AFTER bill_no;
