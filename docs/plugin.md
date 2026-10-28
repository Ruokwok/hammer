## 插件
Hammer支持使用安装插件以提高HSP脚本的扩展性.

### 安装插件
- 下载插件的JAR文件，并放置在Hammer的插件目录中。通常位于Hammer安装目录下的plugins目录。
- Hammer启动时会自动加载插件。

### 查看已安装的插件
对于Linux平台上使用HMR安装的Hammer，可以通过以下命令查看已安装的插件：

`hmr plugins` 或者 `hmr -p`

此命令将打印已安装的插件和模块列表.

### 重载插件
使用`hmr reload`命令可以重载插件，对已加载的插件依次调用`onDisable()`与`onEnable()`，但不会重新读取plugins目录下的JAR文件。新增或更新插件JAR后，需重启服务器生效。
