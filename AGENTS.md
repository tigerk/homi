# AGENTS.md

本文件是 `homi` 后端项目的长期协作规则。处理 PR、代码改动、排查问题和生成实现方案时，优先遵守这里的约定；如果与更具体的用户指令冲突，以用户当前指令为准。

## 项目定位

`homi` 是房屋租赁/资产管理系统后端，采用 Java 25、Spring Boot 4、Maven 多模块架构。当前包含 SaaS 端和平台端两套 Web 应用：

- SaaS 端入口：`homi-saas/saas-web/src/main/java/com/homi/saas/web/SaasApplication.java`
- 平台端入口：`homi-platform/platform-web/src/main/java/com/homi/platform/web/PlatformApplication.java`

主要技术栈：

- Spring Boot 4.x
- MyBatis-Plus
- Sa-Token
- MySQL
- Redis
- MapStruct
- Lombok
- Mockito/JUnit 5

## 模块边界

模块依赖方向必须保持为：

```text
homi-common -> homi-model -> homi-service -> web
```

含义：

- `homi-common/common-lib`：通用工具、统一响应、异常、枚举、注解、基础配置。
- `homi-model`：实体、DTO、VO、Repo、Mapper 等数据与领域模型。
- `homi-external`：外部服务适配，如支付、短信、邮件、签约、地图、存储。
- `homi-service`：核心业务编排，允许调用 Repo、External 和其他 Service。
- `homi-saas/saas-service`：SaaS 业务扩展服务。
- `homi-saas/saas-web`：SaaS Controller、鉴权配置、Web 层 DTO/VO。
- `homi-platform/platform-service`：平台管理业务扩展服务。
- `homi-platform/platform-web`：平台 Controller、鉴权配置、Web 层 DTO/VO。

硬性规则：

- Repo 不能调用其他 Repo。
- Repo 不能调用 Service。
- Service 可以调用 Repo 和其他 Service。
- Web 层只做参数校验、权限入口、响应组装和调用 Service，不写核心业务。
- 不要让 SaaS 端代码依赖 Platform Web 层，反之亦然；共享能力应沉到 common/model/service/external 中。
- 新增跨模块依赖前先确认方向，不为单个调用反向依赖上层模块。

## 命名与领域语言

数据库字段：

- 逻辑删除字段用 `deleted`，不要用 `is_deleted`。
- 排序字段用 `sort_order`，不要用 `sort`。
- 仅表示日期的字段用 `*_date`，如 `bill_start_date`、`due_date`、`biz_date`。
- 表示事件发生时刻的字段用 `*_at`，如 `generated_at`、`approved_at`、`cancel_at`、`pay_at`。
- 手机号统一用 `phone`，按场景加前缀。
- 单价用 `price`，金额总数用 `amount`。
- 合同关系用 `lease`，租金相关用 `rent`，租赁行为用 `rental`。

业务词汇：

- 集中式：`focus`
- 分散式：`scatter`
- 业主：`owner`
- 租客：`tenant`
- 预定：`booking`
- 公司：`company`
- 用户与公司关系：`company_user`

Java 命名：

- 包名保持小写，遵循现有 `com.homi.<module>.<domain>` 结构。
- DTO 用于请求参数，VO 用于返回视图，Entity 对应数据库实体。
- 枚举统一放在 `com.homi.common.lib.enums` 及其领域子包下。
- 金额字段使用 `BigDecimal`，不要用 `double` 或 `float`。
- 时间字段按语义使用 `LocalDate`、`LocalDateTime` 或已有项目约定，避免用字符串传递时间。

## Controller 与响应

- Controller 返回统一响应模型，优先使用项目已有的 `ResponseResult` 体系。
- 请求参数使用 DTO 并配合 Jakarta Validation 注解。
- Controller 方法保持薄层，不在 Controller 中直接操作 Repo。
- 新接口路径、请求方式和命名应与相邻 Controller 保持一致。
- 新增枚举字段时，确认 OpenAPI/前端代码生成需要的枚举元数据是否完整。
- 不把内部异常、三方错误原文或敏感配置直接返回给前端。

## Service 与事务

