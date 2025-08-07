# Getting Started

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

|   字典    |            解释             |
|:-------:|:-------------------------:|
|  focus  | 集中式, 定义见OperationModeEnum |
| scatter | 分散式， 定义见OperationModeEnum |