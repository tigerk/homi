-- 租房 SaaS 租客/租约拆分迁移脚本（开发期）
-- 目标：tenant 仅保留租客身份，新增 lease 作为租约；账单/合同/费用转为 lease 关联
-- 注意：执行前请备份数据库。

START TRANSACTION;

-- 1) 备份旧 tenant 表
DROP TABLE IF EXISTS tenant_backup;
RENAME TABLE tenant TO tenant_backup;

-- 2) 重建 tenant（租客身份表）
CREATE TABLE `tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租客ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `tenant_type` tinyint NOT NULL COMMENT '租客类型：0=个人，1=企业',
  `tenant_type_id` bigint NOT NULL COMMENT '关联 tenant_personal / tenant_company 的 ID',
  `tenant_name` varchar(100) NOT NULL COMMENT '租客名称',
  `tenant_phone` varchar(30) NOT NULL COMMENT '租客联系电话',
  `status` tinyint DEFAULT '1' COMMENT '租客状态：0=停用，1=正常',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_id` (`tenant_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客表';

-- 3) 新建 lease（租约表）
CREATE TABLE `lease` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租约ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID（关联 tenant.id）',
  `parent_lease_id` bigint DEFAULT NULL COMMENT '上一份租约ID（续签/换房时关联）',
  `contract_nature` tinyint NOT NULL COMMENT '合同性质：1=新签，2=续签，3=转租，4=换房',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `room_ids` json NOT NULL COMMENT '房间IDs',
  `rent_price` decimal(12,2) NOT NULL COMMENT '租金价格',
  `deposit_months` int NOT NULL COMMENT '押金月数',
  `payment_months` int NOT NULL COMMENT '支付周期（月）',
  `lease_start` datetime NOT NULL COMMENT '租赁开始时间',
  `lease_end` datetime NOT NULL COMMENT '租赁结束时间',
  `check_in_time` datetime DEFAULT NULL COMMENT '实际入住时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '实际搬离时间',
  `original_lease_start` datetime DEFAULT NULL COMMENT '初始录入租赁开始时间',
  `original_lease_end` datetime DEFAULT NULL COMMENT '初始录入租赁结束时间',
  `lease_duration_days` int DEFAULT NULL COMMENT '累计租房天数',
  `rent_due_type` tinyint DEFAULT NULL COMMENT '收租类型：1=提前，2=固定，3=延后',
  `rent_due_day` tinyint DEFAULT NULL COMMENT '固定收租日',
  `rent_due_offset_days` int DEFAULT NULL COMMENT '收租偏移天数',
  `salesman_id` bigint NOT NULL COMMENT '业务人员ID',
  `helper_id` bigint DEFAULT NULL COMMENT '协助人员ID',
  `sign_status` tinyint DEFAULT '0' COMMENT '签约状态：0=待签字，1=已签字',
  `check_out_status` tinyint DEFAULT '0' COMMENT '退租状态：0=未退租，1=正常退，2=换房退，3=违约退，4=作废',
  `status` tinyint DEFAULT '0' COMMENT '租约状态：0=待审批，1=待签字，2=在租中，3=已退租，-1=已作废',
  `approval_status` tinyint DEFAULT '2' COMMENT '审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回',
  `tenant_source` bigint DEFAULT NULL COMMENT '租客来源',
  `deal_channel` bigint DEFAULT NULL COMMENT '成交渠道',
  `remark` varchar(500) DEFAULT '' COMMENT '合同备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  -- 迁移临时字段：旧 tenant.id
  `old_tenant_id` bigint DEFAULT NULL COMMENT '迁移用旧tenant id',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_parent_lease_id` (`parent_lease_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租约表';

-- 4) 迁移租客身份数据（按 tenant_type + tenant_type_id 去重）
INSERT INTO tenant (
  company_id, tenant_type, tenant_type_id, tenant_name, tenant_phone,
  status, deleted, create_by, create_time, update_by, update_time
)
SELECT
  tb.company_id,
  tb.tenant_type,
  tb.tenant_type_id,
  tb.tenant_name,
  tb.tenant_phone,
  1 AS status,
  MAX(COALESCE(tb.deleted, 0)) AS deleted,
  tb.create_by,
  MIN(tb.create_time) AS create_time,
  tb.update_by,
  MAX(tb.update_time) AS update_time
FROM tenant_backup tb
GROUP BY tb.company_id, tb.tenant_type, tb.tenant_type_id, tb.tenant_name, tb.tenant_phone, tb.create_by, tb.update_by;

-- 5) 迁移租约数据（一条旧 tenant 迁为一条 lease）
INSERT INTO lease (
  tenant_id, parent_lease_id, contract_nature, company_id, dept_id, room_ids,
  rent_price, deposit_months, payment_months, lease_start, lease_end,
  check_in_time, check_out_time, original_lease_start, original_lease_end,
  lease_duration_days, rent_due_type, rent_due_day, rent_due_offset_days,
  salesman_id, helper_id, sign_status, check_out_status, status, approval_status,
  tenant_source, deal_channel, remark, deleted, create_by, create_time, update_by, update_time,
  old_tenant_id
)
SELECT
  t.id AS tenant_id,
  NULL AS parent_lease_id,
  tb.contract_nature,
  tb.company_id,
  tb.dept_id,
  tb.room_ids,
  tb.rent_price,
  tb.deposit_months,
  tb.payment_months,
  tb.lease_start,
  tb.lease_end,
  tb.check_in_time,
  tb.check_out_time,
  tb.original_lease_start,
  tb.original_lease_end,
  tb.lease_duration_days,
  tb.rent_due_type,
  tb.rent_due_day,
  tb.rent_due_offset_days,
  tb.salesman_id,
  tb.helper_id,
  tb.sign_status,
  tb.check_out_status,
  tb.status,
  tb.approval_status,
  tb.tenant_source,
  tb.deal_channel,
  tb.remark,
  COALESCE(tb.deleted, 0) AS deleted,
  tb.create_by,
  tb.create_time,
  tb.update_by,
  tb.update_time,
  tb.id AS old_tenant_id
FROM tenant_backup tb
JOIN tenant t
  ON t.company_id = tb.company_id
 AND t.tenant_type = tb.tenant_type
 AND t.tenant_type_id = tb.tenant_type_id;

-- 6) 如果旧表存在 parent_tenant_id，可将其映射为 parent_lease_id
-- 如果旧表没有 parent_tenant_id，请注释掉此更新语句。
UPDATE lease l
JOIN tenant_backup tb ON tb.id = l.old_tenant_id
JOIN lease lp ON lp.old_tenant_id = tb.parent_tenant_id
SET l.parent_lease_id = lp.id;

-- 7) 重命名账单/合同/费用表（租约语义）
RENAME TABLE tenant_bill TO lease_bill;
RENAME TABLE tenant_contract TO lease_contract;
RENAME TABLE tenant_other_fee TO lease_other_fee;
RENAME TABLE tenant_bill_other_fee TO lease_bill_other_fee;

-- 7.1) 重命名退租相关表（租约语义）
RENAME TABLE tenant_checkout TO lease_checkout;
RENAME TABLE tenant_checkout_fee TO lease_checkout_fee;

-- 8) 修改 lease_bill 结构并回填 lease_id、结转字段
ALTER TABLE `lease_bill`
  ADD COLUMN `lease_id` bigint NOT NULL COMMENT '租约ID' AFTER `tenant_id`,
  ADD COLUMN `carry_over_from_bill_id` bigint DEFAULT NULL COMMENT '结转来源账单ID' AFTER `bill_type`,
  ADD COLUMN `carry_over_to_bill_id` bigint DEFAULT NULL COMMENT '结转目标账单ID' AFTER `carry_over_from_bill_id`,
  ADD KEY `idx_lease_id` (`lease_id`);

ALTER TABLE `lease_bill`
  MODIFY COLUMN `bill_type` tinyint DEFAULT '1' COMMENT '账单类型：1=租金，2=押金，3=杂费，4=退租结算，5=押金结转入，6=押金结转出';

UPDATE lease_bill tb
JOIN lease l ON l.old_tenant_id = tb.tenant_id
SET tb.lease_id = l.id;

-- 9) 修改 lease_contract 结构并回填 lease_id
ALTER TABLE `lease_contract`
  CHANGE COLUMN `tenant_id` `lease_id` bigint NOT NULL COMMENT '租约ID';

UPDATE lease_contract tc
JOIN lease l ON l.old_tenant_id = tc.lease_id
SET tc.lease_id = l.id;

-- 10) 修改 lease_other_fee 结构并回填 lease_id
ALTER TABLE `lease_other_fee`
  CHANGE COLUMN `tenant_id` `lease_id` bigint NOT NULL COMMENT '租约ID';

UPDATE lease_other_fee tof
JOIN lease l ON l.old_tenant_id = tof.lease_id
SET tof.lease_id = l.id;

-- 11) 退租表补充 lease_id 并回填
ALTER TABLE `lease_checkout`
  ADD COLUMN `lease_id` bigint DEFAULT NULL COMMENT '租约ID' AFTER `tenant_id`,
  ADD KEY `idx_lease_id` (`lease_id`);

UPDATE lease_checkout lc
JOIN lease l ON l.old_tenant_id = lc.tenant_id
SET lc.lease_id = l.id;

-- 12) 清理临时字段
ALTER TABLE `lease` DROP COLUMN `old_tenant_id`;

COMMIT;
