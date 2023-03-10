package com.easyjson.gen;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import com.easyjson.util.ClassDecompiler;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @Author: author
 * @Description:
 * @Date: 2023/1/8 10:49 上午
 * @Version: 1.0
 */
public class Generator {
    private String fileName;

    private String filePath;
    PrintStream out;

    private String pkgName;
    private String hashString;

    int varCounter;

    boolean noStdMarshalers;
    boolean omitEmpty;
    boolean disallowUnknownFields;
    FieldNamer fieldNamer = new DefaultFieldNamer();
    boolean simpleBytes;
    boolean skipMemberNameUnescaping;

    // package path to local alias map for tracking imports
    Map<String, String> imports = new HashMap<>();

    // types that marshalers were requested for by user
    Map<Class, Boolean> marshalers = new HashMap<>();

    // types that encoders were already generated for
    Map<Class, Boolean> typesSeen = new HashMap<>();

    // types that encoders were requested for (e.g. by encoders of other types)
    LinkedList<Class> typesUnseen = new LinkedList<>();

    Map<Class,Boolean> typeUnseenDuplicate  = new HashMap<>();
    // function name to relevant type maps to track names of de-/encoders in
    // case of a name clash or unnamed structs
    Map<String, Class> functionNames = new HashMap<>();

    Encoder encoder;
    public Generator(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
        HashCode hashCode = HashCode.fromLong(fileName.hashCode());
        this.hashString = hashCode.toString();
    }

    public void run() throws IOException, IllegalAccessException, FormatterException {
        if (Strings.isNullOrEmpty(filePath)) {
            URL resource = typesUnseen.getFirst().getClass().getResource("./");
            this.filePath = resource.getPath();
        }
        this.out = new PrintStream(filePath + File.separator +fileName);
        this.encoder = new Encoder(this);
        while (!typesUnseen.isEmpty()) {
            Class targetClass = typesUnseen.poll();
            typesSeen.put(targetClass, true);
            //            genDecoder(targetClass);
            encoder.genEncoder(targetClass);
            if (!this.marshalers.containsKey(targetClass)) {
                continue;
            }
            genStructMarshaler(targetClass);
            //            genStructUnmarshaler(targetClass);
        }
        Pair<PrintStream, Long> printStreamLongPair = printHeader();
        PrintStream printStream = printStreamLongPair.getKey();
        out.flush();
        out.close();
        IoUtil.copy(Files.newInputStream(Paths.get(filePath + File.separator +fileName)), printStream);
        printStream.println("}");
        printStream.flush();
        printStream.close();
        String read = IoUtil.read(Files.newInputStream(Paths.get("tmp-" + fileName + printStreamLongPair.getValue())),
                "utf-8");
        String formattedSource = new Formatter().formatSource(read);
        OutputStream outputStream = Files.newOutputStream(Paths.get(filePath + File.separator + fileName + ".java"));
        outputStream.write(formattedSource.getBytes());
        outputStream.flush();
        outputStream.close();
        new File(filePath + File.separator +fileName).delete();
        new File("tmp-" + fileName + printStreamLongPair.getValue()).delete();
    }
    public String GetJSONFieldName(Class t, Field f) {
        return this.fieldNamer.GetJSONFieldName(t,f);
    }

    private Pair<PrintStream, Long> printHeader() throws FileNotFoundException {
        HashCode hashCode = HashCode.fromLong(new Random().nextLong());
        PrintStream printStream = new PrintStream("tmp-" + fileName + hashCode.asLong());
        printStream.println("// Code generated by easyjson for marshaling/unmarshaling. DO NOT EDIT.");
        printStream.println();
        printStream.println("package " + pkgName + ";");
        printStream.println();
        printStream.println("import com.easyjson.jwriter.Writer;");
        printStream.println("import com.fasterxml.jackson.databind.util.StdDateFormat;");
        printStream.println("import java.util.Objects;");
        imports.forEach((k, v) -> printStream.println("import " + v + ";"));
        printStream.println();
        //print class info
        printStream.println("public class " + fileName + "{");

        return new Pair<>(printStream, hashCode.asLong());
    }

    public void genDecoder(Class t) {
        if (List.class.isAssignableFrom(t) || Map.class.isAssignableFrom(t) || t.isArray()) {
            genSliceArrayDecoder(t);
        } else {
            genStructDecoder(t);
        }
    }

    public void genSliceArrayDecoder(Class t) {

    }

    public void genStructDecoder(Class t) {

    }


    public AnnotationInfo parseFieldAnnotation(Field f, Class o) {

        AnnotationInfo annotationInfo = new AnnotationInfo();
        if (Collection.class.isAssignableFrom(f.getType())) {
            annotationInfo.setlK(ClassDecompiler.readClassCollection(f.getName(),o));
        } else if (Map.class.isAssignableFrom(f.getType())) {
            Pair<Class, Class> classClassPair = ClassDecompiler.readClassMap(f.getName(), o);
            annotationInfo.setlK(classClassPair.getKey());
            annotationInfo.setmV(classClassPair.getValue());
        } else if (f.getType().isArray()) {
            annotationInfo.setArray(true);
            annotationInfo.setlK(ClassDecompiler.readClassArray(f.getName(),o));
        } else {
            annotationInfo.setNormal(f.getType());
        }

        return annotationInfo;
    }

    public void add(Class t) {
        addType(t);
        marshalers.put(t, true);
    }

