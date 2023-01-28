package com.easyjson.util;

import cn.hutool.core.lang.Pair;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.renamer.PoolInterceptor;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.attr.StructGenericSignatureAttribute;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.VBStyleCollection;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @author xpmiao
 * @date 2023/1/28
 */
public class ClassDecompiler {
    static {
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        DecompilerContext.initContext(objectObjectHashMap);
        PoolInterceptor interceptor = new PoolInterceptor(null);
        DecompilerContext.setPoolInterceptor(interceptor);
        DecompilerContext.setCounterContainer(new CounterContainer());
    }
    private static Map<Class,StructClass> CACHE = new HashMap<>();
    public static byte[] entryLoad(JarFile archive, JarEntry entry) {
        try {
            return InterpreterUtil.getBytes(archive, entry);
        } catch (IOException e) {

        }
        return null;
    }
    public static byte[] classLoad(String file) {
        try {
            return InterpreterUtil.getBytes(new File(file));
        } catch (IOException e) {

        }
        return null;
    }
    public static StructClass build(String jarFile, String entry) throws IOException {
        JarFile jarFile1 = new JarFile(jarFile);
        return new StructClass(entryLoad(jarFile1, jarFile1.getJarEntry(entry)), true, null);
    }
    public static StructClass build(String file) throws IOException {
        return new StructClass(classLoad(file), true, null);
    }

    public static Class readClassCollection(String field, Class t) {
        try {
            StructClass structClass = fileParse(field, t);
            VBStyleCollection<StructField, String> fields = structClass.getFields();
            StructField structField = fields.stream().filter(item -> item.getName().equals(field)).findFirst().get();
            StructGenericSignatureAttribute structGeneralAttribute =
                    (StructGenericSignatureAttribute) structField.getAttributes().get(0);
            //Ljava/util/LinkedList<Ljava/lang/Class;>;
            //Ljava/util/Map<Ljava/lang/Class;Ljava/lang/Boolean;>;
            //[Ljava/lang/String;
            String signature = structGeneralAttribute.getSignature();
            String substring = signature.substring(signature.indexOf("<") + 1, signature.indexOf(">"));
            String className = substring.substring(1, substring.length() - 1).replaceAll("/", "\\.");
            return Class.forName(className);
        } catch (Exception e) {

        }
        return null;
    }
    public static StructClass fileParse(String field, Class t) throws Exception {
        StructClass structClass = CACHE.get(t);
        if (structClass != null){
            return structClass;
        }
        URL resource = t.getResource("");
        String path = resource.getPath();

        boolean jar = resource.getProtocol().equals("jar");
        StructClass build = null;
        try {
            if (jar) {
                String jarPath = path.substring(6, path.indexOf("jar") + 3);
                String entryName = t.getPackage().getName() + "/" + t.getSimpleName();
                entryName = entryName.replaceAll("\\.", "/") + ".class";
                build = build(jarPath, entryName);

            } else {
                String file = path.substring(1) + t.getSimpleName() + ".class";
                build = build(file);
            }
        } catch (IOException e) {
        }
        if (build == null) {
            throw new Exception("error " + t.getName() + "." + t.getSimpleName());
        }
        CACHE.put(t,build);
        return build;
    }
    public static Pair<Class, Class> readClassMap(String field, Class t) {

        try {
            StructClass structClass  = fileParse(field, t);
            VBStyleCollection<StructField, String> fields = structClass.getFields();
            StructField structField = fields.stream().filter(item -> item.getName().equals(field)).findFirst().get();
            StructGenericSignatureAttribute structGeneralAttribute =
                    (StructGenericSignatureAttribute) structField.getAttributes().get(0);
            //Ljava/util/Map<Ljava/lang/Class;Ljava/lang/Boolean;>;
            String signature = structGeneralAttribute.getSignature();
            String substring = signature.substring(signature.indexOf("<") + 1, signature.indexOf(">"));
            String[] split = substring.split(";");
            String key = split[0].substring(1).replaceAll("/","\\.");
            String value = split[1].substring(1).replaceAll("/","\\.");
            return new Pair(Class.forName(key),Class.forName(value));
        } catch (Exception e) {

        }
        return null;
    }
    public static Class readClassArray(String field, Class t) {
        try {
            StructClass structClass  = fileParse(field, t);
            //[Ljava/lang/String;
            VBStyleCollection<StructField, String> fields = structClass.getFields();
            StructField structField = fields.stream().filter(item -> item.getName().equals(field)).findFirst().get();
            String descriptor = structField.getDescriptor();
            String className = descriptor.substring(1, descriptor.length() - 1).replaceAll("/", "\\.");
            return Class.forName(className);
        } catch (Exception e) {

        }
        return null;
    }
}
