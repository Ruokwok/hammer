## 客户端请求
内置`Request`对象包含浏览器请求相关的API

- [获取请求方法](#获取请求方法)
- [获取客户端地址](#获取客户端地址)
- [获取客户端端口](#获取客户端端口)
- [获取请求路径](#获取请求路径)
- [获取请求协议](#获取请求协议)
- [获取请求域名](#获取请求域名)
- [获取请求头数据](#获取请求头数据)
- [获取请全部求头](#获取全部请求头)
- [获取浏览器](#获取浏览器)
- [获取浏览器版本](#获取浏览器版本)
- [获取操作系统](#获取操作系统)
- [是否为移动设备](#是否为移动设备)
- [获取浏览器内核](#获取浏览器内核)
- [获取浏览器内核版本](#获取浏览器内核版本)
- 有关GET与POST请求参数请查看[请求参数](param.md)

### 获取请求方法
`Request.getMethod()`

#### 返回值
- 类型：**String**
- 请求方法( GET/POST )

### 获取客户端地址
`Request.getAddress()`

#### 返回值
- 类型：**String**
- 发起请求的客户端的IP地址

### 获取客户端端口
`Request.getPort()`

#### 返回值
- 类型：**int**

### 获取请求路径
`Request.getPath()`

#### 返回值
- 类型：**String**

### 获取请求域名
`Request.getDomain()`

#### 返回值
- 类型：**String**

### 获取请求协议
`Request.getProtocol()`

#### 返回值
- 类型：**String**
- 通常为HTTP/1.1

### 获取请求头数据
`Request.getHeader(key)`

#### 参数
| 名称  | 说明  | 类型     | 必填  |
|-----|-----|--------|-----|
| key | 键名  | String | 是   |

#### 返回值
- 类型: **String**

### 获取全部请求头
`Request.getHeaders()`

#### 返回值
- 类型: **Json<String, String>**
- 请求头的全部数据，键值对格式。

### 获取浏览器
`Request.getBrowser()`

#### 返回值
- 类型: **String**
- 浏览器名称

### 获取浏览器版本
`Request.getBrowserVersion()`

#### 返回值
- 类型: **String**
- 浏览器版本号

### 获取操作系统
`Request.getOS()`

#### 返回值
- 类型: **String**
- 操作系统名称

### 是否为移动设备
`Request.isMobile()`

#### 返回值
- 类型: boolean
- 移动端浏览器时为true, 桌面端为false

### 获取浏览器内核
`Request.getEngine()`

#### 返回值
- 类型: String
- 浏览器内核名称

### 获取浏览器内核版本
`Request.getEngineVersion()`

#### 返回值
- 类型: String
- 浏览器内核版本号