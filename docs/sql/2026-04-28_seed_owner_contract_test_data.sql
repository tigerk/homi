-- 业主合同/房源测试数据
-- 用途：
-- 1. 生成 100 套普通房源：前 50 套整租，后 50 套合租。
-- 2. 生成 10 套包租房源：包含个人业主、业主合同、合同房源、包租规则、首期未付应付单。
-- 3. 便于测试业主合同续约、作废、退房、包租应付单等功能。
--
-- 注意：
-- 1. 默认企业 ID 使用开发库常见 company_id：2038455065434607618。
-- 2. 如需换企业，只修改下面 @company_id 即可。
-- 3. 本脚本使用固定测试 ID 段，可重复执行；再次执行会先清理本脚本生成的数据。

SET @company_id := 2038455065434607618;
SET @operator_id := COALESCE((SELECT id FROM `user` WHERE deleted = 0 ORDER BY id LIMIT 1), 13810428094);
SET @dept_id := COALESCE((SELECT id FROM dept WHERE company_id = @company_id AND deleted = 0 ORDER BY id LIMIT 1), @company_id);

SET @community_id := 2099000000000000001;
SET @layout_entire_id := 2099000000000000002;
SET @layout_shared_id := 2099000000000000003;

SET @house_base_id := 2099000000000100000;
SET @room_base_id := 2099000000000200000;
SET @owner_personal_base_id := 2099000000000300000;
SET @owner_base_id := 2099000000000400000;
SET @owner_account_base_id := 2099000000000500000;
SET @contract_base_id := 2099000000000600000;
SET @contract_subject_base_id := 2099000000000700000;
SET @owner_lease_rule_base_id := 2099000000000900000;
SET @payable_bill_base_id := 2099000000001000000;
SET @payable_bill_fee_base_id := 2099000000001100000;

DROP TEMPORARY TABLE IF EXISTS seed_n;
CREATE TEMPORARY TABLE seed_n (
  n INT PRIMARY KEY
);
INSERT INTO seed_n(n) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),(61),(62),(63),(64),(65),(66),(67),(68),(69),(70),(71),(72),(73),(74),(75),(76),(77),(78),(79),(80),(81),(82),(83),(84),(85),(86),(87),(88),(89),(90),(91),(92),(93),(94),(95),(96),(97),(98),(99),(100),(101),(102),(103),(104),(105),(106),(107),(108),(109),(110);

-- 清理本脚本生成的数据，保证可重复执行。
DELETE FROM owner_payable_bill_fee WHERE bill_id BETWEEN @payable_bill_base_id + 1 AND @payable_bill_base_id + 10;
DELETE FROM owner_payable_bill_payment WHERE bill_id BETWEEN @payable_bill_base_id + 1 AND @payable_bill_base_id + 10;
DELETE FROM owner_payable_bill WHERE id BETWEEN @payable_bill_base_id + 1 AND @payable_bill_base_id + 10;
DELETE FROM owner_contract_checkout WHERE owner_contract_id BETWEEN @contract_base_id + 1 AND @contract_base_id + 10;
DELETE FROM owner_lease_fee WHERE contract_id BETWEEN @contract_base_id + 1 AND @contract_base_id + 10;
DELETE FROM owner_lease_free_rule WHERE contract_id BETWEEN @contract_base_id + 1 AND @contract_base_id + 10;
DELETE FROM owner_lease_rule WHERE contract_id BETWEEN @contract_base_id + 1 AND @contract_base_id + 10;
DELETE FROM owner_contract_subject WHERE contract_id BETWEEN @contract_base_id + 1 AND @contract_base_id + 10;
DELETE FROM owner_contract WHERE id BETWEEN @contract_base_id + 1 AND @contract_base_id + 10;
DELETE FROM owner_account WHERE owner_id BETWEEN @owner_base_id + 1 AND @owner_base_id + 10;
DELETE FROM owner WHERE id BETWEEN @owner_base_id + 1 AND @owner_base_id + 10;
DELETE FROM owner_personal WHERE id BETWEEN @owner_personal_base_id + 1 AND @owner_personal_base_id + 10;
DELETE FROM room WHERE house_id BETWEEN @house_base_id + 1 AND @house_base_id + 110;
DELETE FROM house WHERE id BETWEEN @house_base_id + 1 AND @house_base_id + 110;

