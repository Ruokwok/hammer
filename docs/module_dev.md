## 注册模块

可在插件中注册模块，以供HSP脚本中使用Java对象。

### 注册模块示例

可通过静态`cc.ruok.hammer.plugin.PluginManager`类的静态方法注册模块。

```java
import cc.ruok.hammer.plugin.PluginManager;

...

    String name = "MyModule";
    MyModule module = new MyModule();
    PluginManager.registerModule(name, module);

...
```

#### 参数
| 参数名    | 类型           | 描述   |
|--------|--------------|------|
| name   | String       | 模块名称 |
| module | Object/Class | 模块   |

#### 模块参数
- 当传入的参数为对象时，在脚本中调用将会返回该对象的引用，多此次调用将返回同一个实例。
- 当传入的参数为类时，在脚本中调用将会返回该类的实例，每次调用都会实例化此类，多此调用将返回不同的实例。

### 示例代码
com.example.MyPlugin.java
```java
package com.example;

import cc.ruok.hammer.plugin.HammerPlugin;
import cc.ruok.hammer.plugin.PluginManager;
import cc.ruok.hammer.Logger;

public class MyPlugin extends HammerPlugin {
    
    @Override
    public void onEnable() {
        
        // 使用对象注册
        PluginManager.registerModule("MyModule", new MyModule());
        
        // 使用类注册
        // PluginManager.registerModule("MyModule", MyModule.class);
    }
    
}
```

com.exxample.MyModule.java
```java
package com.example;

public class MyModule {
    
    public void print(String msg) {
        Logger.info(msg);
    }
    
}
```

example.hsp
```js
<?hsp

    var myModule = module("MyModule");
    myModule.print("Hello, World!");

?>
```

- 执行`example.hsp`脚本，控制台将输出`Hello, World!`。

### 总结
当模块被注册后，HSP 脚本可以通过模块名称访问对应的 Java 对象或类。

具体行为取决于传入的 module 参数类型： 
- 如果是对象，脚本中调用时直接返回该对象。 
- 如果是类，脚本中调用时会实例化该类并返回新对象。