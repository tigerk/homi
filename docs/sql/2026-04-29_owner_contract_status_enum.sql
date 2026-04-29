ALTER TABLE `owner_contract`
  MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT '合同状态: 0-待审核,1-待签字,2-已签字,3-已退房,-1-已作废';

UPDATE `owner_contract`
SET `status` = CASE
  WHEN `void_at` IS NOT NULL THEN -1
  WHEN `checkout_status` = 1 THEN 3
  WHEN `approval_status` <> 2 THEN 0
  WHEN `sign_status` = 1 THEN 2
  ELSE 1
END
WHERE `deleted` = 0;
