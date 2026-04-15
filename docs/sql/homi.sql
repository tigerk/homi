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

 Date: 15/04/2026 18:01:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
  `operate_at` datetime DEFAULT NULL COMMENT '操作时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待审批，1=已审批，2=已跳过',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_instance_id` (`instance_id`),
  KEY `idx_approver` (`approver_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批动作表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_biz` (`company_id`,`biz_type`,`deleted`) COMMENT '每个公司每种业务只能有一个有效流程',
  KEY `idx_flow_code` (`flow_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批流程配置表';

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
  `finish_at` datetime DEFAULT NULL COMMENT '完成时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_no` (`instance_no`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批实例表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_order` (`flow_id`,`node_order`,`deleted`),
  KEY `idx_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批节点配置表';

-- ----------------------------
-- Table structure for biz_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `biz_operate_log`;
CREATE TABLE `biz_operate_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `biz_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型: OWNER_PAYABLE_BILL/OWNER_SETTLEMENT_BILL/OWNER_CONTRACT/LEASE_BILL等',
  `biz_id` bigint NOT NULL COMMENT '业务主键ID',
  `operate_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型: CREATE/UPDATE/CANCEL/PAY/APPROVE/REJECT等',
  `operate_desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作描述',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `before_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '操作前快照(JSON)',
  `after_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '操作后快照(JSON)',
  `extra_data` longtext COLLATE utf8mb4_unicode_ci COMMENT '扩展数据(JSON)',
  `source_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人名称',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除: 0否 1是',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz_operate_log_biz` (`company_id`,`biz_type`,`biz_id`),
  KEY `idx_biz_operate_log_source` (`company_id`,`source_type`,`source_id`),
  KEY `idx_biz_operate_log_operator` (`company_id`,`operator_id`),
  KEY `idx_biz_operate_log_create_at` (`company_id`,`create_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用业务操作日志';

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
  `booking_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预定时间',
  `expiry_at` datetime NOT NULL COMMENT '预定到期时间（超过此时间未签合同可视为违约/过期）',
  `expected_lease_start` datetime DEFAULT NULL COMMENT '预计租赁开始时间',
  `expected_lease_end` datetime DEFAULT NULL COMMENT '预计租赁结束时间',
  `expected_rent_price` decimal(12,2) DEFAULT NULL COMMENT '谈定的意向租金',
  `room_ids` json NOT NULL COMMENT '预定房间 ids',
  `salesman_id` bigint NOT NULL COMMENT '业务人员ID',
  `booking_status` tinyint NOT NULL DEFAULT '1' COMMENT '预定状态：1=预定中，2=已转合同，3=客户违约（没收定金），4=业主违约（退还定金），5=已取消/过期',
  `lease_id` bigint DEFAULT NULL COMMENT '转合同后关联的租约ID',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `cancel_reason` varchar(500) DEFAULT '' COMMENT '取消/过期原因备注',
  `cancel_at` datetime DEFAULT NULL COMMENT '实际操作取消的时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_phone` (`tenant_phone`),
  KEY `idx_expiry_at` (`expiry_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预定/定金表';

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
  `adcode` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '行政区划代码',
  `district` varchar(64) NOT NULL COMMENT '区/县',
  `township` varchar(64) DEFAULT NULL COMMENT '街道/乡镇',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `business_area` varchar(128) DEFAULT NULL COMMENT '商圈',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住宅小区表';

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
  `nature` smallint NOT NULL COMMENT '公司性质 1：企业 2：个人',
  `package_id` bigint NOT NULL COMMENT '公司套餐id',
  `house_count` int NOT NULL DEFAULT '0' COMMENT '房源数量',
  `status` smallint NOT NULL COMMENT '状态（1正常，0禁用）',
  `dict_ver` int NOT NULL DEFAULT '0' COMMENT '公司字典当前版本',
  `dict_sync_at` datetime DEFAULT NULL COMMENT '字典最后同步时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司表';

-- ----------------------------
-- Table structure for company_consume
-- ----------------------------
DROP TABLE IF EXISTS `company_consume`;
CREATE TABLE `company_consume` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `consume_no` varchar(64) NOT NULL COMMENT '消费流水号',
  `order_id` bigint DEFAULT NULL COMMENT '关联购买订单ID',
  `product_code` varchar(50) NOT NULL COMMENT '商品编码',
  `biz_type` varchar(50) NOT NULL COMMENT '业务类型：SMS/CONTRACT/ID_AUTH/HOUSE/...',
  `biz_id` bigint DEFAULT NULL COMMENT '业务关联ID',
  `biz_no` varchar(100) DEFAULT NULL COMMENT '业务单号',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '消耗数量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1成功，2失败，3已退还',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consume_no` (`consume_no`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_biz_type` (`biz_type`),
  KEY `idx_biz_id` (`biz_id`),
  KEY `idx_create_at` (`create_at`),
  KEY `idx_company_time` (`company_id`,`create_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业配额消费记录表';

-- ----------------------------
-- Table structure for company_dict_sync_log
-- ----------------------------
DROP TABLE IF EXISTS `company_dict_sync_log`;
CREATE TABLE `company_dict_sync_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `from_ver` int NOT NULL COMMENT '起始版本',
  `to_ver` int NOT NULL COMMENT '目标版本',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0进行中 1成功 -1失败',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功处理条数',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT '失败条数',
  `error_msg` text COMMENT '错误信息',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_dict_sync_log_company` (`company_id`),
  KEY `idx_company_dict_sync_log_to_ver` (`to_ver`),
  KEY `idx_company_dict_sync_log_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司字典同步日志';

-- ----------------------------
-- Table structure for company_order
-- ----------------------------
DROP TABLE IF EXISTS `company_order`;
CREATE TABLE `company_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `order_no` varchar(64) NOT NULL COMMENT '购买订单号',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(50) NOT NULL COMMENT '商品编码（冗余）',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称（冗余）',
  `unit_price` decimal(10,4) NOT NULL COMMENT '下单时单价（元）',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_amount` decimal(15,2) NOT NULL COMMENT '订单总金额（元）',
  `expire_date` date DEFAULT NULL COMMENT '配额有效期（NULL表示永不过期）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态：1待支付，2已支付，3已取消，4已退款',
  `cancel_at` datetime DEFAULT NULL COMMENT '取消时间',
  `refund_amount` decimal(15,2) DEFAULT '0.00' COMMENT '退款金额（元）',
  `refund_at` datetime DEFAULT NULL COMMENT '退款时间',
  `pay_method` tinyint DEFAULT NULL COMMENT '支付方式：1线上支付，2线下转账，3后台代付',
  `pay_channel` varchar(50) DEFAULT NULL COMMENT '支付渠道：alipay/wechat/bank',
  `transaction_no` varchar(100) DEFAULT NULL COMMENT '第三方交易流水号',
  `pay_at` datetime DEFAULT NULL COMMENT '支付完成时间',
  `notify_at` datetime DEFAULT NULL COMMENT '支付回调通知时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID（后台代购时填写）',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_status` (`status`),
  KEY `idx_pay_channel` (`pay_channel`),
  KEY `idx_create_at` (`create_at`),
  KEY `idx_company_time` (`company_id`,`create_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业购买订单表';

-- ----------------------------
-- Table structure for company_package
-- ----------------------------
DROP TABLE IF EXISTS `company_package`;
CREATE TABLE `company_package` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '套餐名称',
  `package_menus` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '关联菜单id',
  `month_price` decimal(10,2) NOT NULL COMMENT '月付单价',
  `year_price` decimal(10,2) DEFAULT NULL COMMENT '年付总价（NULL表示无年付优惠）',
  `house_count` int DEFAULT NULL COMMENT '房源数量',
  `register_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为注册默认套餐：1是 0否',
  `status` smallint NOT NULL COMMENT '状态（0正常，-1禁用）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司套餐表';

-- ----------------------------
-- Table structure for company_package_order
-- ----------------------------
DROP TABLE IF EXISTS `company_package_order`;
CREATE TABLE `company_package_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称（冗余）',
  `house_count` int NOT NULL COMMENT '套餐包含房源数（冗余）',
  `order_type` tinyint NOT NULL COMMENT '订单类型：1首购，2续费，3升级',
  `months` int NOT NULL COMMENT '购买月数',
  `start_date` date DEFAULT NULL COMMENT '生效日期',
  `end_date` date DEFAULT NULL COMMENT '到期日期',
  `from_package_id` bigint DEFAULT NULL COMMENT '升级前套餐ID（升级时填写）',
  `upgrade_credit` decimal(10,2) DEFAULT '0.00' COMMENT '升级时原套餐折抵金额',
  `unit_price` decimal(10,2) NOT NULL COMMENT '下单时套餐月单价（元）',
  `total_amount` decimal(15,2) NOT NULL COMMENT '应付金额（元）',
  `actual_amount` decimal(15,2) NOT NULL COMMENT '实付金额（元，扣除折抵后）',
  `pay_method` tinyint DEFAULT NULL COMMENT '支付方式：1余额，2线上，3线下，4后台',
  `pay_channel` varchar(50) DEFAULT NULL COMMENT '支付渠道：alipay/wechat/bank',
  `transaction_no` varchar(100) DEFAULT NULL COMMENT '第三方交易流水号',
  `pay_at` datetime DEFAULT NULL COMMENT '支付时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1待支付，2已支付，3已取消，4已退款',
  `cancel_at` datetime DEFAULT NULL COMMENT '取消时间',
  `refund_amount` decimal(15,2) DEFAULT '0.00' COMMENT '退款金额（元）',
  `refund_at` datetime DEFAULT NULL COMMENT '退款时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID（后台代购时填写）',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_package_id` (`package_id`),
  KEY `idx_status` (`status`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_company_time` (`company_id`,`create_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='套餐订阅订单表';

-- ----------------------------
-- Table structure for company_product
-- ----------------------------
DROP TABLE IF EXISTS `company_product`;
CREATE TABLE `company_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` varchar(50) NOT NULL COMMENT '商品编码：HOUSE/CONTRACT/SMS/ID_AUTH/ZHIMA_HOUSE/YUMENG_HOUSE/ALIPAY_HOUSE/ZHIMA_CREDIT',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `unit` varchar(20) NOT NULL COMMENT '单位：间/份/次/条/个',
  `unit_price` decimal(10,4) NOT NULL COMMENT '单价（元）',
  `min_quantity` int NOT NULL DEFAULT '1' COMMENT '最小购买数量',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '商品介绍',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1上架，0下架',
  `deleted` tinyint(1) DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业商品表';

-- ----------------------------
-- Table structure for company_quota
-- ----------------------------
DROP TABLE IF EXISTS `company_quota`;
CREATE TABLE `company_quota` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `product_code` varchar(50) NOT NULL COMMENT '商品编码',
  `total_quota` int NOT NULL DEFAULT '0' COMMENT '总配额',
  `used_quota` int NOT NULL DEFAULT '0' COMMENT '已用配额',
  `frozen_quota` int NOT NULL DEFAULT '0' COMMENT '冻结中的配额（操作进行中）',
  `expire_date` date DEFAULT NULL COMMENT '有效期（NULL表示永不过期）',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_product` (`company_id`,`product_code`),
  KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业配额表';

-- ----------------------------
-- Table structure for company_subscription
-- ----------------------------
DROP TABLE IF EXISTS `company_subscription`;
CREATE TABLE `company_subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `package_id` bigint NOT NULL COMMENT '当前套餐ID',
  `house_count` int NOT NULL COMMENT '当前套餐房源数',
  `start_date` date NOT NULL COMMENT '生效日期',
  `end_date` date NOT NULL COMMENT '到期日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1有效，2已过期',
  `last_order_id` bigint DEFAULT NULL COMMENT '最近订单ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_id` (`company_id`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业当前订阅状态表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `type` smallint DEFAULT NULL COMMENT '是否系统内置（0否1是）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='参数配置表';

-- ----------------------------
-- Table structure for contract_seal
-- ----------------------------
DROP TABLE IF EXISTS `contract_seal`;
CREATE TABLE `contract_seal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `seal_type` tinyint NOT NULL COMMENT '印章类型:1=企业,2=个人',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源:1=自有图片,2=法大大,3=E签宝,4=其他第三方',
  `company_name` varchar(255) NOT NULL COMMENT '公司名称',
  `company_uscc` varchar(255) DEFAULT NULL COMMENT '公司社会统一信用代码',
  `legal_person` varchar(50) NOT NULL COMMENT '法人姓名',
  `legal_person_id_type` varchar(50) NOT NULL COMMENT '法人证件类型',
  `legal_person_id_no` varchar(32) DEFAULT NULL COMMENT '法人证件号',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态:0=待审核,1=正常,2=已禁用,3=审核失败',
  `remark` text COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_create_at` (`create_at`),
  KEY `idx_company_time` (`company_id`,`create_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业电子印章';

-- ----------------------------
-- Table structure for contract_seal_provider
-- ----------------------------
DROP TABLE IF EXISTS `contract_seal_provider`;
CREATE TABLE `contract_seal_provider` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `seal_id` bigint NOT NULL COMMENT '关联主表ID',
  `account_id` varchar(255) DEFAULT NULL COMMENT '服务商平台的账号/企业ID',
  `provider_seal_id` varchar(255) DEFAULT NULL COMMENT '服务商平台的印章ID',
  `auth_status` tinyint NOT NULL DEFAULT '0' COMMENT '认证状态:0=未认证,1=认证中,2=已认证,3=失败',
  `auth_at` datetime DEFAULT NULL COMMENT '认证完成时间',
  `expire_at` datetime DEFAULT NULL COMMENT '授权到期时间',
  `extra` json DEFAULT NULL COMMENT '各服务商差异化字段,JSON存储',
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seal_id` (`seal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方签章供应商信息';

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
  `seal_id` bigint DEFAULT NULL COMMENT '电子签章ID',
  `status` tinyint DEFAULT '0' COMMENT '合同状态：0=未生效，1=生效中，-1=已作废',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '合同模板备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同模板表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_delivery_id` (`delivery_id`),
  KEY `idx_item_code` (`item_code`),
  KEY `idx_category` (`item_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物业交割明细表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `from_template` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否来自模板（1是 0否）',
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否锁定不被模板覆盖（1是 0否）',
  `template_ver` int DEFAULT NULL COMMENT '最后同步模板版本',
  `sync_at` datetime DEFAULT NULL COMMENT '最后模板同步时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_code` (`company_id`,`dict_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典表';

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
  `deletable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可删除（1可删除 0不可删除）',
  `from_template` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否来自模板（1是 0否）',
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否锁定不被模板覆盖（1是 0否）',
  `template_ver` int DEFAULT NULL COMMENT '最后同步模板版本',
  `sync_at` datetime DEFAULT NULL COMMENT '最后模板同步时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_data_company_dict_value` (`company_id`,`dict_id`,`value`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ----------------------------
-- Table structure for dict_data_template
-- ----------------------------
DROP TABLE IF EXISTS `dict_data_template`;
CREATE TABLE `dict_data_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_code` varchar(100) NOT NULL COMMENT '归属字典编码',
  `name` varchar(100) NOT NULL COMMENT '数据项名称',
  `value` varchar(100) NOT NULL COMMENT '数据项值',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `color` varchar(50) DEFAULT NULL COMMENT '颜色值',
  `status` smallint NOT NULL DEFAULT '1' COMMENT '状态（1开启 0关闭）',
  `deletable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可删除（1可删 0不可删）',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '模板项是否启用',
  `ver` int NOT NULL COMMENT '模板版本号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_data_template_code_value_ver` (`dict_code`,`value`,`ver`),
  KEY `idx_dict_data_template_ver` (`ver`),
  KEY `idx_dict_data_template_dict_code` (`dict_code`),
  KEY `idx_dict_data_template_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据模板表';

-- ----------------------------
-- Table structure for dict_template
-- ----------------------------
DROP TABLE IF EXISTS `dict_template`;
CREATE TABLE `dict_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_code` varchar(100) NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `parent_code` varchar(100) NOT NULL DEFAULT '0' COMMENT '父字典编码，0为根',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` smallint NOT NULL DEFAULT '1' COMMENT '状态（1开启 0关闭）',
  `hidden` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否隐藏',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '模板项是否启用',
  `ver` int NOT NULL COMMENT '模板版本号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_template_code_ver` (`dict_code`,`ver`),
  KEY `idx_dict_template_ver` (`ver`),
  KEY `idx_dict_template_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典模板表';

-- ----------------------------
-- Table structure for EVENT_PUBLICATION
-- ----------------------------
DROP TABLE IF EXISTS `EVENT_PUBLICATION`;
CREATE TABLE `EVENT_PUBLICATION` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `listener_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_type` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `serialized_event` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `publication_date` timestamp NOT NULL,
  `completion_date` timestamp NULL DEFAULT NULL,
  `STATUS` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件状态',
  `COMPLETION_ATTEMPTS` int DEFAULT NULL COMMENT '补偿/重试次数',
  `LAST_RESUBMISSION_DATE` timestamp(6) NULL DEFAULT NULL COMMENT '最后一次重投时间',
  PRIMARY KEY (`id`),
  KEY `idx_completion_date` (`completion_date`),
  KEY `idx_publication_date` (`publication_date`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_company` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务附件关联表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hash` (`file_hash`),
  KEY `idx_file_hash` (`file_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件资源表（防孤儿文件）';

-- ----------------------------
-- Table structure for finance_flow
-- ----------------------------
DROP TABLE IF EXISTS `finance_flow`;
CREATE TABLE `finance_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_no` varchar(32) NOT NULL COMMENT '财务流水号，如 FL202503150001',
  `company_id` bigint NOT NULL COMMENT '公司ID（多租户）',
  `payment_flow_id` bigint NOT NULL COMMENT '关联支付流水ID（payment_flow.id），同一次收款操作共享同一个值',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型：LEASE_BILL_FEE/DEPOSIT/REFUND/ADJUST',
  `biz_id` bigint NOT NULL COMMENT '业务单据ID，由 biz_type 决定指向哪张表',
  `biz_no` varchar(32) DEFAULT NULL COMMENT '业务单据编号（冗余，便于展示）',
  `flow_type` varchar(16) NOT NULL COMMENT '流水类型：RECEIVE=收款 REFUND=退款 ADJUST=调整 VOID=作废',
  `flow_direction` varchar(8) NOT NULL COMMENT '资金方向：IN=收入 OUT=支出',
  `amount` decimal(10,2) NOT NULL COMMENT '本条流水金额',
  `currency` char(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=入账中 1=已入账 2=失败 3=已作废',
  `refund_flow_id` bigint DEFAULT NULL COMMENT '退款时关联原始 finance_flow.id',
  `flow_at` datetime DEFAULT NULL COMMENT '流水发生时间',
  `payer_name` varchar(64) DEFAULT NULL COMMENT '付款方姓名',
  `payer_phone` varchar(20) DEFAULT NULL COMMENT '付款方手机号',
  `receiver_name` varchar(64) DEFAULT NULL COMMENT '收款方名称',
  `operator_id` bigint DEFAULT NULL COMMENT '操作员工ID',
  `operator_name` varchar(32) DEFAULT NULL COMMENT '操作员工姓名',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否 1=是',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`),
  KEY `idx_payment_flow_id` (`payment_flow_id`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_company_flow_at` (`company_id`,`flow_at`),
  KEY `idx_refund_flow` (`refund_flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务流水表';

-- ----------------------------
-- Table structure for focus
-- ----------------------------
DROP TABLE IF EXISTS `focus`;
CREATE TABLE `focus` (
  `id` bigint NOT NULL,
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `focus_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目编号',
  `focus_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL DEFAULT '0' COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房型设置';

-- ----------------------------
-- Table structure for lease
-- ----------------------------
DROP TABLE IF EXISTS `lease`;
CREATE TABLE `lease` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租约ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID（关联 tenant.id）',
  `parent_lease_id` bigint DEFAULT NULL COMMENT '上一份租约ID（续签/换房时关联）',
  `contract_nature` tinyint NOT NULL COMMENT '合同性质：1=新签，2=续签，3=转租，4=换房',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `room_ids` json NOT NULL COMMENT '房间IDs',
  `rent_price` decimal(12,2) NOT NULL COMMENT '租金价格',
  `deposit_months` int NOT NULL COMMENT '押金月数',
  `payment_months` int NOT NULL COMMENT '支付周期（月）',
  `lease_start` datetime NOT NULL COMMENT '租赁开始时间',
  `lease_end` datetime NOT NULL COMMENT '租赁结束时间',
  `check_in_at` datetime DEFAULT NULL COMMENT '实际入住时间',
  `check_out_at` datetime DEFAULT NULL COMMENT '实际搬离时间',
  `original_lease_start` datetime DEFAULT NULL COMMENT '初始录入租赁开始时间',
  `original_lease_end` datetime DEFAULT NULL COMMENT '初始录入租赁结束时间',
  `lease_duration_days` int DEFAULT NULL COMMENT '累计租房天数',
  `rent_due_type` tinyint DEFAULT NULL COMMENT '收租类型：1=提前，2=固定，3=延后',
  `rent_due_day` tinyint DEFAULT NULL COMMENT '固定收租日',
  `rent_due_offset_days` int DEFAULT NULL COMMENT '收租偏移天数',
  `salesman_id` bigint NOT NULL COMMENT '业务人员ID',
  `helper_id` bigint DEFAULT NULL COMMENT '协助人员ID',
  `sign_status` tinyint DEFAULT '0' COMMENT '签约状态：0=待签字，1=已签字',
  `check_out_status` tinyint DEFAULT '0' COMMENT '退租状态：0=未退租，1=正常退，2=换房退，3=违约退，4=作废',
  `status` tinyint DEFAULT '0' COMMENT '租约状态：0=待审批，1=待签字，2=在租中，3=已退租，-1=已作废',
  `approval_status` tinyint DEFAULT '2' COMMENT '审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回',
  `tenant_source` bigint DEFAULT NULL COMMENT '租客来源',
  `deal_channel` bigint DEFAULT NULL COMMENT '成交渠道',
  `remark` varchar(500) DEFAULT '' COMMENT '合同备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_parent_lease_id` (`parent_lease_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租约表';

-- ----------------------------
-- Table structure for lease_bill
-- ----------------------------
DROP TABLE IF EXISTS `lease_bill`;
CREATE TABLE `lease_bill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `lease_id` bigint NOT NULL COMMENT '租约ID',
  `sort_order` int NOT NULL COMMENT '账单顺序',
  `bill_type` tinyint DEFAULT '1' COMMENT '账单类型：1=租金，2=押金，3=杂费，4=退租结算，5=押金结转入，6=押金结转出',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '账单状态：1=正常 2=已作废',
  `historical` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否历史账单：0=否 1=是',
  `void_reason` varchar(255) DEFAULT NULL COMMENT '作废原因',
  `void_at` datetime DEFAULT NULL COMMENT '作废时间',
  `void_by` bigint DEFAULT NULL COMMENT '作废人',
  `carry_over_from_bill_id` bigint DEFAULT NULL COMMENT '结转来源账单ID',
  `carry_over_to_bill_id` bigint DEFAULT NULL COMMENT '结转目标账单ID',
  `bill_start` date DEFAULT NULL COMMENT '账单周期开始日期',
  `bill_end` date DEFAULT NULL COMMENT '账单周期结束日期',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '账单总金额',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已收金额（汇总自 lease_bill_fee）',
  `unpaid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '待收金额 = total_amount - paid_amount',
  `due_date` date NOT NULL COMMENT '应缴日期',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态：0=未支付，1=部分支付，2=已支付',
  `remark` varchar(500) DEFAULT '' COMMENT '备注信息',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_lease_id` (`lease_id`),
  KEY `idx_company_pay_status` (`company_id`,`pay_status`),
  KEY `idx_due_date` (`due_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客账单表';

-- ----------------------------
-- Table structure for lease_bill_fee
-- ----------------------------
DROP TABLE IF EXISTS `lease_bill_fee`;
CREATE TABLE `lease_bill_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bill_id` bigint NOT NULL COMMENT '账单ID（关联 lease_bill.id）',
  `fee_type` varchar(16) NOT NULL COMMENT '费用类型：RENTAL/DEPOSIT/OTHER_FEE',
  `dict_data_id` bigint DEFAULT NULL COMMENT '费用字典ID',
  `fee_name` varchar(64) NOT NULL COMMENT '费用名称快照',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '应收金额',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已收金额',
  `unpaid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '待收金额 = amount - paid_amount',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '支付状态：0=未支付，1=部分支付，2=已支付',
  `fee_start` date DEFAULT NULL COMMENT '费用周期开始',
  `fee_end` date DEFAULT NULL COMMENT '费用周期结束',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客账单费用明细表';

-- ----------------------------
-- Table structure for lease_checkout
-- ----------------------------
DROP TABLE IF EXISTS `lease_checkout`;
CREATE TABLE `lease_checkout` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退租单ID',
  `checkout_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '退租单编号',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `tenant_id` bigint NOT NULL COMMENT '租客ID',
  `lease_id` bigint DEFAULT NULL COMMENT '租约ID',
  `delivery_id` bigint DEFAULT NULL COMMENT '交割单ID（关联 delivery.id，handover_type=CHECK_OUT）',
  `checkout_type` tinyint NOT NULL COMMENT '退租类型：1=正常退，2=违约退',
  `breach_reason` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '解约原因（违约退时选填）',
  `lease_end` datetime DEFAULT NULL COMMENT '合同到期日',
  `actual_checkout_date` date NOT NULL COMMENT '实际离房日期',
  `deposit_amount` decimal(12,2) DEFAULT '0.00' COMMENT '押金总额',
  `income_amount` decimal(12,2) DEFAULT '0.00' COMMENT '收入总额（租客应付）',
  `expense_amount` decimal(12,2) DEFAULT '0.00' COMMENT '支出总额（退还租客）',
  `final_amount` decimal(12,2) DEFAULT '0.00' COMMENT '最终结算（正数=租客补缴，负数=应退租客）',
  `expected_payment_date` date DEFAULT NULL COMMENT '预计收/付款时间',
  `settlement_method` tinyint DEFAULT NULL COMMENT '账单处理方式：1=生成待付账单，2=线下付款，3=申请付款，4=标记坏账',
  `bad_debt_reason` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '坏账原因(标记坏账时必填)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=草稿，1=待确认，2=已完成，3=已取消',
  `approval_status` tinyint DEFAULT NULL COMMENT '审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回',
  `settlement_at` datetime DEFAULT NULL COMMENT '结算完成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退租备注',
  `attachment_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '退租凭证附件ID列表（JSON数组）',
  `payee_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收款人姓名',
  `payee_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收款人电话',
  `payee_id_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收款人证件类型',
  `payee_id_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收款人证件号',
  `bank_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行类型（银联等）',
  `bank_card_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行卡类型（借记卡/信用卡）',
  `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行账号',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行名称',
  `bank_branch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支行名称',
  `send_confirmation` tinyint(1) DEFAULT '0' COMMENT '是否发送退租确认单：0=否，1=是',
  `confirmation_template` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退租确认单模板',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkout_code` (`checkout_code`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_checkout_type` (`checkout_type`),
  KEY `idx_create_at` (`create_at`),
  KEY `idx_lease_id` (`lease_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退租主表（退租并结账）';

-- ----------------------------
-- Table structure for lease_checkout_fee
-- ----------------------------
DROP TABLE IF EXISTS `lease_checkout_fee`;
CREATE TABLE `lease_checkout_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `checkout_id` bigint NOT NULL COMMENT '退租单ID',
  `fee_direction` tinyint NOT NULL COMMENT '收支类型：1=收（租客应付），2=支（退还租客）',
  `fee_type` int NOT NULL COMMENT '费用类型：1=租金,2=押金,3=水费,4=电费,5=燃气费,6=物业费,7=清洁费,8=物品损坏,9=违约金,10=其他,51=租金退,52=押金退,53=其他退',
  `fee_sub_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '费用子类名称（如"房屋押金"）',
  `fee_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '费用金额（正数）',
  `fee_period_start` date DEFAULT NULL COMMENT '费用周期开始',
  `fee_period_end` date DEFAULT NULL COMMENT '费用周期结束',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '费用备注',
  `bill_id` bigint DEFAULT NULL COMMENT '关联账单ID（如有）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_at` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_checkout_id` (`checkout_id`),
  KEY `idx_fee_direction` (`fee_direction`),
  KEY `idx_fee_type` (`fee_type`),
  KEY `idx_bill_id` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退租费用明细表';

-- ----------------------------
-- Table structure for lease_contract
-- ----------------------------
DROP TABLE IF EXISTS `lease_contract`;
CREATE TABLE `lease_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租客合同ID',
  `lease_id` bigint NOT NULL COMMENT '租约ID',
  `contract_code` varchar(100) DEFAULT NULL COMMENT '合同编码',
  `contract_template_id` bigint NOT NULL COMMENT '合同模板ID',
  `contract_content` text COMMENT '合同内容',
  `sign_status` tinyint DEFAULT NULL COMMENT '签约状态：0=待签字、1=已签字',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '合同签约备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contract_code` (`contract_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客合同表';

-- ----------------------------
-- Table structure for lease_other_fee
-- ----------------------------
DROP TABLE IF EXISTS `lease_other_fee`;
CREATE TABLE `lease_other_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lease_id` bigint NOT NULL COMMENT '租约ID',
  `dict_data_id` bigint NOT NULL COMMENT '其他费用 ID',
  `name` varchar(32) DEFAULT NULL COMMENT '其他费用名称',
  `payment_method` tinyint NOT NULL DEFAULT '1' COMMENT '付款方式（如：随房租付、按固定金额等）',
  `price_method` tinyint NOT NULL DEFAULT '1' COMMENT '价格计算方式',
  `price_input` decimal(10,2) NOT NULL DEFAULT '1.00' COMMENT '价格输入值',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`lease_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客其他费用';

-- ----------------------------
-- Table structure for lease_room
-- ----------------------------
DROP TABLE IF EXISTS `lease_room`;
CREATE TABLE `lease_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lease_id` bigint NOT NULL COMMENT '租约ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_lease_id` (`lease_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2039902283379220482 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='租约-房间关联表';

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
  `login_at` datetime DEFAULT NULL COMMENT '登录时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模块标题',
  `operation_type` smallint DEFAULT NULL COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `operator_type` smallint DEFAULT NULL COMMENT '操作人类别（0其它 1后台用户 2前台用户）',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
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
  `request_at` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT NULL COMMENT '消耗时间',
  PRIMARY KEY (`id`),
  KEY `idx_sol_bt` (`operation_type`) USING BTREE,
  KEY `idx_sol_ot` (`request_at`) USING BTREE,
  KEY `idx_sol_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2044323665047412739 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统日志记录表';

-- ----------------------------
-- Table structure for owner
-- ----------------------------
DROP TABLE IF EXISTS `owner`;
CREATE TABLE `owner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `owner_type_id` bigint NOT NULL COMMENT '业主类型关联ID',
  `owner_type` int NOT NULL COMMENT '业主类型：0=个人，1=企业',
  `owner_name` varchar(128) NOT NULL COMMENT '业主名称',
  `owner_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_company` (`company_id`),
  KEY `idx_owner_type` (`owner_type`,`owner_type_id`),
  KEY `idx_owner_name` (`owner_name`),
  KEY `idx_owner_phone` (`owner_phone`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主主表';

-- ----------------------------
-- Table structure for owner_account
-- ----------------------------
DROP TABLE IF EXISTS `owner_account`;
CREATE TABLE `owner_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `account_status` int NOT NULL DEFAULT '1' COMMENT '账户状态：1=启用，0=禁用',
  `available_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '可用金额',
  `frozen_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '冻结金额',
  `pending_settlement_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '待结算金额',
  `total_income_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计收入',
  `total_reduction_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计扣减',
  `total_withdraw_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计提现',
  `version` bigint NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_account_owner` (`owner_id`),
  KEY `idx_owner_account_company` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主账户表';

-- ----------------------------
-- Table structure for owner_account_flow
-- ----------------------------
DROP TABLE IF EXISTS `owner_account_flow`;
CREATE TABLE `owner_account_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '业务ID',
  `flow_direction` varchar(32) DEFAULT NULL COMMENT '流水方向',
  `change_type` varchar(64) DEFAULT NULL COMMENT '变动类型',
  `amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动金额',
  `available_before` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动前可用金额',
  `available_after` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动后可用金额',
  `frozen_before` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动前冻结金额',
  `frozen_after` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动后冻结金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_account_flow_company` (`company_id`),
  KEY `idx_owner_account_flow_owner` (`owner_id`),
  KEY `idx_owner_account_flow_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主账户流水表';

-- ----------------------------
-- Table structure for owner_company
-- ----------------------------
DROP TABLE IF EXISTS `owner_company`;
CREATE TABLE `owner_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `name` varchar(200) NOT NULL COMMENT '企业名称',
  `uscc` varchar(64) DEFAULT NULL COMMENT '统一社会信用代码',
  `legal_person` varchar(128) DEFAULT NULL COMMENT '法人姓名',
  `legal_person_id_type` int DEFAULT NULL COMMENT '法人证件类型：0=身份证，1=护照，2=港澳通行证，3=台胞证',
  `legal_person_id_no` varchar(64) DEFAULT NULL COMMENT '法人证件号码',
  `contact_name` varchar(128) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系人电话',
  `payee_name` varchar(64) DEFAULT NULL COMMENT '收款人姓名',
  `payee_phone` varchar(32) DEFAULT NULL COMMENT '收款人电话',
  `payee_id_type` int DEFAULT NULL COMMENT '收款人证件类型',
  `payee_id_no` varchar(64) DEFAULT NULL COMMENT '收款人证件号码',
  `bank_account_name` varchar(128) DEFAULT NULL COMMENT '银行卡开户名',
  `bank_account_no` varchar(64) DEFAULT NULL COMMENT '银行卡号',
  `bank_name` varchar(128) DEFAULT NULL COMMENT '开户行名称',
  `registered_address` varchar(500) DEFAULT NULL COMMENT '注册地址',
  `tags` json DEFAULT NULL COMMENT '标签JSON数组',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_company_company` (`company_id`),
  KEY `idx_owner_company_name` (`name`),
  KEY `idx_owner_company_contact_phone` (`contact_phone`),
  KEY `idx_owner_company_uscc` (`uscc`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主企业信息表';

-- ----------------------------
-- Table structure for owner_contract
-- ----------------------------
DROP TABLE IF EXISTS `owner_contract`;
CREATE TABLE `owner_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `cooperation_mode` varchar(32) NOT NULL COMMENT '合作模式：LIGHT_MANAGED=轻托管，MASTER_LEASE=包租',
  `contract_no` varchar(64) NOT NULL COMMENT '合同编号',
  `contract_template_id` bigint DEFAULT NULL COMMENT '合同模板ID',
  `contract_content` longtext COMMENT '合同内容快照',
  `sign_status` int NOT NULL DEFAULT '0' COMMENT '签署状态：0=待签字，1=已签字',
  `sign_type` varchar(32) DEFAULT NULL COMMENT '签约类型：NEW=新签，RENEW=续签',
  `contract_medium` varchar(32) DEFAULT NULL COMMENT '合同介质：ELECTRONIC=电子合同，PAPER=纸质合同',
  `notify_owner` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否通知业主',
  `contract_start` date DEFAULT NULL COMMENT '合同开始日期',
  `contract_end` date DEFAULT NULL COMMENT '合同结束日期',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `approval_status` int NOT NULL DEFAULT '2' COMMENT '审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_contract_no` (`contract_no`),
  KEY `idx_owner_contract_company` (`company_id`),
  KEY `idx_owner_contract_owner` (`owner_id`),
  KEY `idx_owner_contract_mode` (`cooperation_mode`),
  KEY `idx_owner_contract_date` (`contract_start`,`contract_end`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主合同主表';

-- ----------------------------
-- Table structure for owner_contract_subject
-- ----------------------------
DROP TABLE IF EXISTS `owner_contract_subject`;
CREATE TABLE `owner_contract_subject` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `subject_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'HOUSE' COMMENT '合同房源类型: HOUSE/FOCUS_BUILDING/FOCUS',
  `subject_id` bigint NOT NULL COMMENT '签约标的ID',
  `subject_name_snapshot` varchar(255) DEFAULT NULL COMMENT '签约标的名称快照',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_contract_subject` (`contract_id`,`subject_type`,`subject_id`,`deleted`),
  KEY `idx_owner_contract_subject_company` (`company_id`),
  KEY `idx_owner_contract_subject_ref` (`subject_type`,`subject_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主合同关联房源表';

-- ----------------------------
-- Table structure for owner_lease_fee
-- ----------------------------
DROP TABLE IF EXISTS `owner_lease_fee`;
CREATE TABLE `owner_lease_fee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `fee_type` varchar(64) NOT NULL COMMENT '费用科目类型',
  `fee_name` varchar(64) NOT NULL COMMENT '费用名称',
  `fee_direction` varchar(16) NOT NULL COMMENT '方向: IN/OUT',
  `payment_method` int DEFAULT NULL COMMENT '付款方式',
  `price_method` int DEFAULT NULL COMMENT '金额方式',
  `price_input` decimal(12,2) DEFAULT NULL COMMENT '金额或比例值',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_lease_fee_company` (`company_id`),
  KEY `idx_owner_lease_fee_contract` (`contract_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='包租其他费用配置';

-- ----------------------------
-- Table structure for owner_lease_free_rule
-- ----------------------------
DROP TABLE IF EXISTS `owner_lease_free_rule`;
CREATE TABLE `owner_lease_free_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `free_type` varchar(32) DEFAULT NULL COMMENT '免租类型：BUILT_IN/OUTSIDE',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `calc_mode` varchar(32) DEFAULT NULL COMMENT '计算方式：FIXED/RATIO',
  `free_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '免租金额',
  `free_ratio` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '免租比例',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_lease_free_rule_company` (`company_id`),
  KEY `idx_owner_lease_free_rule_contract` (`contract_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='包租免租规则表';

-- ----------------------------
-- Table structure for owner_lease_rule
-- ----------------------------
DROP TABLE IF EXISTS `owner_lease_rule`;
CREATE TABLE `owner_lease_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `rent_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '总月租金',
  `deposit_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '总押金',
  `deposit_months` int NOT NULL DEFAULT '0' COMMENT '押金月数',
  `payment_months` int NOT NULL DEFAULT '1' COMMENT '付款月数',
  `pay_way` varchar(64) DEFAULT NULL COMMENT '付款方式文案',
  `rent_due_type` int DEFAULT NULL COMMENT '收租类型：1=提前，2=固定，3=延后',
  `rent_due_day` int DEFAULT NULL COMMENT '固定收租日',
  `rent_due_offset_days` int DEFAULT NULL COMMENT '收租偏移天数',
  `first_pay_date` date DEFAULT NULL COMMENT '首付日期',
  `handover_date` date DEFAULT NULL COMMENT '交房日期',
  `usage_type` varchar(64) DEFAULT NULL COMMENT '承租用途',
  `billing_start` date DEFAULT NULL COMMENT '计费开始日期',
  `billing_end` date DEFAULT NULL COMMENT '计费结束日期',
  `prorate_type` varchar(32) DEFAULT NULL COMMENT '折算方式：BY_DAYS/FULL_PERIOD',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_lease_rule_contract` (`contract_id`,`deleted`),
  KEY `idx_owner_lease_rule_company` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='包租规则表';

-- ----------------------------
-- Table structure for owner_payable_bill
-- ----------------------------
DROP TABLE IF EXISTS `owner_payable_bill`;
CREATE TABLE `owner_payable_bill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应付单号',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `subject_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合同房源类型: HOUSE/FOCUS/FOCUS_BUILDING',
  `subject_id` bigint NOT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同房源名称快照',
  `bill_start_date` date NOT NULL COMMENT '账期开始日期',
  `bill_end_date` date NOT NULL COMMENT '账期结束日期',
  `due_date` date DEFAULT NULL COMMENT '应付日期',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '应付金额',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已付金额',
  `unpaid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '未付金额',
  `adjust_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '调整金额',
  `payment_status` tinyint NOT NULL DEFAULT '0' COMMENT '付款状态: 0未付款 1部分付款 2已付款',
  `bill_status` tinyint NOT NULL DEFAULT '1' COMMENT '单据状态: 1正常 2已作废',
  `cancel_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作废原因',
  `cancel_by` bigint DEFAULT NULL COMMENT '作废操作人ID',
  `cancel_by_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作废操作人名称',
  `cancel_at` datetime DEFAULT NULL COMMENT '作废时间',
  `generated_at` datetime DEFAULT NULL COMMENT '生成时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_payable_bill_no` (`company_id`,`bill_no`,`deleted`),
  UNIQUE KEY `uk_owner_payable_bill_period` (`company_id`,`contract_id`,`subject_type`,`subject_id`,`bill_start_date`,`bill_end_date`,`deleted`),
  KEY `idx_owner_payable_bill_owner` (`company_id`,`owner_id`),
  KEY `idx_owner_payable_bill_contract` (`company_id`,`contract_id`),
  KEY `idx_owner_payable_bill_subject` (`company_id`,`subject_type`,`subject_id`),
  KEY `idx_owner_payable_bill_status` (`company_id`,`payment_status`,`bill_status`),
  KEY `idx_owner_payable_bill_due_date` (`company_id`,`due_date`),
  KEY `idx_owner_payable_bill_generated_at` (`company_id`,`generated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包租业主应付单';

-- ----------------------------
-- Table structure for owner_payable_bill_line
-- ----------------------------
DROP TABLE IF EXISTS `owner_payable_bill_line`;
CREATE TABLE `owner_payable_bill_line` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '应付单ID',
  `source_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `subject_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同房源类型',
  `subject_id` bigint DEFAULT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同房源名称快照',
  `item_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目类型',
  `direction` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OUT' COMMENT '方向: IN/OUT',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `biz_date` date DEFAULT NULL COMMENT '业务日期',
  `formula_snapshot` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '计算说明快照',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_payable_bill_line_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_payable_bill_line_source` (`company_id`,`source_type`,`source_id`),
  KEY `idx_owner_payable_bill_line_subject` (`company_id`,`subject_type`,`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包租业主应付单明细';

-- ----------------------------
-- Table structure for owner_payable_bill_payment
-- ----------------------------
DROP TABLE IF EXISTS `owner_payable_bill_payment`;
CREATE TABLE `owner_payable_bill_payment` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '应付单ID',
  `payment_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '付款单号',
  `pay_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '付款金额',
  `pay_at` datetime NOT NULL COMMENT '付款时间',
  `pay_channel` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '付款渠道',
  `third_trade_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方流水号',
  `voucher_urls` text COLLATE utf8mb4_unicode_ci COMMENT '支付凭证URL列表(JSON)',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_payable_bill_payment_no` (`company_id`,`payment_no`,`deleted`),
  KEY `idx_owner_payable_bill_payment_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_payable_bill_payment_pay_at` (`company_id`,`pay_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包租业主应付单付款记录';

-- ----------------------------
-- Table structure for owner_personal
-- ----------------------------
DROP TABLE IF EXISTS `owner_personal`;
CREATE TABLE `owner_personal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `name` varchar(128) NOT NULL COMMENT '姓名',
  `gender` int DEFAULT NULL COMMENT '性别：0=未知，1=男，2=女',
  `id_type` int DEFAULT NULL COMMENT '证件类型：0=身份证，1=护照，2=港澳通行证，3=台胞证',
  `id_no` varchar(64) DEFAULT NULL COMMENT '证件号码',
  `phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `payee_name` varchar(64) DEFAULT NULL COMMENT '收款人姓名',
  `payee_phone` varchar(32) DEFAULT NULL COMMENT '收款人电话',
  `payee_id_type` int DEFAULT NULL COMMENT '收款人证件类型',
  `payee_id_no` varchar(64) DEFAULT NULL COMMENT '收款人证件号码',
  `bank_account_name` varchar(128) DEFAULT NULL COMMENT '银行卡开户名',
  `bank_account_no` varchar(64) DEFAULT NULL COMMENT '银行卡号',
  `bank_name` varchar(128) DEFAULT NULL COMMENT '开户行名称',
  `tags` json DEFAULT NULL COMMENT '标签JSON数组',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_personal_company` (`company_id`),
  KEY `idx_owner_personal_name` (`name`),
  KEY `idx_owner_personal_phone` (`phone`),
  KEY `idx_owner_personal_id_no` (`id_no`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主个人信息表';

-- ----------------------------
-- Table structure for owner_rent_free_rule
-- ----------------------------
DROP TABLE IF EXISTS `owner_rent_free_rule`;
CREATE TABLE `owner_rent_free_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `contract_subject_id` bigint NOT NULL COMMENT '业主合同签约标的ID',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用免租',
  `free_type` varchar(32) DEFAULT NULL COMMENT '免租类型：BUILT_IN/OUTSIDE',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `bear_type` varchar(32) DEFAULT NULL COMMENT '承担方式：PLATFORM/OWNER/SHARED',
  `owner_ratio` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '业主承担比例',
  `platform_ratio` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '平台承担比例',
  `calc_mode` varchar(32) DEFAULT NULL COMMENT '计算方式：BY_DAYS/FIXED/RATIO',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_rent_free_rule_company` (`company_id`),
  KEY `idx_owner_rent_free_rule_contract` (`contract_id`),
  KEY `idx_owner_rent_free_rule_subject` (`contract_subject_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='轻托管免租规则表';

-- ----------------------------
-- Table structure for owner_settlement_bill
-- ----------------------------
DROP TABLE IF EXISTS `owner_settlement_bill`;
CREATE TABLE `owner_settlement_bill` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结算单号',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `subject_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合同房源类型: HOUSE/FOCUS/FOCUS_BUILDING',
  `subject_id` bigint NOT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同房源名称快照',
  `bill_start_date` date NOT NULL COMMENT '账期开始日期',
  `bill_end_date` date NOT NULL COMMENT '账期结束日期',
  `income_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '收入金额',
  `expense_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '费用金额',
  `reduction_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '减免金额',
  `adjust_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '调账金额',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '应结金额',
  `settled_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已结金额',
  `withdrawable_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '可提现金额',
  `withdrawn_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已提现金额',
  `freeze_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '冻结金额',
  `bill_status` tinyint NOT NULL DEFAULT '1' COMMENT '单据状态: 1正常',
  `approval_status` tinyint NOT NULL DEFAULT '1' COMMENT '审批状态: 1审批中 2已通过 3已驳回 4已撤回',
  `settlement_status` tinyint NOT NULL DEFAULT '0' COMMENT '结算状态: 0未结算 1部分结算 2已结算',
  `generated_at` datetime DEFAULT NULL COMMENT '生成时间',
  `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_settlement_bill_no` (`company_id`,`bill_no`,`deleted`),
  UNIQUE KEY `uk_owner_settlement_bill_period` (`company_id`,`contract_id`,`subject_type`,`subject_id`,`bill_start_date`,`bill_end_date`,`deleted`),
  KEY `idx_owner_settlement_bill_owner` (`company_id`,`owner_id`),
  KEY `idx_owner_settlement_bill_contract` (`company_id`,`contract_id`),
  KEY `idx_owner_settlement_bill_subject` (`company_id`,`subject_type`,`subject_id`),
  KEY `idx_owner_settlement_bill_status` (`company_id`,`approval_status`,`settlement_status`,`bill_status`),
  KEY `idx_owner_settlement_bill_generated_at` (`company_id`,`generated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轻托管业主结算单';

-- ----------------------------
-- Table structure for owner_settlement_bill_line
-- ----------------------------
DROP TABLE IF EXISTS `owner_settlement_bill_line`;
CREATE TABLE `owner_settlement_bill_line` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '结算单ID',
  `source_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `subject_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同房源类型',
  `subject_id` bigint DEFAULT NULL COMMENT '合同房源ID',
  `subject_name_snapshot` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同房源名称快照',
  `item_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目类型',
  `direction` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '方向: IN/OUT',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `biz_date` date DEFAULT NULL COMMENT '业务日期',
  `formula_snapshot` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '计算说明快照',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_settlement_bill_line_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_settlement_bill_line_source` (`company_id`,`source_type`,`source_id`),
  KEY `idx_owner_settlement_bill_line_subject` (`company_id`,`subject_type`,`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轻托管业主结算单明细';

-- ----------------------------
-- Table structure for owner_settlement_bill_reduction
-- ----------------------------
DROP TABLE IF EXISTS `owner_settlement_bill_reduction`;
CREATE TABLE `owner_settlement_bill_reduction` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `bill_id` bigint NOT NULL COMMENT '结算单ID',
  `source_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `reduction_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '减免项名称',
  `reduction_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '减免类型',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '减免金额',
  `biz_date` date DEFAULT NULL COMMENT '业务日期',
  `rule_snapshot` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规则快照',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除: 0否 1是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_settlement_bill_reduction_bill` (`company_id`,`bill_id`),
  KEY `idx_owner_settlement_bill_reduction_source` (`company_id`,`source_type`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轻托管业主结算单减免';

-- ----------------------------
-- Table structure for owner_settlement_item
-- ----------------------------
DROP TABLE IF EXISTS `owner_settlement_item`;
CREATE TABLE `owner_settlement_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `contract_subject_id` bigint NOT NULL COMMENT '合同签约标的ID',
  `fee_direction` varchar(16) NOT NULL DEFAULT 'IN' COMMENT '收支方向: IN/OUT',
  `fee_type` varchar(64) NOT NULL COMMENT '费用科目类型',
  `item_name` varchar(64) NOT NULL COMMENT '费用科目名称',
  `transfer_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否转给业主',
  `transfer_ratio` decimal(5,2) DEFAULT NULL COMMENT '转给业主比例(0-100)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_settlement_item_company` (`company_id`),
  KEY `idx_owner_settlement_item_contract` (`contract_id`),
  KEY `idx_owner_settlement_item_subject` (`contract_subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='轻托管分账费用科目规则';

-- ----------------------------
-- Table structure for owner_settlement_rule
-- ----------------------------
DROP TABLE IF EXISTS `owner_settlement_rule`;
CREATE TABLE `owner_settlement_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `contract_id` bigint NOT NULL COMMENT '业主合同ID',
  `contract_subject_id` bigint NOT NULL COMMENT '业主合同签约标的ID',
  `rule_version` int NOT NULL DEFAULT '1' COMMENT '规则版本',
  `income_basis` varchar(32) DEFAULT NULL COMMENT '收入口径：RECEIVED/RECEIVABLE',
  `settlement_mode` varchar(64) DEFAULT NULL COMMENT '结算模式：FIXED/SHARE_GROSS/SHARE_NET/GUARANTEE_PLUS_SHARE/AGENCY',
  `guaranteed_rent_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '保底租金',
  `has_guaranteed_rent` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否有保底租金',
  `commission_mode` varchar(32) DEFAULT NULL COMMENT '佣金方式：RATIO/FIXED',
  `commission_value` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '佣金值',
  `service_fee_mode` varchar(32) DEFAULT NULL COMMENT '服务费方式：RATIO/FIXED',
  `service_fee_value` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '服务费值',
  `management_fee_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用管理费',
  `management_fee_mode` varchar(32) DEFAULT NULL COMMENT '管理费方式：RATIO/FIXED',
  `management_fee_value` decimal(18,2) DEFAULT NULL COMMENT '管理费值',
  `bear_tax_type` varchar(32) DEFAULT NULL COMMENT '税费承担方：PLATFORM/OWNER/SHARED',
  `payment_fee_bear_type` varchar(32) DEFAULT NULL COMMENT '支付手续费承担方式',
  `settlement_timing` varchar(32) DEFAULT NULL COMMENT '分账时间',
  `rent_free_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用免租规则',
  `calc_priority` int DEFAULT NULL COMMENT '计算优先级',
  `effective_start` date DEFAULT NULL COMMENT '生效开始日期',
  `effective_end` date DEFAULT NULL COMMENT '生效结束日期',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
  `rule_snapshot` longtext COMMENT '规则快照',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_owner_settlement_rule_company` (`company_id`),
  KEY `idx_owner_settlement_rule_contract` (`contract_id`),
  KEY `idx_owner_settlement_rule_subject` (`contract_subject_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='轻托管结算规则表';

-- ----------------------------
-- Table structure for owner_withdraw_apply
-- ----------------------------
DROP TABLE IF EXISTS `owner_withdraw_apply`;
CREATE TABLE `owner_withdraw_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT 'SaaS企业ID',
  `owner_id` bigint NOT NULL COMMENT '业主ID',
  `apply_no` varchar(64) NOT NULL COMMENT '提现申请单号',
  `apply_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '申请金额',
  `fee_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '手续费',
  `actual_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '实际到账金额',
  `approval_status` int NOT NULL DEFAULT '1' COMMENT '审批状态：1=审批中，2=已通过，3=已驳回，4=已撤回',
  `withdraw_status` int NOT NULL DEFAULT '0' COMMENT '提现状态',
  `payee_name` varchar(128) DEFAULT NULL COMMENT '收款人姓名',
  `payee_account_no` varchar(64) DEFAULT NULL COMMENT '收款账号',
  `payee_bank_name` varchar(128) DEFAULT NULL COMMENT '开户行名称',
  `channel` varchar(32) DEFAULT NULL COMMENT '打款渠道',
  `third_trade_no` varchar(128) DEFAULT NULL COMMENT '第三方交易号',
  `failure_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `applied_at` datetime DEFAULT NULL COMMENT '申请时间',
  `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
  `paid_at` datetime DEFAULT NULL COMMENT '打款时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_withdraw_apply_no` (`apply_no`),
  KEY `idx_owner_withdraw_apply_company` (`company_id`),
  KEY `idx_owner_withdraw_apply_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业主提现申请表';

-- ----------------------------
-- Table structure for payment_flow
-- ----------------------------
DROP TABLE IF EXISTS `payment_flow`;
CREATE TABLE `payment_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `payment_no` varchar(32) NOT NULL COMMENT '系统支付流水号（如 PAY202503150001）',
  `company_id` bigint NOT NULL COMMENT '公司ID（多租户隔离）',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型：LEASE_BILL/DEPOSIT 等',
  `biz_id` bigint DEFAULT NULL COMMENT '业务单据ID，1:1场景直接关联账单，合并付款场景留NULL',
  `channel` varchar(16) NOT NULL COMMENT '支付渠道：CASH/TRANSFER/ALIPAY/WECHAT/POS/OTHER',
  `channel_account` varchar(64) DEFAULT NULL COMMENT '渠道收款账户（如具体收款码、银行卡号）',
  `third_trade_no` varchar(64) DEFAULT NULL COMMENT '第三方支付平台交易号',
  `payment_voucher_url` varchar(255) DEFAULT NULL COMMENT '支付凭证图片',
  `third_status` varchar(16) DEFAULT NULL COMMENT '第三方平台原始状态（冗余存储，便于对账）',
  `amount` decimal(10,2) NOT NULL COMMENT '金额（分）',
  `currency` char(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `refunded_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已退款金额（分）',
  `flow_direction` varchar(8) NOT NULL COMMENT '资金方向：IN 入账 / OUT 出账',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=支付中、1=支付成功、2=支付失败、3=已关闭、4=退款中、5=已退款',
  `approval_status` tinyint NOT NULL DEFAULT '2' COMMENT '审批状态：1-审批中 2-已通过 3-已驳回 4-已撤回',
  `pay_at` datetime DEFAULT NULL COMMENT '实际支付完成时间',
  `expire_at` datetime DEFAULT NULL COMMENT '支付超时时间（预下单场景）',
  `payer_name` varchar(64) DEFAULT NULL COMMENT '付款方姓名',
  `payer_phone` varchar(20) DEFAULT NULL COMMENT '付款方手机号',
  `payer_account` varchar(64) DEFAULT NULL COMMENT '付款方账号（银行卡/支付宝账号等）',
  `receiver_name` varchar(64) DEFAULT NULL COMMENT '收款方名称',
  `receiver_account` varchar(64) DEFAULT NULL COMMENT '收款方账号',
  `operator_id` bigint DEFAULT NULL COMMENT '操作员工ID（线下收款场景）',
  `operator_name` varchar(32) DEFAULT NULL COMMENT '操作员工姓名',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段（存储渠道原始回调报文等）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  UNIQUE KEY `uk_third_trade_no` (`channel`,`third_trade_no`),
  KEY `idx_company_status` (`company_id`,`status`),
  KEY `idx_company_pay_at` (`company_id`,`pay_at`),
  KEY `idx_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付流水表（渠道层）';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `vacancy_start_at` datetime DEFAULT NULL COMMENT '空置开始时间',
  `available_date` datetime DEFAULT NULL COMMENT '可出租日期',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `occupancy_status` int NOT NULL DEFAULT '0' COMMENT '出租占用状态',
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '锁定状态：是否锁定',
  `closed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '禁用状态：是否已禁用',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '房间特色',
  `facilities` json DEFAULT NULL COMMENT '设施、从字典dict_data获取并配置',
  `image_list` json DEFAULT NULL COMMENT '图片列表',
  `video_list` json DEFAULT NULL COMMENT '视频',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间扩展表';

-- ----------------------------
-- Table structure for room_lock
-- ----------------------------
DROP TABLE IF EXISTS `room_lock`;
CREATE TABLE `room_lock` (
  `id` bigint NOT NULL,
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `room_id` bigint DEFAULT NULL,
  `lock_reason` int DEFAULT '1' COMMENT '锁房原因: 1-永久锁房, 2-指定时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `remark` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '锁房备注',
  `lock_status` int DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint DEFAULT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间锁房表';

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
  `create_by` bigint NOT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_room_deleted` (`room_id`,`deleted`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间价格表';

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
  `default_plan` tinyint(1) DEFAULT '0' COMMENT '是否默认方案',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  PRIMARY KEY (`id`),
  KEY `idx_room_deleted` (`room_id`,`deleted`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间租金方案表';

-- ----------------------------
-- Table structure for room_track
-- ----------------------------
DROP TABLE IF EXISTS `room_track`;
CREATE TABLE `room_track` (
  `id` bigint NOT NULL,
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `room_id` bigint DEFAULT NULL,
  `track_content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '跟进记录',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0 否，1 是',
  `create_by` bigint NOT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='房间跟进表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NOT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
  `id` bigint NOT NULL COMMENT '消息ID',
  `company_id` bigint NOT NULL COMMENT '公司/租户ID',
  `sender_id` bigint NOT NULL COMMENT '发送人（0=系统自动发送）',
  `receiver_id` bigint NOT NULL COMMENT '接收人',
  `title` varchar(100) DEFAULT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NOT NULL DEFAULT '1' COMMENT '1=系统消息 2=租约提醒 3=缴费提醒 4=报修通知 5=私信',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '关联业务类型（contract/bill/repair/room 等）',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务ID，前端据此跳转到对应详情页',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0=未读 1=已读',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  `deleted_by_sender` tinyint(1) NOT NULL DEFAULT '0' COMMENT '发送方删除：0=否 1=是',
  `deleted_by_receiver` tinyint(1) NOT NULL DEFAULT '0' COMMENT '接收方删除：0=否 1=是',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_read` (`receiver_id`,`is_read`,`create_at`),
  KEY `idx_sender` (`sender_id`,`create_at`),
  KEY `idx_company_biz` (`company_id`,`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内信/个人消息表';

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL COMMENT '公告ID',
  `company_id` bigint NOT NULL COMMENT '公司/租户ID',
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容（富文本）',
  `notice_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1=系统公告 2=运营通知',
  `target_scope` tinyint NOT NULL DEFAULT '1' COMMENT '发布范围：1=全员 2=房东 3=租客 4=指定角色',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0=草稿 1=已发布 2=已撤回',
  `publish_at` datetime DEFAULT NULL COMMENT '发布时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否 1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_status` (`company_id`,`status`,`publish_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';

-- ----------------------------
-- Table structure for sys_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read`;
CREATE TABLE `sys_notice_read` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `read_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告已读记录表';

-- ----------------------------
-- Table structure for sys_notice_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_role`;
CREATE TABLE `sys_notice_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_role` (`notice_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告角色关联表';

-- ----------------------------
-- Table structure for sys_todo
-- ----------------------------
DROP TABLE IF EXISTS `sys_todo`;
CREATE TABLE `sys_todo` (
  `id` bigint NOT NULL COMMENT '待办ID',
  `company_id` bigint NOT NULL COMMENT '公司/租户ID',
  `user_id` bigint NOT NULL COMMENT '待办负责人',
  `title` varchar(200) NOT NULL COMMENT '待办标题（如：张三 3月房租待收）',
  `content` varchar(500) DEFAULT NULL COMMENT '待办描述',
  `todo_type` tinyint NOT NULL COMMENT '1=租约到期 2=账单催收 3=报修处理 4=合同续签 5=退房办理 6=其他',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '关联业务类型（contract/bill/repair 等）',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务ID，点击可跳转到对应详情页',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级：1=高 2=中 3=低',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0=待处理 1=已处理 2=已忽略 3=已过期',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `handle_at` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(255) DEFAULT NULL COMMENT '处理备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否 1=是',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `read_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`,`deadline`),
  KEY `idx_company_type` (`company_id`,`todo_type`,`status`),
  KEY `idx_deadline` (`deadline`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='待办任务表';

-- ----------------------------
-- Table structure for tenant
-- ----------------------------
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租客ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `tenant_type` tinyint NOT NULL COMMENT '租客类型：0=个人，1=企业',
  `tenant_type_id` bigint NOT NULL COMMENT '关联 tenant_personal / tenant_company 的 ID',
  `tenant_name` varchar(100) NOT NULL COMMENT '租客名称',
  `tenant_phone` varchar(30) NOT NULL COMMENT '租客联系电话',
  `status` tinyint DEFAULT '1' COMMENT '租客状态：0=停用，1=正常',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_by` bigint NOT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_id` (`tenant_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客表';

-- ----------------------------
-- Table structure for tenant_backup
-- ----------------------------
DROP TABLE IF EXISTS `tenant_backup`;
CREATE TABLE `tenant_backup` (
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
  `check_in_at` datetime DEFAULT NULL COMMENT '实际入住时间',
  `check_out_at` datetime DEFAULT NULL COMMENT '实际搬离时间',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_type_id` (`tenant_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客表';

-- ----------------------------
-- Table structure for tenant_company
-- ----------------------------
DROP TABLE IF EXISTS `tenant_company`;
CREATE TABLE `tenant_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '企业租客ID',
  `company_id` bigint NOT NULL COMMENT '公司ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uscc` (`uscc`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业租客信息表';

-- ----------------------------
-- Table structure for tenant_mate
-- ----------------------------
DROP TABLE IF EXISTS `tenant_mate`;
CREATE TABLE `tenant_mate` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '同住人ID',
  `tenant_id` bigint unsigned NOT NULL COMMENT '租客ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `gender` tinyint DEFAULT NULL COMMENT '性别：0=男，1=女',
  `id_type` tinyint NOT NULL COMMENT '证件类型：1=身份证，2=护照',
  `id_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件号码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `tags` json DEFAULT NULL COMMENT '标签列表',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=停用，1=启用',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_id_no` (`id_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同住人信息表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租客个人信息表';

-- ----------------------------
-- Table structure for trial_application
-- ----------------------------
DROP TABLE IF EXISTS `trial_application`;
CREATE TABLE `trial_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号',
  `region_id` bigint NOT NULL COMMENT '城市区域ID',
  `city_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '城市名称',
  `usage_remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '如何使用系统',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0申请中 1已通过 2已拒绝',
  `handle_remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '处理备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0否 1是',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_trial_application_phone` (`phone`),
  KEY `idx_trial_application_status` (`status`),
  KEY `idx_trial_application_region_id` (`region_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试用申请表';

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
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一用户表';

-- ----------------------------
-- Table structure for user_wechat
-- ----------------------------
DROP TABLE IF EXISTS `user_wechat`;
CREATE TABLE `user_wechat` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `open_id` varchar(64) NOT NULL COMMENT '微信 openid',
  `union_id` varchar(64) DEFAULT NULL COMMENT '微信 unionid',
  `app_id` varchar(64) NOT NULL COMMENT '小程序 appid',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_open_app` (`open_id`,`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户微信绑定表';

SET FOREIGN_KEY_CHECKS = 1;
