-- 退租表重命名（tenant_checkout -> lease_checkout）
-- 用于新环境或尚未执行 2026-02-06_migrate_lease.sql 的场景

START TRANSACTION;

RENAME TABLE tenant_checkout TO lease_checkout;
RENAME TABLE tenant_checkout_fee TO lease_checkout_fee;

COMMIT;
