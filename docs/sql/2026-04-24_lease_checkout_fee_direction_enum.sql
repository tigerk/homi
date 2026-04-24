ALTER TABLE `lease_checkout_fee`
  MODIFY COLUMN `fee_direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收支类型: IN-收入, OUT-支出';

UPDATE `lease_checkout_fee`
SET `fee_direction` = CASE `fee_direction`
  WHEN '1' THEN 'IN'
  WHEN '2' THEN 'OUT'
  ELSE `fee_direction`
END
WHERE `fee_direction` IS NOT NULL;