-- 基础小区与房型。
INSERT INTO community (
  id, name, alias, province, city_id, city, district, township, adcode, address, business_area,
  longitude, latitude, built_year, building_count, household_count, greening_rate, plot_ratio,
  property_company, developer, deleted, create_by, create_at, update_by, update_at
) VALUES (
  @community_id, '测试小区-业主合同专用', '业主合同测试小区', '北京市', 110100, '北京市', '朝阳区', '测试街道', '110105',
  '北京市朝阳区测试路 100 号', '测试商圈', 116.480000, 39.920000, 2018, 12, 1200, 35.00, 2.50,
  '测试物业公司', '测试开发商', 0, @operator_id, NOW(), @operator_id, NOW()
) ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  update_by = VALUES(update_by),
  update_at = VALUES(update_at);

INSERT INTO house_layout (
  id, company_id, lease_mode, lease_mode_id, layout_name, living_room, bathroom, kitchen, bedroom,
  tags, facilities, image_list, video_list, deleted, create_by, create_at, update_by, update_at
) VALUES
  (
    @layout_entire_id, @company_id, 2, @community_id, '测试整租两居', 1, 1, 1, 2,
    '["整租","测试"]',
    '[{"name":"BED","count":"1"},{"name":"WARDROBE","count":"1"},{"name":"AIR_CONDITIONER","count":"1"},{"name":"WASHER","count":"1"},{"name":"WIFI","count":"1"}]',
    '[]', '[]',
    0, @operator_id, NOW(), @operator_id, NOW()
  ),
  (
    @layout_shared_id, @company_id, 2, @community_id, '测试合租三居', 1, 1, 1, 3,
    '["合租","测试"]',
    '[{"name":"BED","count":"3"},{"name":"WARDROBE","count":"3"},{"name":"AIR_CONDITIONER","count":"3"},{"name":"DESK","count":"3"},{"name":"WIFI","count":"1"}]',
    '[]', '[]',
    0, @operator_id, NOW(), @operator_id, NOW()
  )
ON DUPLICATE KEY UPDATE
  layout_name = VALUES(layout_name),
  tags = VALUES(tags),
  facilities = VALUES(facilities),
  image_list = VALUES(image_list),
  video_list = VALUES(video_list),
  update_by = VALUES(update_by),
  update_at = VALUES(update_at);

-- 100 套普通房源 + 10 套包租房源。
INSERT INTO house (
  id, house_code, house_name, company_id, dept_id, salesman_id, lease_mode, lease_mode_id, community_id,
  building, unit, door_number, house_layout_id, rental_type, area, direction, decoration_type, floor, floor_total,
  water, electricity, heating, has_elevator, has_gas, property_fee, heating_fee, mgmt_fee,
  room_count, rest_room_count, certificate_no, shared_owner, mortgaged, customer_id, house_status,
  approval_status, locked, closed, house_desc, business_desc, remark, deleted, create_by, create_at, update_by, update_at
)
SELECT
  @house_base_id + n,
  CASE
    WHEN n <= 50 THEN CONCAT('SEED-ENTIRE-', LPAD(n, 3, '0'))
    WHEN n <= 100 THEN CONCAT('SEED-SHARED-', LPAD(n - 50, 3, '0'))
    ELSE CONCAT('SEED-MASTER-', LPAD(n - 100, 3, '0'))
  END,
  CASE
    WHEN n <= 50 THEN CONCAT('测试整租房源-', LPAD(n, 3, '0'))
    WHEN n <= 100 THEN CONCAT('测试合租房源-', LPAD(n - 50, 3, '0'))
    ELSE CONCAT('测试包租房源-', LPAD(n - 100, 3, '0'))
  END,
  @company_id,
  @dept_id,
  @operator_id,
  CASE WHEN n BETWEEN 51 AND 100 THEN 3 ELSE 2 END,
  @community_id,
  @community_id,
  CONCAT('T', 1 + MOD(n, 8), '栋'),
  CONCAT(1 + MOD(n, 3), '单元'),
  CONCAT(1000 + n, '室'),
  CASE WHEN n BETWEEN 51 AND 100 THEN @layout_shared_id ELSE @layout_entire_id END,
  CASE WHEN n BETWEEN 51 AND 100 THEN 2 ELSE 1 END,
  CASE
    WHEN n BETWEEN 51 AND 100 THEN 98 + MOD(n, 25)
    WHEN n > 100 THEN 72 + MOD(n, 18)
    ELSE 58 + MOD(n, 22)
  END,
  CASE MOD(n, 4) WHEN 0 THEN '南' WHEN 1 THEN '南北' WHEN 2 THEN '东' ELSE '西南' END,
  3,
  1 + MOD(n, 25),
  30,
  '民水',
  '民电',
  '集中供暖',
  1,
  1,
  5.00,
  0.00,
  0.00,
  CASE WHEN n BETWEEN 51 AND 100 THEN 3 ELSE 1 END,
  CASE WHEN n BETWEEN 51 AND 100 THEN 3 ELSE 1 END,
  CONCAT('CERT-SEED-', LPAD(n, 3, '0')),
  0,
  0,
  NULL,
  1,
  2,
  0,
  0,
  '业主合同测试数据',
  '用于业主合同续约、作废、退房测试',
  CASE WHEN n > 100 THEN '包租业主合同测试房源' ELSE '普通房源测试数据' END,
  0,
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 110;

