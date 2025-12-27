# Getting Started

# 设计
company 记录公司信息 、user 登录用户 、 company-user 关联，然后每个 user 有单独的权限，但是他们的账号是一套。

1. 一个账号开通后创建公司，一个账号可以绑定多个公司，一个公司只有一个管理员账号，user 和 company 是多对多关系。

2. 在company_user 记录公司和用户关系，在companyUserType记录账号类型

3. 一个管家可不可以在多个公司任职，添加该管家时如果账号已经创建过，让用户自己申请加入公司。

## 切换公司

1. 登录用户，查看自己绑定的公司列表，选择一个公司，相当于重新做一个遍登录操作，获取该公司下的菜单、权限。

# 规范

## 数据库命名

不要定义is_开头的字段

### 比如 删除字段

✅ 推荐使用：deleted
优点：

1. 语义清晰自然：deleted 表示“是否已被删除”，常见于英语数据库设计规范中。
2. 与布尔语义更贴合：deleted = true 更自然地表达“已删除”。

对齐主流 ORM/框架习惯：

1. Laravel：deleted_at（时间戳）
2. Django：is_deleted（布尔）
3. SQLAlchemy：deleted / is_deleted
4. Spring JPA：逻辑删除一般自定义 deleted

## 数据字典



### 业务字段命名规范表

| 中文      |   英文写法   |                说明                 |
|---------|:--------:|:---------------------------------:|
| 集中式     |  focus   |     集中式, 定义见OperationModeEnum     |
| 分散式     | scatter  |     分散式， 定义见OperationModeEnum     |
| 电话号/手机号 |  phone   | 手机号, 所有手机号都统一使用这个字段，根据不同场景增加对应的前缀 |
| 业主      | landlord |                业主                 |
| 租客      |  tenant  |                租客                 |
| 预定      | booking  |                预定                 |

### 🏠常见费用字段命名规范表

✅ 三、最佳实践建议（总结一句话）

👉 lease 表示“合同关系”，rent 表示“付款行为”。

👉 price 表示单价（单位金额），amount 表示金额总数。

所以：

在“房源/合同”层面（描述租金标准）用 price ✅

在“账单/支付”层面（实际金额流转）用 amount ✅

| 中文    | 英文写法                               | 系统字段命名建议（CamelCase） | 数据库字段命名建议（snake\_case）                | 说明              |
|-------|------------------------------------|---------------------|---------------------------------------|-----------------|
| 租金    | rent_price                       | `leaseAmount`       | `lease_amount`                        | 每月/每期支付的费用      |
| 定金    | Booking Fee                        | `bookingFee`        | `reservation_deposit` / `booking_fee` | 预订时支付，用于锁定房源    |
| 押金    | Security Deposit                   | `securityDeposit`   | `security_deposit`                    | 合同履约保证，租期结束后可退还 |
| 违约金   | Penalty / Liquidated Damages       | `penaltyFee`        | `penalty_fee`                         | 租客/房东违约时支付的费用   |
| 服务费   | Service Fee                        | `serviceFee`        | `service_fee`                         | 中介、物业或平台收取的服务费用 |
| 管理费   | Management Fee                     | `managementFee`     | `management_fee`                      | 物业/平台日常管理费用     |
| 水费    | Water Bill / Water Fee             | `waterFee`          | `water_fee`                           | 按用量或固定金额收取      |
| 电费    | Electricity Bill / Electricity Fee | `electricityFee`    | `electricity_fee`                     | 按用量或固定金额收取      |
| 燃气费   | Gas Bill / Gas Fee                 | `gasFee`            | `gas_fee`                             | 公寓/住房燃气使用费用     |
| 网费    | Internet Fee                       | `internetFee`       | `internet_fee`                        | 宽带/网络使用费用       |
| 维修费   | Maintenance Fee                    | `maintenanceFee`    | `maintenance_fee`                     | 租期内维修产生的费用      |
| 清洁费   | Cleaning Fee                       | `cleaningFee`       | `cleaning_fee`                        | 租客搬出时的清洁费用      |
| 逾期滞纳金 | Late Fee                           | `lateFee`           | `late_fee`                            | 租金逾期支付的附加费用     |
| 其他费用  | Other Fee                          | `otherFee`          | `other_fee`                           | 灵活扩展项，便于存放额外费用  |

> “排序” 字段使用 sort_order 而不是 sort。

# 代码开发手册

*Repo 不能调用其他Repo

Service 可以调用 Repo 和其他 Service
