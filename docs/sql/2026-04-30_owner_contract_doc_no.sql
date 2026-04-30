ALTER TABLE `owner_contract_doc`
  CHANGE COLUMN `contract_no` `doc_no` varchar(64) NOT NULL COMMENT '签约合同文档编号';

UPDATE `owner_contract_doc` d
JOIN `owner_contract` c ON c.`id` = d.`owner_contract_id`
SET d.`doc_no` = CONCAT(c.`contract_no`, '-DOC-01')
WHERE d.`doc_no` = c.`contract_no`;
