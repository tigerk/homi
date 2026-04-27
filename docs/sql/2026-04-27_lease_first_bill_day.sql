ALTER TABLE `lease`
  ADD COLUMN `first_bill_day` tinyint DEFAULT NULL COMMENT '首期账单收租日: 0-跟随合同起租日, 1-跟随合同创建日' AFTER `payment_months`;
