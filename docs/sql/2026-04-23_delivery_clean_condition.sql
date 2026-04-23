ALTER TABLE `delivery`
ADD COLUMN `clean_condition` varchar(255) DEFAULT NULL COMMENT '清洁情况' AFTER `remark`;
