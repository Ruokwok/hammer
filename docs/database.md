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
    idle_conn_test: 1800  # 空闲连接测试时间，单位秒
    
```
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