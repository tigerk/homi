ALTER TABLE company_package
    ADD COLUMN register_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为注册默认套餐：1是 0否';

UPDATE company_package
SET register_default = 1
WHERE id = 1;
