INSERT INTO `owner_contract_doc` (
  `id`, `company_id`, `owner_contract_id`, `doc_no`, `contract_template_id`, `contract_content`,
  `sign_status`, `contract_medium`, `remark`, `deleted`, `create_by`, `create_at`, `update_by`, `update_at`
)
SELECT
  `id`, `company_id`, `id`, CONCAT(`contract_no`, '-DOC-01'), `contract_template_id`, `contract_content`,
  `sign_status`, `contract_medium`, `remark`, `deleted`, `create_by`, `create_at`, `update_by`, `update_at`
FROM `owner_contract`
WHERE `contract_no` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `owner_contract_doc` d WHERE d.`owner_contract_id` = `owner_contract`.`id`
  );

ALTER TABLE `owner_contract`
  DROP COLUMN `contract_template_id`,
  DROP COLUMN `contract_content`,
  DROP COLUMN `sign_status`,
  DROP COLUMN `contract_medium`;
