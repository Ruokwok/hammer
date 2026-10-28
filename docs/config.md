## 站点配置文件
站点配置文件位于hammer根目录下的`config/`目录中，每个YAML文件对应一个网站，文件修改后会自动热加载。

```yml
name: SiteName      # 网站名称
path: wwwroot/www   # 网站根目录，不存在时会自动创建
type: script        # 网站类型(static/script)
domain:             # 网站域名，支持多个
  - 'www.example.com'
  - 'example.com'
ssl_keystore: 'keystore.jks'  #ssl证书文件
ssl_password: 'password'      #ssl证书密码
permission:                   # 网站权限(static类型可省略)
  file_read: true             # 站内文件读权限
  file_write: true            # 站内文件写权限
  public_file_read: true      # 站外文件读权限，默认为true
  public_file_write: false    # 站外文件写权限
database_pool:                #数据库连接池配置
  test:
    url: 'mysql://localhost:3306/test'
    username: 'root'
    password: '123456'
protects:                     #访问保护规则，命中规则的路径将返回403
  - '/admin'
  - '/config.yml'
pseudo_static:                #伪静态规则
  - '/list/{1} -> /list.jsp?page={1}'
error_page:                   #自定义错误页面，路径相对于网站根目录
  error_404: 'errors/404.html'
  error_500: 'errors/500.html'
tasks:                        #定时任务，仅script类型有效
  backup:
    cron: '0 0 3 * * ?'       #cron表达式
    script: '/task/backup.hsp' #要执行的脚本，相对于网站根目录
values:                       #自定义配置，可在脚本中通过Values模块读取
  site_title: 'My Website'
```
### 网站类型
- `static` 静态类型，hammer不会处理任何内容
- `script` 脚本类型，以`.hsp`、`.hs`、`.hspx`结尾的文件将被解释执行（可通过`server.yml`中的`scriptFileTypes`自定义脚本扩展名）

### SSL证书
SSL证书用于部署HTTPS服务，需使用JKS格式文件，保存至hammer根目录下的ssl/目录中。

### 数据库连接池
可配置数据库链接池，在脚本中直接调用连接，具体文档请查看[数据库](database.md)。

### 访问保护
`protects`用于禁止访问指定路径，支持精确匹配与前缀匹配（前缀匹配时需以`/`或字符串结尾），命中规则的请求将返回403。

### 伪静态
`pseudo_static`用于将请求路径重写为站内其他路径，格式为`原路径 -> 目标路径`，原路径中可使用`{1}`、`{2}`等占位符捕获部分内容并在目标路径中引用。

### 定时任务
`tasks`仅在`script`类型的网站中生效，`cron`为cron表达式，`script`为网站根目录下的脚本路径，到达指定时间时将在后台执行该脚本。

### 自定义配置
`values`中的键值对可在脚本中通过`module("Values")`读取，具体请查看[内置模块](module.md)。

### 修改配置文件
- hammer自动监听`config/`目录中配置文件的变化，当文件被修改时会重新读取并加载网站。
- 修改ssl证书配置后，需重启服务器。
