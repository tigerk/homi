ALTER TABLE room
    ADD COLUMN delete_reason varchar(500) DEFAULT NULL COMMENT '删除原因' AFTER deleted,
    ADD COLUMN delete_by bigint DEFAULT NULL COMMENT '删除人ID' AFTER delete_reason,
    ADD COLUMN delete_at datetime DEFAULT NULL COMMENT '删除时间' AFTER delete_by,
    ADD COLUMN restore_reason varchar(500) DEFAULT NULL COMMENT '恢复原因' AFTER delete_at,
    ADD COLUMN restore_by bigint DEFAULT NULL COMMENT '恢复人ID' AFTER restore_reason,
    ADD COLUMN restore_at datetime DEFAULT NULL COMMENT '恢复时间' AFTER restore_by;

ALTER TABLE house
    ADD COLUMN delete_reason varchar(500) DEFAULT NULL COMMENT '删除原因' AFTER deleted,
    ADD COLUMN delete_by bigint DEFAULT NULL COMMENT '删除人ID' AFTER delete_reason,
    ADD COLUMN delete_at datetime DEFAULT NULL COMMENT '删除时间' AFTER delete_by,
    ADD COLUMN restore_reason varchar(500) DEFAULT NULL COMMENT '恢复原因' AFTER delete_at,
    ADD COLUMN restore_by bigint DEFAULT NULL COMMENT '恢复人ID' AFTER restore_reason,
    ADD COLUMN restore_at datetime DEFAULT NULL COMMENT '恢复时间' AFTER restore_by;
