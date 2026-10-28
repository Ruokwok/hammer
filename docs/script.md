## HSP脚本
HSP (Hammer Script Page)是hammer中可执行的JavaScript脚本类型文件，文件扩展名默认为`.hsp`、`.hs`、`.hspx`，在script类型的网站中会解释执行。可通过`server.yml`中的`scriptFileTypes`自定义脚本扩展名。

### HSP可以做什么
- 动态生成页面内容
- 读取和修改服务器上的文件
- 发送和接收Cookie
- 连接并操作数据库
- 调用Java对象

### 语法
HSP脚本在服务器上执行，并将文本结果发送给浏览器，与PHP类似，HSP可以嵌入到HTML页面的任意位置。
扩展名为`.hsp`或`.hs`的文件将被解释执行。

HSP脚本完全遵循JavaScript标准语法，hammer提供了一些相关的API。

HSP脚本以`<?hsp ?>`标签包裹，以下是一个简单的示例

```html
<!DOCTYPE html> 
<html> 
<body> 

<?hsp 
print("Hello World!"); 
?> 

</body> 
</html>
```
- 当文件内只包含hsp脚本，或hsp脚本位于文本末尾时，可以省略结尾的`?>`。
- `<?hsp ?>`标签以外的内容将被原样输出。
- 当请求一个目录且未指定文件名时，hammer将按`index.hs`、`index.hsp`、`index.html`的顺序依次查找默认首页。

### 纯脚本文件
扩展名为`.hspx`的文件将被作为纯JavaScript脚本解释执行，无需使用`<?hsp ?>`标签包裹，文件中的所有内容均视为脚本代码。

```javascript
print("Hello World!");
```
