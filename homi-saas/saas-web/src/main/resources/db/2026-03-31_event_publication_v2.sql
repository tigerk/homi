-- Spring Modulith event_publication 表从 v1 升级到 v2（兼容较老 MySQL）
-- 执行前请确认当前库已选中：USE homi;

SET @db_name = DATABASE();

SET @add_status = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db_name
              AND TABLE_NAME = 'EVENT_PUBLICATION'
              AND COLUMN_NAME = 'STATUS'
        ),
        'SELECT 1',
        'ALTER TABLE EVENT_PUBLICATION ADD COLUMN STATUS VARCHAR(20) NULL COMMENT ''事件状态'''
    )
);
PREPARE stmt FROM @add_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_completion_attempts = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db_name
              AND TABLE_NAME = 'EVENT_PUBLICATION'
              AND COLUMN_NAME = 'COMPLETION_ATTEMPTS'
        ),
        'SELECT 1',
        'ALTER TABLE EVENT_PUBLICATION ADD COLUMN COMPLETION_ATTEMPTS INT NULL COMMENT ''补偿/重试次数'''
    )
);
PREPARE stmt FROM @add_completion_attempts;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_last_resubmission_date = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = @db_name
              AND TABLE_NAME = 'EVENT_PUBLICATION'
              AND COLUMN_NAME = 'LAST_RESUBMISSION_DATE'
        ),
        'SELECT 1',
        'ALTER TABLE EVENT_PUBLICATION ADD COLUMN LAST_RESUBMISSION_DATE TIMESTAMP(6) NULL DEFAULT NULL COMMENT ''最后一次重投时间'''
    )
);
PREPARE stmt FROM @add_last_resubmission_date;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE EVENT_PUBLICATION
SET STATUS = 'COMPLETED'
WHERE COMPLETION_DATE IS NOT NULL
  AND (STATUS IS NULL OR STATUS = '');

UPDATE EVENT_PUBLICATION
SET STATUS = 'PROCESSING'
WHERE COMPLETION_DATE IS NULL
  AND (STATUS IS NULL OR STATUS = '');

UPDATE EVENT_PUBLICATION
SET COMPLETION_ATTEMPTS = 0
WHERE COMPLETION_ATTEMPTS IS NULL;
