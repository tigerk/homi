CREATE TABLE trial_application
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    phone         VARCHAR(20)  NOT NULL COMMENT '手机号',
    region_id     BIGINT       NOT NULL COMMENT '城市区域ID',
    city_name     VARCHAR(64)  NOT NULL COMMENT '城市名称',
    usage_remark  VARCHAR(500) NULL COMMENT '如何使用系统',
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0申请中 1已通过 2已拒绝',
    handle_remark VARCHAR(500) NULL COMMENT '处理备注',
    deleted       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by     BIGINT       NULL COMMENT '创建人',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by     BIGINT       NULL COMMENT '更新人',
    PRIMARY KEY (id),
    KEY idx_trial_application_phone (phone),
    KEY idx_trial_application_status (status),
    KEY idx_trial_application_region_id (region_id)
) COMMENT ='试用申请表';