- 涉及多表写入、状态流转、支付/审批/账单等一致性操作时，在 Service 层声明事务。
- 事务边界应包住完整业务动作，不要只包单个 Repo 调用。
- 状态流转要显式校验当前状态，避免重复提交、越权修改或逆向流转。
- 支付、退款、作废、审批、账单生成等操作必须考虑幂等。
- 批量操作优先使用已有 Repo/Service 的批量方法，避免无意义循环查库。
- 不要用魔法数字表达状态，使用枚举。

## Repo、Mapper 与 SQL

- 优先使用现有 MyBatis-Plus Repo/Mapper 风格。
- 查询条件要包含租户、公司或业务归属约束，除非明确是平台全局查询。
- 列表接口必须分页，避免无上限查询。
- 软删除数据默认过滤 `deleted`。
- 自定义 SQL 需要明确排序、分页、状态过滤和索引影响。
- 新增字段或表时，在 `docs/sql/` 下补充可执行 SQL，文件名包含日期和主题。

## 多租户与权限

系统支持一个用户绑定多个公司。公司切换等同于重新获得该公司下的菜单和权限。

处理相关代码时必须确认：

- 当前登录上下文来自 SaaS 端还是平台端。
- 公司、用户、角色、菜单、权限是否按当前公司隔离。
- 平台端管理能力是否不应泄漏到 SaaS 端。
- 查询、导出、详情、操作接口都要有一致的权限和数据边界。

## 外部服务与敏感信息

- 三方支付、短信、邮件、签约、地图、对象存储等集成应放在 `homi-external`。
- 配置项走 Spring 配置绑定，不在代码里硬编码密钥、商户号、回调地址或私钥路径。
- `.gitignore` 中已排除的环境配置、证书、私钥和本地配置不能提交。
- 回调接口要校验签名、来源、幂等键和业务状态。

## 测试与验证

常用命令：

当前本机默认 Java 是 Java 8，`~/.zshrc` 已配置 Java 25 Maven Wrapper alias：`mvnw25='JAVA_HOME=/opt/homebrew/opt/openjdk@25 PATH=/opt/homebrew/opt/openjdk@25/bin:$PATH ./mvnw'`。运行本项目 Maven 命令时使用 `mvnw25`，不要直接用 `./mvnw`。

```bash
mvnw25 test
mvnw25 -pl homi-service test
mvnw25 -pl homi-service,homi-saas/saas-web -am -DskipTests compile
mvnw25 -pl homi-service,homi-platform/platform-web -am -DskipTests compile
```

验证原则：

- 改动核心业务逻辑时，补充或更新单元测试。
- 金额、账单周期、支付状态、审批状态、租约日期计算必须覆盖边界场景。
- 只改 Web 层时至少运行相关模块 compile。
- 无法运行测试时，在最终说明中写清楚原因和风险。
- 测试代码不要使用 `System.out.println` 作为断言替代；断言必须表达业务结果。

## PR 与代码改动流程

开始改动前：

- 先查看 `git status --short`，识别用户已有未提交改动。
- 不回滚、不格式化、不移动与任务无关的文件。
- 先读相邻代码，沿用现有命名、异常处理、响应包装和目录结构。

实现时：

- 改动范围尽量贴近需求，不做顺手重构。
- 公共抽象只有在真实降低重复和复杂度时才新增。
- 修改接口契约时同步更新 DTO/VO、OpenAPI 暴露、前端生成依赖和 SQL。
- 新增功能必须考虑权限、租户、幂等、事务、日志和异常。

提交/交付前：

- 运行与改动范围匹配的 Maven 验证命令。
- 检查是否误提交 `target/`、`logs/`、本地配置、证书、私钥。
- 总结行为变化、验证命令和未验证风险。

## Review 重点

Review PR 时优先找这些问题：

- 模块依赖方向被破坏。
- Controller 承担业务逻辑或直接调用 Repo。
- 查询缺少公司/租户/权限边界。
- 状态流转未校验当前状态。
- 金额计算使用浮点数或缺少精度处理。
- 写操作缺少事务或幂等。
- 新字段命名不符合数据库规范。
- 新增接口未统一响应、未校验参数、未处理异常。
- 测试没有覆盖金额、日期、状态和重复提交边界。
