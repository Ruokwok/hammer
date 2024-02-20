## COOKIE
Cookie是浏览器缓存的文本文件，用于存储用户的信息和状态，可以在客户端和服务器之间传递数据。

## 使用
可以通过内置的_COOKIE变量获取Cookie对象

`var cookie = _COOKIE[name]`

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