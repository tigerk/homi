ALTER TABLE `owner_lease_fee`
  ADD COLUMN `dict_data_id` bigint NULL COMMENT '费用字典ID' AFTER `contract_id`;
