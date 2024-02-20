## SESSION
### 开启SESSION
`sessionStart(time)`

在页面中开启SESSION功能，启用后可在页面中操作SESSION数据。

#### 参数

| 名称   | 说明      | 类型  | 必填  | 默认值  |
|------|---------|-----|-----|------|
| time | 存活时间(秒) | int | 否   | 3600 |


### 关闭SESSION
`sessionClose()`

关闭SESSION，所有保存的SESSION数据将被销毁。

### 读取SESSION
`_SESSION[key]`

#### 参数

| 名称  | 说明  | 类型     | 必填  |
|-----|-----|--------|-----|
| key | 键名  | String | 是   |

#### 返回值
- 类型: **Object**

### 删除SESSION
`_SESSION[key] = null`

#### 参数

| 名称  | 说明  | 类型     | 必填  |
|-----|-----|--------|-----|
| key | 键名  | String | 是   |