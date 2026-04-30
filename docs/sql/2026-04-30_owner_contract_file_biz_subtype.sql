ALTER TABLE `file_attach`
  ADD COLUMN `biz_subtype` varchar(64) DEFAULT NULL COMMENT '业务子类型' AFTER `biz_type`,
  ADD KEY `idx_file_attach_biz_subtype` (`biz_id`, `biz_type`, `biz_subtype`);

UPDATE `file_attach`
JOIN `owner_contract` ON `owner_contract`.`id` = `file_attach`.`biz_id`
SET `file_attach`.`biz_subtype` = 'SIGNED_CONTRACT'
WHERE `file_attach`.`biz_type` = 'contract_file'
  AND `owner_contract`.`sign_status` = 1
  AND (`file_attach`.`biz_subtype` IS NULL OR `file_attach`.`biz_subtype` = '');

UPDATE `file_attach`
SET `biz_subtype` = 'OTHER'
WHERE `biz_type` = 'contract_file'
  AND (`biz_subtype` IS NULL OR `biz_subtype` = '');
