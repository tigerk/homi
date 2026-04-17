ALTER TABLE `owner_payable_bill`
  DROP INDEX `uk_owner_payable_bill_period`,
  DROP INDEX `idx_owner_payable_bill_subject`,
  DROP COLUMN `subject_type`,
  DROP COLUMN `subject_id`,
  ADD UNIQUE KEY `uk_owner_payable_bill_period` (`company_id`, `contract_id`, `bill_start_date`, `bill_end_date`, `deleted`);

ALTER TABLE `owner_payable_bill_fee`
  DROP INDEX `idx_owner_payable_bill_fee_subject`,
  DROP COLUMN `subject_type`,
  DROP COLUMN `subject_id`;
