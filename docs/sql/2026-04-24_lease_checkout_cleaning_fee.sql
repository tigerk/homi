ALTER TABLE `lease_checkout`
  ADD COLUMN `add_cleaning_fee` tinyint(1) DEFAULT 0 COMMENT '是否加收房屋清洁费' AFTER `final_amount`,
  ADD COLUMN `cleaning_fee_amount` decimal(10,2) DEFAULT NULL COMMENT '房屋清洁费金额' AFTER `add_cleaning_fee`;

ALTER TABLE `lease_checkout_fee`
  MODIFY COLUMN `fee_type` int DEFAULT NULL COMMENT '费用类型（LeaseBillTypeEnum：租金/押金/其他费用）';
