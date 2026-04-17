RENAME TABLE `owner_settlement_item` TO `owner_settlement_fee`;

ALTER TABLE `owner_settlement_fee`
  ADD COLUMN `dict_data_id` bigint NULL COMMENT '费用字典数据项ID' AFTER `fee_type`;
