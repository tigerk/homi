-- 当前库已经存在 source_type/source_id/source_no 时，单独执行这份即可删除旧字段。
-- 如果还没有执行 2026-05-07_finance_flow_source_fields.sql，请先执行那份完整迁移 SQL。

ALTER TABLE `finance_flow`
  DROP COLUMN `payment_flow_id`;
