## 站点配置文件
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
  public_file_read: false     # 站外文件读权限
  public_file_write: false    # 站外文件写权限
database_pool:                #数据库连接池配置
  test:
    url: 'mysql://localhost:3306/test'
    username: 'root'
    password: '123456'
```
### 网站类型
- `static` 静态类型，hammer不会处理任何内容
- `script` 脚本类型，以.hsp结尾的文件将被解释执行

### SSL证书
SSL证书用于部署HTTPS服务，需使用JKS格式文件，保存至hammer根目录下的ssl/目录中。

### 数据库连接池
可配置数据库链接池，在脚本中直接调用连接，具体文档请查看[数据库](database.md)。

### 修改配置文件
- hammer自动监听配置文件变化，当文件被修改时会重新读取并加载网站。
- 修改ssl证书配置后，需重启服务器。