-- 整租与包租房源：每套 1 间整租房间。
INSERT INTO room (
  id, company_id, house_id, keywords, floor, room_number, room_type, price, area, direction,
  vacancy_start_at, available_date, remark, occupancy_status, locked, closed, deleted, tags, facilities,
  image_list, video_list, create_by, create_at, update_by, update_at
)
SELECT
  @room_base_id + n * 10 + 1,
  @company_id,
  @house_base_id + n,
  CONCAT(
    CASE
      WHEN n <= 50 THEN CONCAT('测试整租房源-', LPAD(n, 3, '0'))
      ELSE CONCAT('测试包租房源-', LPAD(n - 100, 3, '0'))
    END,
    '-整租'
  ),
  1 + MOD(n, 25),
  '整租',
  1,
  CASE WHEN n > 100 THEN 5200 + (n - 100) * 300 ELSE 4200 + n * 30 END,
  CASE WHEN n > 100 THEN 72 + MOD(n, 18) ELSE 58 + MOD(n, 22) END,
  CASE MOD(n, 4) WHEN 0 THEN '南' WHEN 1 THEN '南北' WHEN 2 THEN '东' ELSE '西南' END,
  NOW(),
  CURDATE(),
  CASE WHEN n > 100 THEN '包租房源整租房间' ELSE '普通整租房间' END,
  0,
  0,
  0,
  0,
  '["整租","可签约"]',
  '[{"name":"BED","count":"1"},{"name":"WARDROBE","count":"1"},{"name":"AIR_CONDITIONER","count":"1"},{"name":"WASHER","count":"1"},{"name":"WIFI","count":"1"}]',
  '[]',
  '[]',
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 50 OR n > 100;

-- 合租房源：每套 3 间。
INSERT INTO room (
  id, company_id, house_id, keywords, floor, room_number, room_type, price, area, direction,
  vacancy_start_at, available_date, remark, occupancy_status, locked, closed, deleted, tags, facilities,
  image_list, video_list, create_by, create_at, update_by, update_at
)
SELECT
  @room_base_id + h.n * 10 + r.m,
  @company_id,
  @house_base_id + h.n,
  CONCAT('测试合租房源-', LPAD(h.n - 50, 3, '0'), '-', CHAR(64 + r.m), '房'),
  1 + MOD(h.n, 25),
  CONCAT(CHAR(64 + r.m), '房'),
  1,
  1600 + (h.n - 50) * 20 + r.m * 150,
  18 + r.m * 4 + MOD(h.n, 6),
  CASE r.m WHEN 1 THEN '南' WHEN 2 THEN '北' ELSE '东' END,
  NOW(),
  CURDATE(),
  '普通合租房间',
  0,
  0,
  0,
  0,
  '["合租","可签约"]',
  '[{"name":"BED","count":"1"},{"name":"WARDROBE","count":"1"},{"name":"AIR_CONDITIONER","count":"1"},{"name":"DESK","count":"1"},{"name":"WIFI","count":"1"}]',
  '[]',
  '[]',
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n h
       JOIN (SELECT 1 AS m UNION ALL SELECT 2 UNION ALL SELECT 3) r
WHERE h.n BETWEEN 51 AND 100;

