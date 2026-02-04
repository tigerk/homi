/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80404 (8.4.4)
 Source Host           : 127.0.0.1:3306
 Source Schema         : homi

 Target Server Type    : MySQL
 Target Server Version : 80404 (8.4.4)
 File Encoding         : 65001

 Date: 04/02/2026 10:00:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for EVENT_PUBLICATION
-- ----------------------------
DROP TABLE IF EXISTS `EVENT_PUBLICATION`;
CREATE TABLE `EVENT_PUBLICATION` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `listener_id` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_type` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `serialized_event` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `publication_date` timestamp NOT NULL,
  `completion_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_completion_date` (`completion_date`),
  KEY `idx_publication_date` (`publication_date`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for approval_action
-- ----------------------------
DROP TABLE IF EXISTS `approval_action`;
CREATE TABLE `approval_action` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_id` bigint NOT NULL COMMENT '实例ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `node_order` int NOT NULL COMMENT '节点序号',
  `node_name` varchar(100) DEFAULT NULL COMMENT '节点名称（冗余）',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) DEFAULT NULL COMMENT '审批人姓名（冗余）',
  `action` tinyint DEFAULT NULL COMMENT '操作：1=通过，2=驳回，3=转交',
  `remark` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `transfer_to_id` bigint DEFAULT NULL COMMENT '转交目标人ID',
  `operate_time` datetime DEFAULT NULL COMMENT '操作时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待审批，1=已审批，2=已跳过',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_instance_id` (`instance_id`),
  KEY `idx_approver` (`approver_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批动作表';

