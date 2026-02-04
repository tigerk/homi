CREATE TABLE IF NOT EXISTS user_wechat (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  open_id VARCHAR(64) NOT NULL COMMENT '微信 openid',
  union_id VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
  app_id VARCHAR(64) NOT NULL COMMENT '小程序 appid',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_open_app (open_id, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户微信绑定表';