-- 10 个包租个人业主。
INSERT INTO owner_personal (
  id, company_id, name, gender, id_type, id_no, phone, payee_name, payee_phone, payee_id_type, payee_id_no,
  bank_account_name, bank_account_no, bank_name, tags, remark, status, deleted, create_by, create_at, update_by, update_at
)
SELECT
  @owner_personal_base_id + n,
  @company_id,
  CONCAT('测试包租业主-', LPAD(n, 2, '0')),
  CASE WHEN MOD(n, 2) = 0 THEN 2 ELSE 1 END,
  0,
  CONCAT('11010119800101', LPAD(n, 4, '0')),
  CONCAT('1390001', LPAD(n, 4, '0')),
  CONCAT('测试包租业主-', LPAD(n, 2, '0')),
  CONCAT('1390001', LPAD(n, 4, '0')),
  0,
  CONCAT('11010119800101', LPAD(n, 4, '0')),
  CONCAT('测试包租业主-', LPAD(n, 2, '0')),
  CONCAT('622202020000', LPAD(n, 7, '0')),
  '中国工商银行北京测试支行',
  '["测试","包租"]',
  '业主合同测试数据',
  1,
  0,
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 10;

INSERT INTO owner (
  id, company_id, owner_type_id, owner_type, owner_name, owner_phone, status, deleted, create_by, create_at, update_by, update_at
)
SELECT
  @owner_base_id + n,
  @company_id,
  @owner_personal_base_id + n,
  0,
  CONCAT('测试包租业主-', LPAD(n, 2, '0')),
  CONCAT('1390001', LPAD(n, 4, '0')),
  1,
  0,
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 10;

INSERT INTO owner_account (
  id, company_id, owner_id, account_status, available_amount, frozen_amount, pending_settlement_amount,
  total_income_amount, total_reduction_amount, total_withdraw_amount, version, create_at, update_at
)
SELECT
  @owner_account_base_id + n,
  @company_id,
  @owner_base_id + n,
  1,
  0.00,
  0.00,
  0.00,
  0.00,
  0.00,
  0.00,
  0,
  NOW(),
  NOW()
FROM seed_n
WHERE n <= 10;

-- 包租业主合同。
INSERT INTO owner_contract (
  id, company_id, owner_id, cooperation_mode, contract_no, contract_template_id, contract_content,
  sign_status, sign_type, contract_medium, notify_owner, contract_start, contract_end, status, approval_status,
  remark, parent_contract_id, contract_nature, renew_from_contract_no, checkout_status, checkout_date,
  checkout_reason, checkout_by, checkout_by_name, checkout_at, deleted, create_by, create_at, update_by, update_at
)
SELECT
  @contract_base_id + n,
  @company_id,
  @owner_base_id + n,
  'MASTER_LEASE',
  CONCAT('OWN-SEED-ML-', LPAD(n, 3, '0')),
  NULL,
  CONCAT('<p>测试包租合同 ', LPAD(n, 3, '0'), '</p>'),
  1,
  'NEW',
  'ELECTRONIC',
  0,
  '2026-05-01 00:00:00',
  '2027-04-30 23:59:59',
  1,
  2,
  '包租业主合同测试数据，可用于续约、作废、退房测试',
  NULL,
  1,
  NULL,
  0,
  NULL,
  NULL,
  NULL,
  NULL,
  NULL,
  0,
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 10;

INSERT INTO owner_contract_subject (
  id, company_id, contract_id, subject_type, subject_id, subject_name_snapshot, remark, status, deleted,
  create_by, create_at, update_by, update_at
)
SELECT
  @contract_subject_base_id + n,
  @company_id,
  @contract_base_id + n,
  'HOUSE',
  @house_base_id + 100 + n,
  CONCAT('测试包租房源-', LPAD(n, 3, '0')),
  '包租合同房源',
  1,
  0,
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 10;

INSERT INTO owner_lease_rule (
  id, company_id, contract_id, rent_amount, deposit_amount, deposit_months, payment_months, pay_way,
  rent_due_type, rent_due_day, rent_due_offset_days, first_pay_date, handover_date, usage_type,
  billing_start, billing_end, prorate_type, status, remark, deleted, create_by, create_at, update_by, update_at
)
SELECT
  @owner_lease_rule_base_id + n,
  @company_id,
  @contract_base_id + n,
  5200 + n * 300,
  5200 + n * 300,
  1,
  1,
  '押1付1',
  1,
  NULL,
  15,
  '2026-05-01 00:00:00',
  '2026-05-01 00:00:00',
  '居住',
  '2026-05-01 00:00:00',
  '2027-04-30 23:59:59',
  'BY_DAYS',
  1,
  '包租规则测试数据',
  0,
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 10;

