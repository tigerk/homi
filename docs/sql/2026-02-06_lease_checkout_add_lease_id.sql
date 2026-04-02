-- 退租表补充 lease_id 并回填
-- 依赖租约表 lease 已建立，并能从 tenant_id 关联到 lease

START TRANSACTION;

ALTER TABLE `lease_checkout`
  ADD COLUMN `lease_id` bigint DEFAULT NULL COMMENT '租约ID' AFTER `tenant_id`,
  ADD KEY `idx_lease_id` (`lease_id`);

UPDATE lease_checkout lc
JOIN (
  SELECT tenant_id, MAX(id) AS lease_id
  FROM lease
  GROUP BY tenant_id
) l ON l.tenant_id = lc.tenant_id
SET lc.lease_id = l.lease_id
WHERE lc.lease_id IS NULL;

COMMIT;
