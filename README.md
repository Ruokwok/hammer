# Hammer
## 简介
Hammer是一款Java实现的基于Jetty的Web服务器，通过Graal.js引擎支持在页面中嵌入JavaScript脚本。

此项目仍处于早期开发阶段，API频繁改动，并且可能存在大量未知错误，请勿用于生产环境。
## 启动
可从[Release](https://github.com/Ruokwok/hammer/releases)下载最新版本可执行jar文件，并执行以下命令启动服务器。

`java -jar hammer.jar`
- JRE版本需 >= 17

在使用GraalVM时，需安装js模块，命令如下:

`gu install js`

## 安装
在Linux系统上，可以安装Hammer Manager管理服务，一键安装命令如下:
```shell
sudo curl -O https://hmr.starelement.net/install.sh && chmod 755 install.sh && sudo bash install.sh 
```
- 确保拥有执行权限

### Hammer Manager
安装后可以使用`hmr`命令管理服务器, 使用`hmr help`查看帮助。
```test
root@ubuntu:~# hmr

Usage: hmr [command]

Commands:
  start         Start the hammer in background
  run           Start the hammer in this session
  stop          Stop the hammer
  restart       Restart the hammer
  -s,status     Print hammer status
  log           Print logs
  -v,version    Print version information
  -w,sites      Print websites list
  -p,plugins    Print installed plugins list
  reload        Reload websites and plugins

root@ubuntu:~#
```

## 目录
### 使用
- [站点配置文件](docs/config.md)
- [HSP脚本](docs/script.md)
### API
- [内置函数](docs/function.md)
- [客户端请求](docs/request.md)
- [请求参数](docs/param.md)
- [COOKIE](docs/cookie.md)
- [SESSION](docs/session.md)
- [文件](docs/file.md)
- [数据库](docs/database.md)
- [日期与时间](docs/date.md)
- [数据摘要算法](docs/digester.md)
- [编解码器](docs/codec.md)
### 插件
- [插件](docs/plugin.md)
- [脚本模块](docs/module.md)
- [插件开发](docs/plugin_dev.md)
- [注册模块](docs/module_dev.md)