# 存储服务数据库初始化说明

本目录包含存储服务（storage-service）所需的数据库初始化脚本。

## 初始化步骤

### 方法一：直接执行SQL脚本

如果您的MySQL服务已经启动，可以直接执行以下命令初始化数据库：

```bash
# 进入Docker容器（根据实际的容器名称调整）
docker exec -it mysql_container bash

# 在容器内执行MySQL命令
mysql -u root -p < /path/to/init.sql
```

### 方法二：使用Docker挂载卷

如果您在启动Docker容器时，已经将`/docker-entrypoint-initdb.d/`目录映射到了主机目录，可以将`init.sql`脚本放入该目录，MySQL容器在初始化时会自动执行该目录下的所有`.sql`、`.sh`和`.sql.gz`文件。

```bash
# 将初始化脚本复制到Docker卷目录
cp init.sql /path/to/docker-entrypoint-initdb.d/
```

### 方法三：手动导入

```bash
# 连接到MySQL服务器
mysql -u root -p

# 然后在MySQL命令行中执行
source /path/to/init.sql
```

## 数据库表说明

### metadata

元数据表，用于存储各种配置和元数据信息。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| type | varchar(50) | 元数据类型 |
| ref_id | varchar(100) | 关联ID |
| meta_key | varchar(100) | 元数据键 |
| meta_value | text | 元数据值 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### time_series_index

时序数据索引表（可选），用于加快查询速度，记录时序数据的一些统计信息。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| asset_id | varchar(100) | 资产ID |
| metric_name | varchar(100) | 指标名称 |
| first_timestamp | datetime | 首次记录时间 |
| last_timestamp | datetime | 最后记录时间 |
| sample_count | bigint | 样本数量 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

## 注意事项

1. 确保您的MySQL服务已经启动并且可以连接
2. 根据实际环境修改数据库连接信息（用户名、密码、主机等）
3. 如果您使用的不是Docker环境，请根据您的实际环境调整初始化步骤 