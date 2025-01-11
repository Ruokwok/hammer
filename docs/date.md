## 日期与时间
`var date = module("Date");`

## Date

- [获取当前时间戳](#获取当前时间戳)
- [格式化时间](#格式化时间)
- [获取当前年份](#获取当前年份)
- [获取当前月份](#获取当前月份)
- [获取当前日](#获取当前日)
- [获取当前日为今年第几天](#获取当前日为今年第几天)
- [获取当前星期](#获取当前星期)

### 获取当前时间戳
`date.getTime()`

#### 返回值
- 类型: **long**
- 毫秒级13位时间戳

### 格式化时间
`date.format(exp, time)`

#### 参数

| 名称   | 说明  | 类型     | 必填  |
|------|-----|--------|-----|
| exp  | 表达式 | String | 是   |
| time | 时间戳 | long   | 否   |
- time参数省略时，格式化当前时间

#### 返回值
- 类型：**String**

#### 示例
```js
    var date = module("Date");
    var time = 1708184706000;       //时间戳
    var str = date.format("yyyy-MM-dd hh:mm:ss", time); //格式化时间
    print(str);         //输出为 2024-02-17 11:45:06
```

### 获取当前年份
`date.getYear()`

#### 返回值
- 类型：**int**

### 获取当前月份
`date.getMonth()`

#### 返回值
- 类型：**int**
- 真实月份无需+1

### 获取当前日
`date.getDayOfMonth()`

#### 返回值
- 类型：**int**

### 获取当前日为今年第几天
`date.getDayOfYear()`

#### 返回值
- 类型：**int**

### 获取当前星期
`date.getDayOfWeek()`

#### 返回值
- 类型：**int**