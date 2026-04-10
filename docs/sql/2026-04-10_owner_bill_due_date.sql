ALTER TABLE owner_bill
  ADD COLUMN due_date date DEFAULT NULL COMMENT '应付日期' AFTER bill_end,
  ADD KEY idx_owner_bill_due_date (due_date);
