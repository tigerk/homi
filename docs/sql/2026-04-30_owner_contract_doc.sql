CREATE TABLE IF NOT EXISTS `owner_contract_doc` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `owner_contract_id` bigint NOT NULL COMMENT '业主合同主单ID',
  `doc_no` varchar(64) NOT NULL COMMENT '签约合同文档编号',
  `contract_template_id` bigint DEFAULT NULL COMMENT '合同模板ID',
  `contract_content` longtext COMMENT '合同内容快照',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '签署状态：0=待签字，1=已签字',
  `contract_medium` varchar(32) DEFAULT NULL COMMENT '合同介质',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_contract_doc_company` (`company_id`),
  KEY `idx_owner_contract_doc_contract` (`owner_contract_id`),
  KEY `idx_owner_contract_doc_sign_status` (`sign_status`),
  KEY `idx_owner_contract_doc_create_at` (`create_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主合同签约文档';

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

UPDATE `file_attach` fa
JOIN `owner_contract_doc` d ON d.`owner_contract_id` = fa.`biz_id`
SET fa.`biz_type` = 'owner_contract_doc',
    fa.`biz_id` = d.`id`
WHERE fa.`biz_type` = 'contract_file'
  AND fa.`biz_subtype` = 'SIGNED_CONTRACT';
