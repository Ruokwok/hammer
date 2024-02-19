## 内置函数
除了标准JavaScript引擎的函数外，hammer环境还提供一些内置函数。

- [输出内容](#输出内容)
- [引入其他页面](#引入其他页面)
- [获取文件对象](#获取文件对象)
- [设置HTTP状态码](#设置http状态码)
- [开启SESSION](#开启session)
- [关闭SESSION](#关闭session)
- [添加COOKIE](#添加cookie)
- [计算MD5](#计算md5)

### 输出内容
`print(str)`

`echo(str)`

在网页中输出内容，两个函数的效果相同

#### 参数

| 名称  | 说明    | 类型     | 必填  |
|-----|-------|--------|-----|
| str | 输出的内容 | String | 是   |

### 引入其他页面
`include(page)`

在脚本中引入其他页面，可以是另一脚本

#### 参数

| 名称   | 说明   | 类型     | 必填  |
|------|------|--------|-----|
| page | 文件路径 | String | 是   |
- page为相对路径时，当前目录为网站的根目录，而不是脚本所在的目录。
- 每个脚本只能被引入一次，重复引入或循环引入时会抛出异常。

### 获取文件对象
`getFile(filename)`

#### 参数

| 名称       | 说明   | 类型     | 必填  |
|----------|------|--------|-----|
| fimename | 文件路径 | String | 是   |

#### 返回值
- 类型：**File**
- 文件的对象，关于文件接口请查看[文件](file.md)

### 设置HTTP状态码
`setStatus(code)`

修改HTTP响应的状态码，默认为`200`。

#### 参数

| 名称   | 说明  | 类型  | 必填  |
|------|-----|-----|-----|
| code | 状态码 | int | 是   |

### 开启SESSION
`sessionStart()`

在页面中开启SESSION功能，启用后可在页面中操作SESSION数据。

### 关闭SESSION
`sessionClose()`

关闭会话的SESSION，所有保存的SESSION数据将被销毁。

### 添加COOKIE
`addCookie(name, value, path, domain, age, httpOnly)`

向浏览器发送COOKIE数据

#### 参数

| 名称       | 说明        | 类型      | 必填  |
|----------|-----------|---------|-----|
| name     | 键名        | String  | 是   |
| value    | 值         | Object  | 是   |
| path     | 路径        | String  | 否   |
| domain   | 域名        | String  | 否   |
| age      | 存活时间      | int     | 否   |
| httpOnly | 仅在http时生效 | boolean | 否   |

### 删除COOKIE
`removeCookie(name)`

#### 参数

| 名称   | 说明  | 类型     | 必填  |
|------|-----|--------|-----|
| name | 键名  | String | 是   |

### 计算MD5
`md5(str)`

`md5(file)`

计算字符串或文件的MD5值

#### 参数

| 名称   | 说明     | 类型     |
|------|--------|--------|
| str  | 待计算字符串 | String |
| file | 待计算文件  | File   |

#### 返回值
- 类型: **String**
- 返回文件或字符串的32位MD5值