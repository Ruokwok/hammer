## 插件开发

此文档描述了如何编写简单的Hammer插件示例。

### Maven依赖
在`pom.xml`中添加如下依赖
```xml
<dependency>
    <groupId>cc.ruok.hammer</groupId>
    <artifactId>hammer</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
### 插件开发

Hammer插件最低支持JDK17。

#### 清单文件
创建`src/resources/plugin.yml`文件，内容如下
```yaml
name: PluginName      # 插件名称
author: AuthorName    # 插件作者
version: 1.0.0        # 插件版本
main: com.example.MyPlugin  # 插件主类
```

#### 主类
- 创建插件主类，需继`cc.ruok.hammer.plugin.HammerPlugin`类。
- 实现其抽象方法

示例代码
```java
package com.example;

import cc.ruok.hammer.plugin.HammerPlugin;
import cc.ruok.hammer.Logger;

public class MyPlugin extends HammerPlugin {
    
    @Override
    public void onLoad() {
        Logger.info("插件加载");
    }
    
    @Override
    public void onEnable() {
        Logger.info("插件启用");
    }
    
    @Override
    public void onDisable() {
        Logger.info("插件禁用");
    }
    
}
```
- **onLoad()** 方法在插件加载时被调用，可用于加载资源文件，初始化变量等。在Hammer实例生命周期中只会被调用一次.
- **onEnable()** 方法在onLoad()之后被调用，可用于注册模块，加载配置文件等。在每次插件启用时都会被调用。
- **onDisable()** 方法在插件禁用时被调用，可用于卸载模块，保存配置文件等。