-- 每个包租合同生成一张首期未付应付单，便于测试包租应付单、退房时未付账单处理。
INSERT INTO owner_payable_bill (
  id, company_id, owner_id, contract_id, subject_name_snapshot, bill_no, bill_scene,
  bill_start_date, bill_end_date, due_date, payable_amount, paid_amount, unpaid_amount, adjust_amount,
  payment_status, bill_status, cancel_reason, cancel_by, cancel_by_name, cancel_at, generated_at, remark,
  deleted, create_by, create_at, update_by, update_at
)
SELECT
  @payable_bill_base_id + n,
  @company_id,
  @owner_base_id + n,
  @contract_base_id + n,
  CONCAT('测试包租房源-', LPAD(n, 3, '0')),
  CONCAT('AP-SEED-ML-', LPAD(n, 3, '0')),
  'REGULAR',
  '2026-05-01',
  '2026-05-31',
  '2026-04-16',
  5200 + n * 300,
  0.00,
  5200 + n * 300,
  0.00,
  0,
  1,
  NULL,
  NULL,
  NULL,
  NULL,
  NOW(),
  '测试数据：包租首期应付单',
  0,
  @operator_id,
  NOW(),
  @operator_id,
  NOW()
FROM seed_n
WHERE n <= 10;

INSERT INTO owner_payable_bill_fee (
  id, company_id, bill_id, source_type, source_id, subject_name_snapshot, fee_type, dict_data_id,
  fee_name, direction, amount, biz_date, remark, formula_snapshot, create_at
)
SELECT
  @payable_bill_fee_base_id + n,
  @company_id,
  @payable_bill_base_id + n,
  'OWNER_CONTRACT',
  @contract_base_id + n,
  CONCAT('测试包租房源-', LPAD(n, 3, '0')),
  'RENT',
  NULL,
  '包租租金',
  'IN',
  5200 + n * 300,
  '2026-05-01',
  '测试数据：包租首期租金',
  'seed: owner_lease_rule.rent_amount',
  NOW()
FROM seed_n
WHERE n <= 10;

SELECT
  'seed_owner_contract_test_data_done' AS result,
  100 AS normal_house_count,
  10 AS master_lease_house_count,
  10 AS master_lease_contract_count,
  10 AS owner_payable_bill_count;

-- 如果旧版本脚本已执行过，可单独执行以下修复语句，将字符串数组设施修正为 FacilityItemDTO 对象数组。
-- UPDATE house_layout
-- SET facilities = '[{"name":"BED","count":"1"},{"name":"WARDROBE","count":"1"},{"name":"AIR_CONDITIONER","count":"1"},{"name":"WASHER","count":"1"},{"name":"WIFI","count":"1"}]',
--     update_by = @operator_id,
--     update_at = NOW()
-- WHERE id = @layout_entire_id;
--
-- UPDATE house_layout
-- SET facilities = '[{"name":"BED","count":"3"},{"name":"WARDROBE","count":"3"},{"name":"AIR_CONDITIONER","count":"3"},{"name":"DESK","count":"3"},{"name":"WIFI","count":"1"}]',
--     update_by = @operator_id,
--     update_at = NOW()
-- WHERE id = @layout_shared_id;
--
-- UPDATE room
-- SET facilities = '[{"name":"BED","count":"1"},{"name":"WARDROBE","count":"1"},{"name":"AIR_CONDITIONER","count":"1"},{"name":"WASHER","count":"1"},{"name":"WIFI","count":"1"}]',
--     update_by = @operator_id,
--     update_at = NOW()
-- WHERE house_id BETWEEN @house_base_id + 1 AND @house_base_id + 50
--    OR house_id BETWEEN @house_base_id + 101 AND @house_base_id + 110;
--
-- UPDATE room
-- SET facilities = '[{"name":"BED","count":"1"},{"name":"WARDROBE","count":"1"},{"name":"AIR_CONDITIONER","count":"1"},{"name":"DESK","count":"1"},{"name":"WIFI","count":"1"}]',
--     update_by = @operator_id,
--     update_at = NOW()
-- WHERE house_id BETWEEN @house_base_id + 51 AND @house_base_id + 100;