-- ----------------------------
-- Table structure for approval_flow
-- ----------------------------
DROP TABLE IF EXISTS `approval_flow`;
CREATE TABLE `approval_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流程ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `flow_code` varchar(64) NOT NULL COMMENT '流程编码（唯一标识）',
  `flow_name` varchar(100) NOT NULL COMMENT '流程名称',
  `biz_type` varchar(50) NOT NULL COMMENT '业务类型：TENANT_CHECKIN=租客入住，TENANT_CHECKOUT=退租，HOUSE_CREATE=房源录入，CONTRACT_SIGN=合同签署',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：false=停用，true=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_biz` (`company_id`,`biz_type`,`deleted`) COMMENT '每个公司每种业务只能有一个有效流程',
  KEY `idx_flow_code` (`flow_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批流程配置表';

-- ----------------------------
-- Table structure for approval_instance
-- ----------------------------
DROP TABLE IF EXISTS `approval_instance`;
CREATE TABLE `approval_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实例ID',
  `instance_no` varchar(64) NOT NULL COMMENT '审批单号',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `flow_id` bigint NOT NULL COMMENT '流程ID',
  `biz_type` varchar(50) NOT NULL COMMENT '业务类型',
  `biz_id` bigint NOT NULL COMMENT '业务ID（如 tenant_checkout.id）',
  `biz_code` varchar(64) DEFAULT NULL COMMENT '业务单号（冗余，便于展示）',
  `title` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审批标题',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `applicant_name` varchar(50) DEFAULT NULL COMMENT '申请人姓名（冗余）',
  `current_node_id` bigint DEFAULT NULL COMMENT '当前节点ID',
  `current_node_order` int DEFAULT '1' COMMENT '当前节点序号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待提交，1=审批中，2=已通过，3=已驳回，4=已撤回，5=已取消',
  `result_remark` varchar(500) DEFAULT NULL COMMENT '最终审批意见',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_no` (`instance_no`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批实例表';

-- ----------------------------
-- Table structure for approval_node
-- ----------------------------
DROP TABLE IF EXISTS `approval_node`;
CREATE TABLE `approval_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `flow_id` bigint NOT NULL COMMENT '流程ID',
  `node_name` varchar(100) NOT NULL COMMENT '节点名称（如：部门经理审批、财务审批）',
  `node_order` int NOT NULL COMMENT '节点顺序（从1开始）',
  `approver_type` tinyint NOT NULL COMMENT '审批人类型：1=指定用户，2=指定角色，3=部门主管，4=发起人自选',
  `approver_ids` json DEFAULT NULL COMMENT '审批人ID列表（用户ID或角色ID）',
  `multi_approve_type` tinyint NOT NULL DEFAULT '1' COMMENT '多人审批方式：1=或签（一人通过即可），2=会签（所有人通过）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_order` (`flow_id`,`node_order`,`deleted`),
  KEY `idx_flow_id` (`flow_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批节点配置表';

-- ----------------------------
-- Table structure for booking
-- ----------------------------
DROP TABLE IF EXISTS `booking`;
CREATE TABLE `booking` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预定 ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `tenant_type` tinyint NOT NULL COMMENT '租客类型：0=个人，1=企业',
  `tenant_name` varchar(100) NOT NULL COMMENT '客户姓名',
  `tenant_phone` varchar(30) NOT NULL COMMENT '联系电话',
  `booking_amount` decimal(12,2) NOT NULL COMMENT '预定金金额',
  `booking_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预定时间',
  `expiry_time` datetime NOT NULL COMMENT '预定到期时间（超过此时间未签合同可视为违约/过期）',
  `expected_lease_start` datetime DEFAULT NULL COMMENT '预计租赁开始时间',
  `expected_lease_end` datetime DEFAULT NULL COMMENT '预计租赁结束时间',
  `expected_rent_price` decimal(12,2) DEFAULT NULL COMMENT '谈定的意向租金',
  `room_ids` json NOT NULL COMMENT '预定房间 ids',
  `salesman_id` bigint NOT NULL COMMENT '业务人员ID',
  `booking_status` tinyint NOT NULL DEFAULT '1' COMMENT '预定状态：1=预定中，2=已转合同，3=客户违约（没收定金），4=业主违约（退还定金），5=已取消/过期',
  `tenant_id` bigint DEFAULT NULL COMMENT '转合同后关联的租客表 ID',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `cancel_reason` varchar(500) DEFAULT '' COMMENT '取消/过期原因备注',
  `cancel_time` datetime DEFAULT NULL COMMENT '实际操作取消的时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_phone` (`tenant_phone`),
  KEY `idx_expiry_time` (`expiry_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预定/定金表';

-- ----------------------------
-- Table structure for community
-- ----------------------------
DROP TABLE IF EXISTS `community`;
CREATE TABLE `community` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(128) NOT NULL COMMENT '小区名称',
  `alias` varchar(128) DEFAULT NULL COMMENT '小区别名/常用名',
  `city_id` bigint NOT NULL COMMENT '城市ID，对应的regionId',
  `province` varchar(64) NOT NULL COMMENT '省份',
  `city` varchar(64) NOT NULL COMMENT '城市',
  `district` varchar(64) NOT NULL COMMENT '区/县',
  `township` varchar(64) DEFAULT NULL COMMENT '街道/乡镇',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `business_area` varchar(128) DEFAULT NULL COMMENT '商圈',
  `adcode` varchar(12) DEFAULT NULL COMMENT '行政区划代码',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `built_year` year DEFAULT NULL COMMENT '建成年份',
  `building_count` int DEFAULT NULL COMMENT '楼栋数',
  `household_count` int DEFAULT NULL COMMENT '户数',
  `greening_rate` decimal(5,2) DEFAULT NULL COMMENT '绿化率(%)',
  `plot_ratio` decimal(5,2) DEFAULT NULL COMMENT '容积率',
  `property_company` varchar(128) DEFAULT NULL COMMENT '物业公司',
  `developer` varchar(128) DEFAULT NULL COMMENT '开发商',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住宅小区表';

-- ----------------------------
-- Table structure for company
-- ----------------------------
DROP TABLE IF EXISTS `company`;
CREATE TABLE `company` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `code` varchar(255) DEFAULT NULL COMMENT '公司编码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公司名称',
  `abbr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司简称',
  `uscc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司社会统一信用代码',
  `legal_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '法人姓名',
  `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '通信地址',
  `region_id` bigint DEFAULT NULL COMMENT '区域ID',
  `logo` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '公司LOGO',
  `website` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司网站',
  `contact_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人手机号',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `admin_user_id` bigint NOT NULL COMMENT '公司管理员ID',
  `account_count` int DEFAULT NULL COMMENT '账号数量',
  `nature` smallint NOT NULL COMMENT '公司性质 1：企业 2：个人',
  `package_id` bigint NOT NULL COMMENT '公司套餐id',
  `status` smallint NOT NULL COMMENT '状态（1正常，0禁用）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司表';

-- ----------------------------
-- Table structure for company_account
-- ----------------------------
DROP TABLE IF EXISTS `company_account`;
CREATE TABLE `company_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `balance` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额（元）',
  `frozen_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '冻结金额（元）',
  `total_recharge` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '累计充值（元）',
  `total_consume` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费（元）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1正常，2冻结，-1禁用',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0否，1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司账户表';

-- ----------------------------
-- Table structure for company_consume
-- ----------------------------
DROP TABLE IF EXISTS `company_consume`;
CREATE TABLE `company_consume` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `consume_no` varchar(64) NOT NULL COMMENT '消费流水号',
  `biz_type` varchar(50) NOT NULL COMMENT '业务类型：SMS/CONTRACT/PAYMENT/STORAGE',
  `biz_id` bigint DEFAULT NULL COMMENT '业务关联ID',
  `biz_no` varchar(100) DEFAULT NULL COMMENT '业务单号',
  `fee_item` varchar(100) NOT NULL COMMENT '费用项：短信费/合同费/支付手续费',
  `unit_price` decimal(10,4) NOT NULL COMMENT '单价（元）',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` decimal(15,2) NOT NULL COMMENT '消费金额（元）',
  `before_balance` decimal(15,2) NOT NULL COMMENT '消费前余额（元）',
  `after_balance` decimal(15,2) NOT NULL COMMENT '消费后余额（元）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1成功，2失败，3已退款',
  `refund_amount` decimal(15,2) DEFAULT '0.00' COMMENT '退款金额（元）',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `description` varchar(500) DEFAULT NULL COMMENT '消费描述',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0否，1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consume_no` (`consume_no`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_biz_type` (`biz_type`),
  KEY `idx_biz_id` (`biz_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_company_time` (`company_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司消费记录表';

-- ----------------------------
-- Table structure for company_fee_config
-- ----------------------------
DROP TABLE IF EXISTS `company_fee_config`;
CREATE TABLE `company_fee_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID，NULL表示全局配置',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `fee_code` varchar(50) NOT NULL COMMENT '费用编码：SMS/CONTRACT/PAYMENT',
  `fee_item` varchar(100) NOT NULL COMMENT '费用项目名称',
  `charge_type` tinyint NOT NULL COMMENT '计费方式：1按次，2按量，3按比例',
  `unit_price` decimal(10,4) NOT NULL COMMENT '单价（元）',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位：条/份/%',
  `min_charge` decimal(10,2) DEFAULT NULL COMMENT '最低收费（元）',
  `free_quota` int DEFAULT '0' COMMENT '免费额度',
  `package_id` bigint DEFAULT NULL COMMENT '关联套餐ID',
  `priority` int DEFAULT '0' COMMENT '优先级',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用，0禁用',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `description` text COMMENT '费用说明',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_company_code` (`company_id`,`fee_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司费用配置表';

-- ----------------------------
-- Table structure for company_init
-- ----------------------------
DROP TABLE IF EXISTS `company_init`;
CREATE TABLE `company_init` (
  `id` int NOT NULL COMMENT '主键ID',
  `dicts` json DEFAULT NULL COMMENT '字典默认数据',
  `ver` int DEFAULT NULL COMMENT '版本号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司默认数据表';

-- ----------------------------
-- Table structure for company_package
-- ----------------------------
DROP TABLE IF EXISTS `company_package`;
CREATE TABLE `company_package` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '套餐名称',
  `package_menus` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '关联菜单id',
  `status` smallint NOT NULL COMMENT '状态（0正常，-1禁用）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司套餐表';

-- ----------------------------
-- Table structure for company_recharge
-- ----------------------------
DROP TABLE IF EXISTS `company_recharge`;
CREATE TABLE `company_recharge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `order_no` varchar(64) NOT NULL COMMENT '充值订单号',
  `amount` decimal(15,2) NOT NULL COMMENT '充值金额（元）',
  `bonus_amount` decimal(15,2) DEFAULT '0.00' COMMENT '赠送金额（元）',
  `actual_amount` decimal(15,2) NOT NULL COMMENT '实际到账金额（元）',
  `before_balance` decimal(15,2) DEFAULT NULL COMMENT '充值前余额（元）',
  `after_balance` decimal(15,2) DEFAULT NULL COMMENT '充值后余额（元）',
  `pay_method` tinyint NOT NULL COMMENT '支付方式：1线上支付，2线下转账，3后台充值',
  `pay_channel` varchar(50) DEFAULT NULL COMMENT '支付渠道：alipay/wechat/bank',
  `transaction_no` varchar(100) DEFAULT NULL COMMENT '第三方交易流水号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1待支付，2支付中，3成功，4失败',
  `pay_time` datetime DEFAULT NULL COMMENT '支付完成时间',
  `remark` text COMMENT '备注',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `deleted` tinyint(1) DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司充值记录表';

-- ----------------------------
-- Table structure for company_user
-- ----------------------------
DROP TABLE IF EXISTS `company_user`;
CREATE TABLE `company_user` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_type` int DEFAULT NULL COMMENT '公司用户类型：20=管理员、21=员工',
  `roles` json DEFAULT NULL COMMENT '公司角色列表',
  `visible_dept_ids` json DEFAULT NULL COMMENT '可查看部门列表',
  `status` smallint NOT NULL COMMENT '状态（0=不启用，1=启用）',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户与公司的关联表';

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `id` int NOT NULL COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '参数键名',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '参数键值',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `type` smallint DEFAULT NULL COMMENT '是否系统内置（0否1是）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数配置表';

-- ----------------------------
-- Table structure for contract_template
-- ----------------------------
DROP TABLE IF EXISTS `contract_template`;
CREATE TABLE `contract_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '合同模板ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `contract_type` tinyint DEFAULT NULL COMMENT '合同模板类型：1=租客、2=业主、3=预定',
  `template_name` varchar(50) DEFAULT NULL COMMENT '合同模板名称',
  `template_content` text COMMENT '合同模板内容，包含模板变量占位符',
  `dept_ids` json DEFAULT NULL COMMENT '生效部门json',
  `status` tinyint DEFAULT '0' COMMENT '合同状态：0=未生效，1=生效中，-1=已作废',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '合同模板备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同模板表';

-- ----------------------------
-- Table structure for customer
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` bigint NOT NULL,
  `id_type` int NOT NULL,
  `id_card` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `customer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `emergency_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bank_payee` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bank_branch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户表';

-- ----------------------------
-- Table structure for delivery
-- ----------------------------
DROP TABLE IF EXISTS `delivery`;
CREATE TABLE `delivery` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL,
  `subject_type` enum('TENANT','OWNER') NOT NULL DEFAULT 'TENANT' COMMENT '主体类型：TENANT-租客, OWNER-业主',
  `subject_type_id` bigint unsigned NOT NULL COMMENT '对应主体ID (租客ID或业主ID)',
  `room_id` bigint unsigned NOT NULL COMMENT '房间ID',
  `handover_type` enum('CHECK_IN','CHECK_OUT') NOT NULL DEFAULT 'CHECK_IN' COMMENT '交割方向：CHECK_IN-迁入/接收, CHECK_OUT-迁出/交付',
  `status` tinyint DEFAULT '1' COMMENT '交割单状态: 0-草稿, 1-已签署/生效, -1-作废',
  `handover_date` date NOT NULL COMMENT '交割日期',
  `inspector_id` bigint unsigned DEFAULT NULL COMMENT '操作员/管家ID',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`,`company_id`) USING BTREE,
  KEY `idx_subject` (`subject_type`,`subject_type_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_handover_date` (`handover_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用物业交割主表';

-- ----------------------------
-- Table structure for delivery_item
-- ----------------------------
DROP TABLE IF EXISTS `delivery_item`;
CREATE TABLE `delivery_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `delivery_id` bigint unsigned NOT NULL COMMENT '关联交割主表ID',
  `item_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '交割项编码(字典数据项value)',
  `item_name` varchar(64) NOT NULL COMMENT '交割项名称',
  `item_category` enum('FACILITY','UTILITY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'FACILITY' COMMENT '项目分类: UTILITY-水电气,FACILITY-设施',
  `pre_value` varchar(100) DEFAULT NULL COMMENT '交割前数值/状态(对比参考)',
  `current_value` varchar(100) NOT NULL COMMENT '当前交付数值/状态',
  `item_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位(如: 度、m³、元、个)',
  `damaged` tinyint(1) DEFAULT '0' COMMENT '是否损坏/异常: 0-正常, 1-损坏',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注(如: 空调遥控器缺失)',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_delivery_id` (`delivery_id`),
  KEY `idx_item_code` (`item_code`),
  KEY `idx_category` (`item_category`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业交割明细表';

-- ----------------------------
-- Table structure for dept
-- ----------------------------
DROP TABLE IF EXISTS `dept`;
CREATE TABLE `dept` (
  `id` bigint NOT NULL COMMENT '主键',
  `company_id` bigint DEFAULT NULL COMMENT '公司Id',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父节点id',
  `supervisor_id` bigint DEFAULT NULL COMMENT '部门主管ID',
  `principal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门负责人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门负责人手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '父节点id路径',
  `sort_order` int DEFAULT NULL COMMENT '显示顺序',
  `status` smallint NOT NULL COMMENT '状态（1，0不启用）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `is_store` tinyint(1) DEFAULT NULL COMMENT '是否为门店',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ----------------------------
-- Table structure for dict
-- ----------------------------
DROP TABLE IF EXISTS `dict`;
CREATE TABLE `dict` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `company_id` bigint DEFAULT NULL COMMENT '公司id',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父节点',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` smallint DEFAULT NULL COMMENT '状态（0开启 1关闭）',
  `hidden` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否隐藏',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_code` (`company_id`,`dict_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=143 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典表';

-- ----------------------------
-- Table structure for dict_data
-- ----------------------------
DROP TABLE IF EXISTS `dict_data`;
CREATE TABLE `dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '数据项名称',
  `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '数据项Code',
  `sort_order` int DEFAULT NULL COMMENT '排序',
  `color` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '颜色值',
  `status` smallint DEFAULT NULL COMMENT '状态（1开启 0关闭）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1990055347385098328 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Table structure for file_attach
-- ----------------------------
DROP TABLE IF EXISTS `file_attach`;
CREATE TABLE `file_attach` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司 ID',
  `biz_type` varchar(64) NOT NULL COMMENT '业务类型，如 user_avatar, house_photo, contract_scan',
  `biz_id` bigint DEFAULT NULL COMMENT '关联的业务数据ID',
  `file_url` varchar(512) NOT NULL COMMENT '文件访问URL',
  `file_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'image/jpeg' COMMENT '文件类型，如 image/png, image/jpeg',
  `sort_order` int DEFAULT NULL COMMENT '排序',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记：0 否，1 是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_company` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务附件关联表';

-- ----------------------------
-- Table structure for file_meta
-- ----------------------------
DROP TABLE IF EXISTS `file_meta`;
CREATE TABLE `file_meta` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件存储路径或访问URL',
  `file_name` varchar(64) DEFAULT NULL COMMENT '文件名',
  `file_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件内容MD5',
  `file_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件类型，如 image/png, image/jpeg',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `storage_type` tinyint DEFAULT '0' COMMENT '存储方式：0-本地、1-oss, qiniu, s3 等',
  `is_used` tinyint NOT NULL DEFAULT '0' COMMENT '是否已被业务使用：0=未使用，1=已使用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hash` (`file_hash`),
  KEY `idx_file_hash` (`file_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件资源表（防孤儿文件）';

-- ----------------------------
-- Table structure for focus
-- ----------------------------
DROP TABLE IF EXISTS `focus`;
CREATE TABLE `focus` (
  `id` bigint NOT NULL,
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `focus_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目编号',
  `focus_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `region_id` bigint DEFAULT NULL COMMENT '区域ID',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目地址',
  `community_id` bigint DEFAULT NULL COMMENT '小区ID',
  `store_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门店联系电话',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `salesman_id` bigint DEFAULT NULL COMMENT '业务员ID',
  `facilities` json DEFAULT NULL COMMENT '设施、从字典dict_data获取并配置',
  `water` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '水',
  `electricity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '电',
  `heating` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供暖',
  `has_elevator` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否有电梯',
  `has_gas` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有燃气',
  `room_count` int NOT NULL DEFAULT '1' COMMENT '房间数 为0表示未分配房间',
  `house_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '房源描述、项目介绍',
  `business_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商圈介绍、广告语',
  `tags` json DEFAULT NULL COMMENT '标签',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '项目描述',
  `image_list` json DEFAULT NULL COMMENT '图片列表',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NOT NULL COMMENT '创建人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集中式项目';

-- ----------------------------
-- Table structure for focus_building
-- ----------------------------
DROP TABLE IF EXISTS `focus_building`;
CREATE TABLE `focus_building` (
  `id` bigint NOT NULL,
  `company_id` bigint NOT NULL COMMENT '公司id',
  `focus_id` bigint NOT NULL COMMENT '集中式ID',
  `building` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座栋',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单元',
  `house_count_per_floor` int DEFAULT NULL COMMENT '每层房源数',
  `house_prefix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '房号前缀',
  `number_length` smallint NOT NULL COMMENT '房号长度',
  `exclude_four` tinyint(1) NOT NULL COMMENT '去掉4',
  `floor_total` smallint NOT NULL COMMENT '总楼层',
  `closed_floors` json DEFAULT NULL COMMENT '关闭的楼层列表json',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL DEFAULT '0' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL DEFAULT '0' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集中楼栋表';

-- ----------------------------
-- Table structure for house
-- ----------------------------
DROP TABLE IF EXISTS `house`;
CREATE TABLE `house` (
  `id` bigint NOT NULL COMMENT '房源id',
  `house_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房源编号',
  `house_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房源名称',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `salesman_id` bigint DEFAULT NULL COMMENT '业务员ID',
  `lease_mode` smallint DEFAULT '1' COMMENT '房源租赁类型：1、集中式；2、整租、3、合租',
  `mode_ref_id` bigint NOT NULL DEFAULT '0' COMMENT '来源id，集中式为集中式id，整租、合租为community_id',
  `lease_mode_id` bigint NOT NULL COMMENT '来源id，集中式为集中式id，整租、合租为community_id',
  `community_id` bigint DEFAULT NULL COMMENT '小区ID',
  `building` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座栋',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单元',
  `door_number` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '门牌号，分散式独有',
  `house_layout_id` bigint DEFAULT NULL COMMENT '户型',
  `rental_type` tinyint NOT NULL DEFAULT '1' COMMENT '出租类型：1=整租，2=合租',
  `area` decimal(12,2) DEFAULT NULL COMMENT '套内面积',
  `direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '朝向',
  `decoration_type` int DEFAULT NULL COMMENT '装修类型：1=豪华装，2=简装，3=精装，4=毛坯，5=清水，6=简约，7=未装修',
  `floor` int DEFAULT NULL COMMENT '楼层',
  `floor_total` int DEFAULT NULL COMMENT '总楼层',
  `water` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '水',
  `electricity` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '电',
  `heating` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供暖',
  `has_elevator` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否有电梯',
  `has_gas` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有燃气',
  `property_fee` decimal(12,2) DEFAULT NULL COMMENT '物业费，每月',
  `heating_fee` decimal(12,2) DEFAULT NULL COMMENT '暖气费，每月',
  `mgmt_fee` decimal(12,2) DEFAULT NULL COMMENT '物业费，每月',
  `room_count` int NOT NULL DEFAULT '1' COMMENT '房间数 为0表示未分配房间',
  `rest_room_count` int NOT NULL DEFAULT '1' COMMENT '房间余量',
  `certificate_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权属证明及编号',
  `shared_owner` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否共有产权  0=否 1=是',
  `mortgaged` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否抵押  0=否 1=是',
  `customer_id` bigint DEFAULT NULL COMMENT '客户Id',
  `house_status` int NOT NULL DEFAULT '0' COMMENT '房源状态',
  `approval_status` tinyint NOT NULL DEFAULT '2' COMMENT '审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回',
  `locked` tinyint(1) DEFAULT '0' COMMENT '锁定状态：是否锁定',
  `closed` tinyint(1) DEFAULT '0' COMMENT '禁用状态：是否已禁用',
  `house_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '房源描述、项目介绍',
  `business_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商圈介绍、广告语',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniq_house_code` (`house_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房源表';

-- ----------------------------
-- Table structure for house_layout
-- ----------------------------
DROP TABLE IF EXISTS `house_layout`;
CREATE TABLE `house_layout` (
  `id` bigint NOT NULL COMMENT '主键id',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `lease_mode` int DEFAULT NULL COMMENT '房源租赁类型：1=集中式；2=分散式',
  `lease_mode_id` bigint NOT NULL COMMENT '房源租赁类型关联id',
  `layout_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '房型名称',
  `living_room` smallint DEFAULT NULL COMMENT '厅',
  `bathroom` smallint DEFAULT NULL COMMENT '卫',
  `kitchen` smallint DEFAULT NULL COMMENT '厨',
  `bedroom` smallint DEFAULT NULL COMMENT '室',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '标签',
  `facilities` json DEFAULT NULL COMMENT '设施、从字典dict_data获取并配置',
  `image_list` json DEFAULT NULL COMMENT '图片列表',
  `video_list` json DEFAULT NULL COMMENT '视频',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房型设置';

-- ----------------------------
-- Table structure for house_owner
-- ----------------------------
DROP TABLE IF EXISTS `house_owner`;
CREATE TABLE `house_owner` (
  `id` bigint NOT NULL COMMENT '主键',
  `house_id` bigint DEFAULT NULL COMMENT '房源ID',
  `certificate_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权属证明及编号',
  `shared_owner` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否共有产权  0=否 1=是',
  `mortgaged` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否抵押  0=否 1=是',
  `customer_id` bigint DEFAULT NULL COMMENT '客户Id',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房源业主表';

-- ----------------------------
-- Table structure for login_log
-- ----------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
  `id` bigint NOT NULL COMMENT '访问ID',
  `company_id` bigint DEFAULT NULL COMMENT '公司id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户账号',
  `login_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作系统',
  `status` smallint DEFAULT NULL COMMENT '登录状态（1=成功，0=失败）',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_company_token` (`company_id`,`login_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统访问记录';

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
  `id` bigint NOT NULL COMMENT '菜单ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由名称',
  `menu_type` smallint NOT NULL COMMENT '菜单类型（0代表菜单、1代表iframe、2代表外链、3代表按钮）',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单ID',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由参数',
  `sort_order` int DEFAULT NULL COMMENT '菜单排序（平台规定只有home路由的rank才能为0，所以后端在返回rank的时候需要从非0开始 点击查看更多）',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由重定向',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `extra_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '右侧菜单图标',
  `visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '菜单状态（1显示 0隐藏）',
  `auths` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权限标识',
  `enter_transition` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '进场动画',
  `leave_transition` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '离场动画',
  `platform_type` smallint NOT NULL DEFAULT '0' COMMENT '菜单所属平台（0后台 1前台）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `active_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `frame_src` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'iframe页面地址',
  `frame_loading` tinyint(1) DEFAULT NULL COMMENT '内嵌的iframe页面是否开启首次加载动画（0否 1是）',
  `keep_alive` tinyint(1) DEFAULT NULL COMMENT '路由组件缓存（开启 `true`、关闭 `false`）`可选',
  `hidden_tag` tinyint(1) DEFAULT NULL COMMENT '当前菜单名称或自定义信息禁止添加到标签页（默认`false`）',
  `fixed_tag` tinyint(1) DEFAULT NULL COMMENT '当前菜单名称是否固定显示在标签页且不可关闭（默认`false`）',
  `show_link` tinyint(1) DEFAULT NULL COMMENT '是否在菜单中显示（默认`true`）`可选',
  `show_parent` tinyint(1) DEFAULT NULL COMMENT '是否显示父级菜单 `可选`',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id` bigint NOT NULL COMMENT '公告ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` longblob COMMENT '公告内容',
  `type` smallint NOT NULL COMMENT '公告类型（1通知 2公告）',
  `status` smallint DEFAULT NULL COMMENT '公告状态（0正常 1关闭）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告表';

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模块标题',
  `operation_type` smallint DEFAULT NULL COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `operator_type` smallint DEFAULT NULL COMMENT '操作人类别（0其它 1后台用户 2前台用户）',
  `company_id` bigint DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作用户名',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作系统',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '浏览器类型',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求方式',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '请求URL',
  `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '主机地址',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作地点',
  `param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求参数',
  `json_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '返回参数',
  `status` smallint DEFAULT NULL COMMENT '操作状态（0：正常；-1：异常）',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误消息',
  `request_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT NULL COMMENT '消耗时间',
  PRIMARY KEY (`id`),
  KEY `idx_sol_bt` (`operation_type`) USING BTREE,
  KEY `idx_sol_ot` (`request_time`) USING BTREE,
  KEY `idx_sol_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2018537374242471939 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统日志记录表';

-- ----------------------------
-- Table structure for payment
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `order_id` bigint NOT NULL COMMENT '关联业务单据ID',
  `payer_type` tinyint NOT NULL COMMENT '付款方类型',
  `payer_id` bigint DEFAULT NULL COMMENT '付款方ID',
  `pay_no` varchar(64) NOT NULL COMMENT '平台支付流水号',
  `external_pay_no` varchar(128) DEFAULT NULL COMMENT '第三方支付流水号',
  `pay_type` tinyint NOT NULL COMMENT '交易类型：1=支付，2=退款',
  `pay_channel` tinyint DEFAULT NULL COMMENT '支付渠道：1=现金，2=转账，3=支付宝，4=微信，5=其他',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '交易金额',
  `currency` varchar(10) DEFAULT 'CNY' COMMENT '币种',
  `pay_time` datetime NOT NULL COMMENT '支付发生时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '支付确认时间',
  `pay_status` tinyint DEFAULT '0' COMMENT '支付状态：0=待确认，1=成功，2=失败，3=已退款',
  `remark` varchar(500) DEFAULT '' COMMENT '备注信息',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一交易流水表';

-- ----------------------------
-- Table structure for platform_menu
-- ----------------------------
DROP TABLE IF EXISTS `platform_menu`;
CREATE TABLE `platform_menu` (
  `id` bigint NOT NULL COMMENT '菜单ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由名称',
  `menu_type` smallint NOT NULL COMMENT '菜单类型（0代表菜单、1代表iframe、2代表外链、3代表按钮）',
  `parent_id` bigint DEFAULT NULL COMMENT '父菜单ID',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由参数',
  `sort_order` int DEFAULT NULL COMMENT '菜单排序（平台规定只有home路由的rank才能为0，所以后端在返回rank的时候需要从非0开始 点击查看更多）',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由重定向',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `extra_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '右侧菜单图标',
  `visible` tinyint(1) DEFAULT '1' COMMENT '菜单状态（1显示 0隐藏）',
  `auths` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权限标识',
  `enter_transition` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '进场动画',
  `leave_transition` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '离场动画',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `active_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `frame_src` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'iframe页面地址',
  `frame_loading` tinyint(1) DEFAULT NULL COMMENT '内嵌的iframe页面是否开启首次加载动画（0否 1是）',
  `keep_alive` tinyint(1) DEFAULT NULL COMMENT '路由组件缓存（开启 `true`、关闭 `false`）`可选',
  `hidden_tag` tinyint(1) DEFAULT NULL COMMENT '当前菜单名称或自定义信息禁止添加到标签页（默认`false`）',
  `fixed_tag` tinyint(1) DEFAULT NULL COMMENT '当前菜单名称是否固定显示在标签页且不可关闭（默认`false`）',
  `show_link` tinyint(1) DEFAULT NULL COMMENT '是否在菜单中显示（默认`true`）`可选',
  `show_parent` tinyint(1) DEFAULT NULL COMMENT '是否显示父级菜单 `可选`',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ----------------------------
-- Table structure for platform_role
-- ----------------------------
DROP TABLE IF EXISTS `platform_role`;
CREATE TABLE `platform_role` (
  `id` bigint NOT NULL COMMENT '角色ID',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `status` smallint NOT NULL COMMENT '角色状态（0正常 1停用）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '角色描述',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sr_role_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';

-- ----------------------------
-- Table structure for platform_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `platform_role_menu`;
CREATE TABLE `platform_role_menu` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色和菜单关联表';

-- ----------------------------
-- Table structure for platform_user
-- ----------------------------
DROP TABLE IF EXISTS `platform_user`;
CREATE TABLE `platform_user` (
  `id` bigint NOT NULL COMMENT '主键（用户id）',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户名（登录名）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '真实姓名',
  `id_type` smallint DEFAULT NULL COMMENT '证件类型',
  `id_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证件号',
  `user_type` int NOT NULL COMMENT '用户类型，参考UserTypeEnum',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱号',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '头像',
  `remark` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '简介',
  `gender` smallint NOT NULL COMMENT '性别（0未知，1男，2女）',
  `birthday` datetime DEFAULT NULL COMMENT '出生日期',
  `register_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '注册来源',
  `status` smallint NOT NULL COMMENT '状态（1=正常，0=禁用）',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for platform_user_role
-- ----------------------------
DROP TABLE IF EXISTS `platform_user_role`;
CREATE TABLE `platform_user_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户和角色关联表';

-- ----------------------------
-- Table structure for region
-- ----------------------------
DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` bigint NOT NULL,
  `parent_id` bigint NOT NULL COMMENT '父id',
  `deep` int NOT NULL COMMENT '层级',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '名称',
  `pinyin_prefix` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '拼音前缀',
  `pinyin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '拼音',
  `ext_id` bigint DEFAULT NULL COMMENT '扩展id',
  `ext_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '扩展名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='区域表';

-- ----------------------------
-- Table structure for rent_order
-- ----------------------------
DROP TABLE IF EXISTS `rent_order`;
CREATE TABLE `rent_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '交易订单ID',
  `company_id` bigint NOT NULL,
  `payer_type` tinyint NOT NULL COMMENT '支付主体类型：1=租客，2=房东，3=平台，4=第三方',
  `payer_id` bigint DEFAULT NULL COMMENT '支付主体ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单编号',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态：0=待支付，1=支付中，2=已支付，3=已取消',
  `payment_method` tinyint DEFAULT NULL COMMENT '支付方式',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一交易订单表（租客/房东/平台/第三方支付）';

-- ----------------------------
-- Table structure for rent_order_item
-- ----------------------------
DROP TABLE IF EXISTS `rent_order_item`;
CREATE TABLE `rent_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `bill_id` bigint NOT NULL,
  `allocated_amount` decimal(12,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易订单与账单关联表';

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint NOT NULL COMMENT '角色ID',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `company_id` bigint DEFAULT NULL COMMENT '公司id',
  `status` smallint NOT NULL DEFAULT '0' COMMENT '角色状态（0=未启用，1=启用）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '角色描述',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_role_code_company` (`code`,`company_id`) USING BTREE,
  KEY `idx_sr_role_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';

-- ----------------------------
-- Table structure for role_menu
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色和菜单关联表';

-- ----------------------------
-- Table structure for room
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `id` bigint NOT NULL,
  `company_id` bigint DEFAULT NULL,
  `house_id` bigint DEFAULT NULL,
  `keywords` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '搜索关键字',
  `floor` int DEFAULT NULL COMMENT '楼层',
  `room_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `room_type` int NOT NULL DEFAULT '0' COMMENT '房间类型',
  `price` decimal(10,2) DEFAULT NULL COMMENT '出房价格',
  `area` decimal(12,2) DEFAULT NULL COMMENT '面积',
  `direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '朝向',
  `vacancy_start_time` datetime DEFAULT NULL COMMENT '空置开始时间',
  `available_date` datetime DEFAULT NULL COMMENT '可出租日期',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `room_status` int NOT NULL DEFAULT '0' COMMENT '房间状态',
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '锁定状态：是否锁定',
  `closed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '禁用状态：是否已禁用',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '房间特色',
  `facilities` json DEFAULT NULL COMMENT '设施、从字典dict_data获取并配置',
  `image_list` json DEFAULT NULL COMMENT '图片列表',
  `video_list` json DEFAULT NULL COMMENT '视频',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间表';

-- ----------------------------
-- Table structure for room_detail
-- ----------------------------
DROP TABLE IF EXISTS `room_detail`;
CREATE TABLE `room_detail` (
  `id` bigint NOT NULL,
  `room_id` bigint DEFAULT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '房间特色',
  `facilities` json DEFAULT NULL COMMENT '设施、从字典dict_data获取并配置',
  `image_list` json DEFAULT NULL COMMENT '图片列表',
  `video_list` json DEFAULT NULL COMMENT '视频',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间扩展表';

-- ----------------------------
-- Table structure for room_price_config
-- ----------------------------
DROP TABLE IF EXISTS `room_price_config`;
CREATE TABLE `room_price_config` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_id` bigint unsigned NOT NULL COMMENT '房间ID',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '出房价格（单位：元）',
  `floor_price_method` tinyint unsigned DEFAULT NULL COMMENT '底价方式：1=固定金额，2=按比例',
  `floor_price_input` decimal(10,4) DEFAULT NULL COMMENT '底价录入值（金额或比例，具体由 low_price_method 决定）',
  `floor_price` decimal(10,2) DEFAULT NULL COMMENT '计算后的底价金额（冗余列可选）',
  `other_fees` json DEFAULT NULL COMMENT '其他费用（json）',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  KEY `idx_room_deleted` (`room_id`,`deleted`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间价格表';

-- ----------------------------
-- Table structure for room_price_plan
-- ----------------------------
DROP TABLE IF EXISTS `room_price_plan`;
CREATE TABLE `room_price_plan` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `room_id` bigint unsigned NOT NULL COMMENT '房间ID',
  `plan_name` varchar(120) NOT NULL DEFAULT '' COMMENT '租金方案名称',
  `plan_type` varchar(64) NOT NULL DEFAULT '' COMMENT '租金方案类型（如：长期/短租/节假日）',
  `price_ratio` decimal(5,2) DEFAULT NULL COMMENT '出房价格比例（百分比，如 12.50 表示 12.5%）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '出房价格（若为固定价格）',
  `other_fees` json DEFAULT NULL COMMENT '其他费用',
  `deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  KEY `idx_room_deleted` (`room_id`,`deleted`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间租金方案表';

-- ----------------------------
-- Table structure for scatter
-- ----------------------------
DROP TABLE IF EXISTS `scatter`;
CREATE TABLE `scatter` (
  `id` bigint NOT NULL COMMENT '房源id',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `living_room` int DEFAULT NULL COMMENT '厅',
  `bathroom` int DEFAULT NULL COMMENT '卫',
  `kitchen` int DEFAULT NULL COMMENT '厨',
  `bedroom` int DEFAULT NULL COMMENT '室',
  `floor` int DEFAULT NULL COMMENT '楼层',
  `direction` smallint DEFAULT NULL COMMENT '朝向',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分散式房源扩展表';

-- ----------------------------
-- Table structure for subway_line
-- ----------------------------
DROP TABLE IF EXISTS `subway_line`;
CREATE TABLE `subway_line` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) DEFAULT NULL COMMENT '线路名称',
  `city_id` varchar(100) DEFAULT NULL COMMENT '城市ID',
  `city_name` varchar(150) DEFAULT NULL COMMENT '城市名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='城市地铁线路表';

-- ----------------------------
-- Table structure for subway_station
-- ----------------------------
DROP TABLE IF EXISTS `subway_station`;
CREATE TABLE `subway_station` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL COMMENT '站名',
  `city_id` bigint DEFAULT NULL COMMENT '城市ID',
  `city_name` varchar(50) DEFAULT NULL COMMENT '城市名称',
  `line_id` varchar(100) DEFAULT NULL,
  `longitude` varchar(100) DEFAULT NULL COMMENT '经度',
  `latitude` varchar(100) DEFAULT NULL COMMENT '纬度',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='城市地铁站点表';

-- ----------------------------
-- Table structure for tenant
-- ----------------------------
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租客 ID',
  `parent_tenant_id` bigint DEFAULT NULL COMMENT '关联的租客 ID',
  `contract_nature` tinyint NOT NULL COMMENT '合同性质：1=新签，2=续签，3=转租，4=换房',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `dept_id` bigint NOT NULL COMMENT '部门 ID',
  `room_ids` json NOT NULL COMMENT '房间 ids',
  `tenant_type_id` bigint NOT NULL COMMENT '租客类型关联ID',
  `tenant_type` tinyint NOT NULL COMMENT '租客类型：0=个人，1=企业',
  `tenant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租客名称（冗余字段，便于查询）',
  `tenant_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租客联系电话（冗余字段）',
  `rent_price` decimal(12,2) NOT NULL COMMENT '租金价格',
  `deposit_months` int NOT NULL COMMENT '押金月数',
  `payment_months` int NOT NULL COMMENT '支付周期（月）',
  `lease_start` datetime NOT NULL COMMENT '租赁开始时间',
  `lease_end` datetime NOT NULL COMMENT '租赁结束时间',
  `check_in_time` datetime DEFAULT NULL COMMENT '实际入住时间',
  `check_out_time` datetime DEFAULT NULL COMMENT '实际搬离时间',
  `original_lease_start` datetime DEFAULT NULL COMMENT '初始录入租赁开始时间',
  `original_lease_end` datetime DEFAULT NULL COMMENT '初始录入租赁结束时间',
  `lease_duration_days` int DEFAULT NULL COMMENT '累计租房天数',
  `rent_due_type` tinyint DEFAULT NULL COMMENT '收租类型：1=提前，2=固定，3=延后',
  `rent_due_day` tinyint DEFAULT NULL COMMENT '固定收租日（1-31，0=当月最后一天）',
  `rent_due_offset_days` int DEFAULT NULL COMMENT '收租偏移天数（提前/延后）',
  `salesman_id` bigint NOT NULL COMMENT '业务人员ID',
  `helper_id` bigint DEFAULT NULL COMMENT '协助人员ID',
  `sign_status` tinyint DEFAULT '0' COMMENT '签约状态：0=待签字、1=已签字',
  `check_out_status` tinyint DEFAULT '0' COMMENT '租户退租状态：0=未退租、1=正常退、2=换房退、3=违约退、4=作废',
  `status` tinyint DEFAULT '0' COMMENT '租客状态：0=待审批，1=待签字，2=在租中，3=已退租，-1=已作废',
  `approval_status` tinyint DEFAULT '2' COMMENT '审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回',
  `tenant_source` bigint DEFAULT NULL COMMENT '租客来源',
  `deal_channel` bigint DEFAULT NULL COMMENT '成交渠道',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '合同备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_id` (`tenant_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客表';

-- ----------------------------
-- Table structure for tenant_bill
-- ----------------------------
DROP TABLE IF EXISTS `tenant_bill`;
CREATE TABLE `tenant_bill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `sort_order` int NOT NULL COMMENT '账单顺序',
  `bill_type` tinyint DEFAULT '1' COMMENT '账单类型：1=租金，2=押金，3=杂费，4=退租结算',
  `rent_period_start` datetime NOT NULL COMMENT '账单租期开始日期',
  `rent_period_end` datetime NOT NULL COMMENT '账单租期结束日期',
  `rental_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '租金金额',
  `deposit_amount` decimal(12,2) DEFAULT '0.00' COMMENT '押金金额',
  `other_fee_amount` decimal(12,2) DEFAULT '0.00' COMMENT '其他费用（如水电、物业）',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '账单合计金额',
  `due_date` datetime NOT NULL COMMENT '应收日期（根据 rent_due_xxx 计算）',
  `pay_time` datetime DEFAULT NULL COMMENT '实际支付日期',
  `pay_amount` decimal(12,2) DEFAULT NULL COMMENT '已支付金额',
  `pay_status` tinyint DEFAULT '0' COMMENT '支付状态：0=未支付，1=部分支付，2=已支付，3=逾期',
  `pay_channel` tinyint DEFAULT NULL COMMENT '支付方式：1=现金，2=转账，3=支付宝，4=微信，5=其他',
  `remark` varchar(500) DEFAULT '' COMMENT '备注信息',
  `valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效：1=有效，0=无效',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客账单表';

-- ----------------------------
-- Table structure for tenant_bill_other_fee
-- ----------------------------
DROP TABLE IF EXISTS `tenant_bill_other_fee`;
CREATE TABLE `tenant_bill_other_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bill_id` bigint NOT NULL COMMENT '账单ID（关联 tenant_bill.id）',
  `dict_data_id` bigint NOT NULL COMMENT '费用字典 ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '费用项目名称（如 租金、水费、电费）',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '费用金额（计算结果）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客账单其他费用明细表';

-- ----------------------------
-- Table structure for tenant_checkout
-- ----------------------------
DROP TABLE IF EXISTS `tenant_checkout`;
CREATE TABLE `tenant_checkout` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退租单ID',
  `checkout_code` varchar(64) NOT NULL COMMENT '退租单编号',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `delivery_id` bigint DEFAULT NULL COMMENT '交割单ID（关联 delivery.id，handover_type=CHECK_OUT）',
  `checkout_type` tinyint NOT NULL COMMENT '退租类型：1=正常到期，2=提前退租，3=换房退租，4=违约退租，5=协商解约',
  `checkout_reason` varchar(500) DEFAULT NULL COMMENT '退租原因',
  `lease_end` date NOT NULL COMMENT '合同到期日',
  `actual_checkout_date` date NOT NULL COMMENT '实际退租日',
  `deposit_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '押金总额',
  `deduction_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '扣款总额（欠租+水电+损坏+违约金等）',
  `refund_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '应退金额（多收租金+押金余额等）',
  `final_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '最终结算（正数=租客补缴，负数=退还租客）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=草稿，1=待确认，2=已完成，3=已取消',
  `approval_status` tinyint NOT NULL DEFAULT '2' COMMENT '审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回',
  `settlement_time` datetime DEFAULT NULL COMMENT '结算完成时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkout_code` (`checkout_code`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`,`deleted`) COMMENT '一个租客只能有一条有效退租单',
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退租主表';

-- ----------------------------
-- Table structure for tenant_checkout_fee
-- ----------------------------
DROP TABLE IF EXISTS `tenant_checkout_fee`;
CREATE TABLE `tenant_checkout_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `checkout_id` bigint NOT NULL COMMENT '退租单ID',
  `fee_type` tinyint NOT NULL COMMENT '费用类型：1=欠缴租金，2=欠缴杂费，3=水电燃气，4=物品损坏，5=违约金，6=清洁费，7=其他扣款，8=多收租金退还，9=押金退还',
  `fee_name` varchar(100) NOT NULL COMMENT '费用名称',
  `fee_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '费用金额（正数）',
  `fee_direction` tinyint NOT NULL DEFAULT '1' COMMENT '方向：1=扣款（租客应付），2=退款（退还租客）',
  `bill_id` bigint DEFAULT NULL COMMENT '关联账单ID（如有）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_checkout_id` (`checkout_id`),
  KEY `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退租费用明细表';

-- ----------------------------
-- Table structure for tenant_company
-- ----------------------------
DROP TABLE IF EXISTS `tenant_company`;
CREATE TABLE `tenant_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业租客ID',
  `company_name` varchar(100) NOT NULL COMMENT '企业名称',
  `uscc` varchar(18) NOT NULL COMMENT '统一社会信用代码',
  `legal_person` varchar(50) NOT NULL COMMENT '法定代表人',
  `legal_person_id_type` tinyint DEFAULT NULL COMMENT '法人证件类型',
  `legal_person_id_no` varchar(20) DEFAULT NULL COMMENT '法人证件号码',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(30) NOT NULL COMMENT '联系电话',
  `registered_address` varchar(200) DEFAULT NULL COMMENT '注册地址',
  `business_license_url` varchar(255) DEFAULT NULL COMMENT '营业执照附件',
  `tags` json DEFAULT NULL COMMENT '租客标签',
  `remark` varchar(500) DEFAULT '' COMMENT '租客备注',
  `status` tinyint DEFAULT '1' COMMENT '租客状态：0=停用，1=启用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uscc` (`uscc`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业租客信息表';

-- ----------------------------
-- Table structure for tenant_contract
-- ----------------------------
DROP TABLE IF EXISTS `tenant_contract`;
CREATE TABLE `tenant_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租客合同ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `contract_code` varchar(100) DEFAULT NULL COMMENT '合同编码',
  `contract_template_id` bigint NOT NULL COMMENT '合同模板ID',
  `contract_content` text COMMENT '合同内容',
  `sign_status` tinyint DEFAULT NULL COMMENT '签约状态：0=待签字、1=已签字',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '合同签约备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_code` (`contract_code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客合同表';

-- ----------------------------
-- Table structure for tenant_mate
-- ----------------------------
DROP TABLE IF EXISTS `tenant_mate`;
CREATE TABLE `tenant_mate` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '同住人ID',
  `tenant_id` bigint unsigned NOT NULL COMMENT '租客ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `gender` tinyint DEFAULT NULL COMMENT '性别：0=男，1=女',
  `id_type` tinyint NOT NULL COMMENT '证件类型：1=身份证，2=护照',
  `id_no` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件号码',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `tags` json DEFAULT NULL COMMENT '标签列表',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=停用，1=启用',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_id_no` (`id_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同住人信息表';

-- ----------------------------
-- Table structure for tenant_other_fee
-- ----------------------------
DROP TABLE IF EXISTS `tenant_other_fee`;
CREATE TABLE `tenant_other_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `dict_data_id` bigint NOT NULL COMMENT '其他费用 ID',
  `name` varchar(32) DEFAULT NULL COMMENT '其他费用名称',
  `payment_method` tinyint NOT NULL DEFAULT '1' COMMENT '付款方式（如：随房租付、按固定金额等）',
  `price_method` tinyint NOT NULL DEFAULT '1' COMMENT '价格计算方式',
  `price_input` decimal(10,2) NOT NULL DEFAULT '1.00' COMMENT '价格输入值',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客其他费用';

-- ----------------------------
-- Table structure for tenant_personal
-- ----------------------------
DROP TABLE IF EXISTS `tenant_personal`;
CREATE TABLE `tenant_personal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租客ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `name` varchar(50) NOT NULL COMMENT '租客姓名',
  `gender` tinyint DEFAULT NULL COMMENT '性别：1=男，2=女',
  `id_type` tinyint NOT NULL COMMENT '证件类型：0=身份证，1=护照，2=港澳通行证，3=台胞证',
  `id_no` varchar(20) NOT NULL COMMENT '证件号码',
  `phone` varchar(30) NOT NULL COMMENT '联系电话',
  `tags` json DEFAULT NULL COMMENT '租客标签',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '租客备注',
  `status` tinyint DEFAULT '1' COMMENT '租客状态：0=停用，1=启用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客个人信息表';

-- ----------------------------
-- Table structure for tenant_room
-- ----------------------------
DROP TABLE IF EXISTS `tenant_room`;
CREATE TABLE `tenant_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租客 ID',
  `room_id` bigint DEFAULT NULL COMMENT '房间 ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客房间表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL COMMENT '主键（用户id）',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户名（登录名）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '真实姓名',
  `id_type` smallint DEFAULT NULL COMMENT '证件类型',
  `id_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证件号',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱号',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '头像',
  `remark` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '简介',
  `gender` smallint DEFAULT '0' COMMENT '性别（0未知，1男，2女）',
  `birthday` datetime DEFAULT NULL COMMENT '出生日期',
  `register_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '注册来源',
  `status` smallint NOT NULL COMMENT '状态（0正常，-1禁用）',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一用户表';

SET FOREIGN_KEY_CHECKS = 1;