    public void addType(Class t) {
        if (typesSeen.containsKey(t)) {
            return;
        }
        if (typeUnseenDuplicate.containsKey(t)) {
            return;
        }
        typesUnseen.addLast(t);
        typeUnseenDuplicate.put(t,true);
    }

    public String uniqueVarName() {
        varCounter++;
        return "v" + varCounter;
    }

    public String notEmptyCheck(Field f, String v) {
        String template = "Objects.nonNull(" + v + ") && ";
        if (String.class.isAssignableFrom(f.getClass())) {
            return template + v + ".length() > 0";
        }
        if (Number.class.isAssignableFrom(f.getClass())) {
            return template;
        }
        if (Collection.class.isAssignableFrom(f.getClass())) {
            return template + v + ".isEmpty()";
        }
        if (Map.class.isAssignableFrom(f.getClass())) {
            return template + v + ".isEmpty()";
        }
        return template.substring(0, template.length() - 3);
    }

    public String addIndent(int indent) {
        StringBuilder builder = new StringBuilder("  ");
        for (int i = 0; i < indent; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }


    public String getEncoderName(Class t) {
        return functionNames("endcode", t);
    }

    public String getType(Class t) {
        Package aPackage = t.getPackage();
        String pkgName = aPackage.getName();
        if (this.pkgName != pkgName) {
            imports.putIfAbsent(t.getCanonicalName(), t.getCanonicalName());
        }
        return t.getSimpleName();
    }

    /*********split**************/
    public void genStructMarshaler(Class t) {
        String fname = getEncoderName(t);
        String typ = getType(t);
        if (!noStdMarshalers) {
            out.println("public static String MarshalJSON(" + typ + " v) {");
            out.println("  Writer w = new Writer();");
            out.println("  " + fname + "(w, v);");
            out.println("  return w.toJson();");
            out.println("}");
        }
        out.println("public static void MarshalEasyJSON(Writer w," + typ + " v) {");
        out.println("  " + fname + "(w, v);");
        out.println("}");
    }

    public void genStructUnmarshaler(Class t) {

    }

    public String functionNames(String prefix, Class t) {
        prefix = joinFunctionNameParts(true, "easyjson", hashString, prefix);
        String name = joinFunctionNameParts(true, prefix, getPkgName(t));
        if (!functionNames.containsKey(name)) {
            functionNames.put(name, t);
            return name;
        }
        String finalPrefix = prefix;
        if (functionNames.entrySet()
                .stream()
                .anyMatch(item -> item.getValue() == t && item.getKey().startsWith(finalPrefix))) {
            return name;
        }
        String tmp;
        for (int i = 0; ; i++) {
            tmp = name + i;
            if (functionNames.containsKey(tmp)) {
                continue;
            }
            functionNames.put(tmp, t);
            return tmp;
        }
    }

    // 包名称 + 路径名最后一个
    public String getPkgName(Class t) {
        return t.getName().replaceAll("\\.","");
    }

    public String joinFunctionNameParts(boolean keepFirst, String... parts) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && keepFirst) {
                builder.append(parts[i]);
            } else {
                if (parts[i].length() > 0) {
                    builder.append(parts[i].substring(0, 1).toUpperCase());
                }
                if (parts[i].length() > 0) {
                    builder.append(parts[i].substring(1));
                }
            }
        }

        return builder.toString();
    }
    public String getPkgName() {
        return pkgName;
    }
    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    interface FieldNamer {
        String GetJSONFieldName(Class t, Field f);
    }


    class DefaultFieldNamer implements FieldNamer {

        @Override
        public String GetJSONFieldName(Class t, Field f) {
            JsonProperty annotation = f.getAnnotation(JsonProperty.class);
            if (annotation == null) {
                return f.getName();
            }
            return annotation.value();
        }
    }
    public void setNoStdMarshalers(boolean noStdMarshalers) {
        this.noStdMarshalers = noStdMarshalers;
    }
    public void setOmitEmpty(boolean omitEmpty) {
        this.omitEmpty = omitEmpty;
    }
    public void setDisallowUnknownFields(boolean disallowUnknownFields) {
        this.disallowUnknownFields = disallowUnknownFields;
    }
    public void setFieldNamer(FieldNamer fieldNamer) {
        this.fieldNamer = fieldNamer;
    }
    class AnnotationInfo {
        private Class normal;
        private Class mV;

        private Class lK;

        private boolean isArray;
        public Class getNormal() {
            return normal;
        }
        public void setNormal(Class normal) {
            this.normal = normal;
        }
        public Class getmV() {
            return mV;
        }
        public void setmV(Class mV) {
            this.mV = mV;
        }
        public Class getlK() {
            return lK;
        }
        public void setlK(Class lK) {
            this.lK = lK;
        }
        public boolean isArray() {
            return isArray;
        }
        public void setArray(boolean array) {
            isArray = array;
        }

    }
    public static void main(String[] args) throws Exception {
        // Generator listSearchJson = new Generator("ListSearchJson");
        // listSearchJson.add(ListSearchResponseType.class);
        // listSearchJson.setPkgName("com.ctrip.flight.dom.engine.coresearch.service");
        // listSearchJson.setFilePath("D:\\Users\\xpmiao\\IdeaProjects\\flightcoresearch\\coresearch\\coresearch-service"
        //         + "\\src\\main\\java\\com\\ctrip\\flight\\dom\\engine\\coresearch\\service");
        // listSearchJson.run();
    }
}
