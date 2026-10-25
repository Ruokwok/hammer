## 数据库
hammer提供了数据库相关接口用于连接并操作数据库。

`var database = module("Database")`

### 连接数据库

可以通过以下方法获取一个数据库连接对象。

`database.connect(url, username, password)`

#### 参数

| 名称       | 说明    | 类型     | 必填  |
|----------|-------|--------|-----|
| url      | 数据库地址 | String | 是   |
| username | 用户名   | String | 是   |
| password | 密码    | String | 是   |

#### 返回值
- 类型: [DataBaseConnect](#databaseconnect)
- 数据库连接对象，可用于执行增删改查操作。
- 当连接失败时，返回`null`值。

#### 示例
```
    var database = module("Database");
    var db = database.connect("mysql://localhost:3306/base", "root", "123456");
    var result = db.query("SELECT * FROM `test-table` WHERE id=114514");
    print("name: " + result[0]['id']);
    db.close();
```

### 使用数据库连接池
在站点配置文件中添加`database_pool`字段，即可使用数据库连接池。

#### 示例
配置文件示例
```yaml
database_pool:
  test:   # 连接池名称
    url: 'mysql://localhost:3306/base'  # 数据库地址
    username: 'root'    # 用户名
    password: '123456'  # 密码
    min_size: 5     # 最小连接数
    max_size: 20    # 最大连接数
    max_idle_time: 3600 # 最大空闲时间，单位秒
    idle_conn_test: 60  # 空闲连接测试间隔，单位秒
    checkout_timeout: 10000 # 获取连接的超时时间，单位毫秒
    acquire_retry_attempts: 3 # 创建连接失败后的重试次数
    acquire_retry_delay: 1000 # 创建连接失败后的重试间隔，单位毫秒
    connection_is_valid_timeout: 5 # isValid检测超时时间，单位秒
    max_connection_age: 0 # 连接最大存活时间，单位秒，0为不限制
    test_connection_on_checkin: false # 归还连接时是否检测
    break_after_acquire_failure: false # 重试失败后是否永久关闭连接池
    jdbc_properties: # JDBC驱动参数
      connectTimeout: 5000
      socketTimeout: 10000
      tcpKeepAlive: true
```

#### 连接池配置

| 名称 | 默认值 | 说明 |
|-----|-------|-----|
| `url` | | 数据库地址，不需要填写`jdbc:`前缀 |
| `username` | | 用户名 |
| `password` | | 密码 |
| `init_size` | 5 | 初始化连接数 |
| `min_size` | 5 | 最小连接数 |
| `max_size` | 20 | 最大连接数 |
| `max_idle_time` | 3600 | 最大空闲时间，单位秒 |
| `idle_conn_test` | 60 | 空闲连接测试间隔，单位秒 |
| `preferred_test_query` | 空 | 连接检测SQL，为空时使用`Connection.isValid()` |
| `checkout_timeout` | 10000 | 获取连接的超时时间，单位毫秒，0为不限制 |
| `acquire_retry_attempts` | 3 | 创建连接失败后的重试次数 |
| `acquire_retry_delay` | 1000 | 创建连接失败后的重试间隔，单位毫秒 |
| `connection_is_valid_timeout` | 5 | `Connection.isValid()`检测超时时间，单位秒，0为不限制 |
| `max_connection_age` | 0 | 连接最大存活时间，单位秒，0为不限制 |
| `test_connection_on_checkin` | false | 归还连接时是否检测 |
| `break_after_acquire_failure` | false | 重试失败后是否永久关闭连接池 |
| `jdbc_properties` | | JDBC驱动参数 |

#### 公网数据库

通过公网连接MySQL时，NAT或防火墙可能会静默丢弃长时间空闲的TCP连接，导致下次获取连接时卡顿数十秒。建议：

- 将`idle_conn_test`设置为小于NAT空闲超时的值，例如`60`秒。
- 通过`jdbc_properties`设置`connectTimeout`、`socketTimeout`和`tcpKeepAlive`。
- 将`checkout_timeout`设置为合理值，避免请求无限等待。
- 如仍然出现空闲断连，可将`max_idle_time`设置为小于NAT空闲超时的值，让连接主动过期重建。

注意：`socketTimeout`会限制所有SQL的执行时间，请根据实际查询耗时调整，避免长查询被中断。设置`preferred_test_query`后，c3p0会改用该SQL检测连接并忽略`connection_is_valid_timeout`，此时应同时设置`socketTimeout`，避免死连接检测被无限阻塞。
使用方法示例
```
    var database = module("Database");
    var db = database.get("test");
    var result = db.query("SELECT * FROM `test-table` WHERE id=114514");
    print("name: " + result[0]['id']);
    db.close();
```

hammer内部通过JDBC驱动连接数据库，目前仅支持部分关系型数据库:
- MySQL
- MariaSQL

## DataBaseConnect

数据库连接对象

`var conn = database.connect(url, username, password)`

`var conn = database.get("test")`

- [查询](#查询)
- [更新](#更新)
- [连接是否活跃](#连接是否活跃)
- [连接是否被关闭](#连接是否被关闭)
- [保持连接](#保持连接)
- [开启事务](#开启事务)
- [提交事务](#提交事务)
- [滚回操作](#滚回操作)

### 查询
`conn.query(sql)`

#### 参数

| 名称  | 说明    | 类型     | 必填  |
|-----|-------|--------|-----|
| sql | SQL语句 | String | 是   |

#### 返回值
- 类型: **Array<Json<String,Object>>**
- 返回值为元素类型为键值对的数组

### 更新
`conn.update(sql)`

用于执行增删改操作

#### 参数

| 名称  | 说明    | 类型     | 必填  |
|-----|-------|--------|-----|
| sql | SQL语句 | String | 是   |

#### 返回值
- 类型: **int**
- 返回受到影响的行数。

### 连接是否活跃
`conn.isActive()`

检查是否保持连接
#### 返回值
- 类型: **boolean**
- 返回为true时，说明保持连接，可以操作数据库。

### 连接是否被关闭
`conn.isClosed()`
#### 返回值
- 类型: **boolean**
- 返回为true时，无法继续操作数据库。

### 保持连接
`conn.keep()`

脚本主线程执行结束时，默认会自动关闭连接释放资源，可调用此函数保持连接以便在其他线程中继续使用连接对象。

请注意调用此函数后，系统将不会主动关闭连接，务必在使用结束后调用`close()`函数手动关闭，避免资源占用。

### 开启事务
`conn.trans()`

默认为自动提交，即每次执行update()函数的操作都立即生效，开启事务后将暂停自动提交。

### 提交事务
`conn.commit()`

数据库将写入事务所作的更改

### 滚回操作
`conn.rollback()`

撤销事务所作的更改

### 关于事务
事务是数据库中一系列操作的最小逻辑单元。
在这个逻辑单元中的所有语句，要不都执行成功，要么都执行失败，不存在任何中间状态。
一旦事务执行失败，那么所有的操作都会被撤销，一旦事务执行成功，那么所有的操作结果都会被保存。

## Prepare
预处理对象

- [使用方法](#使用方法)
- [设置查询参数](#设置查询参数)
- [执行查询](#执行查询)
- [执行更新](#执行更新)
- [保持连接](#保持连接)
- [关闭预处理对象](#关闭预处理对象)

### 使用方法
`var prepare = connect.prepare(sql);`

获取预处理对象

#### 参数

| 名称  | 说明    | 类型     | 必填  |
|-----|-------|--------|-----|
| sql | SQL语句 | String | 是   |

#### 返回值
- 类型: Prepare

### 设置查询参数
`prepare.set(index, value)`

#### 参数

| 名称    | 说明 | 类型  | 必填  |
|-------|----|-----|-----|
| index | 索引 | int | 是   |
| value | 值  | any | 是   |
- 索引从1开始

### 执行查询
`prepare.executeQuery()`

#### 返回值
- 类型: **Array<Json<String,Object>>**

### 执行更新
`prepare.executeUpdate()`

### 保持连接
`prepare.keep()`

### 关闭预处理对象
`prepare.close()`