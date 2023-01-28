package com.easyjson.gen;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: author
 * @Description:
 * @Date: 2023/1/8 12:22 下午
 * @Version: 1.0
 */
public class Encoder {
    public static Map<Class,String> PrimitiveEncoders = new HashMap<>();
    static {
        init();
    }
    private Generator generator;
    private PrintStream out;
    public Encoder(Generator generator) {
        this.generator = generator;
        this.out = generator.out;
    }
    public void genEncoder(Class t) throws IllegalAccessException {
        if (List.class.isAssignableFrom(t) || Map.class.isAssignableFrom(t) || t
                .isArray()) {
            genSliceArrayMapEncoder(t);
        } else {
            genStructEncoder(t);
        }
    }

    public void genSliceArrayMapEncoder(Object t) {

    }

    public void genStructEncoder(Class t) throws IllegalAccessException {
        String fName = getEncoderName(t);
        String type = this.generator.getType(t);
        out.println("private static void " + fName + "(Writer out ," + type + " in){");
        out.println("  out.RawByte('{');");
        out.println("  boolean first = true;");
        List<Field> fs = getStructFields(t);
        boolean firstCondition = true;
        for (int i = 0; i < fs.size(); i++) {
            firstCondition = genStructFieldEncoder(t, fs.get(i), i == 0, firstCondition);
        }
        //        if hasUnknownsMarshaler(t) { 暂定不写
        //            if !firstCondition {
        //                fmt.Fprintln(g.out, "  in.MarshalUnknowns(out, false)")
        //            } else {
        //                fmt.Fprintln(g.out, "  in.MarshalUnknowns(out, first)")
        //            }
        //        }
        out.println("  out.RawByte('}');");
        out.println("}");
    }
    public String getEncoderName(Class t) {
        return this.generator.functionNames("endcode", t);
    }

    public List<Field> getStructFields(Class t) {
        List<Field> fields =
                Arrays.asList(t.getDeclaredFields()).stream().filter(item-> !Modifier.isStatic(item.getModifiers())).collect(
                        Collectors.toList());
        Class clazz = t;
        while ((clazz = clazz.getSuperclass()) != null) {
            Field[] declaredFields1 = clazz.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields1));
        }
        fields.forEach(field -> field.setAccessible(true));
        return fields;
    }

    public boolean genStructFieldEncoder(Class t, Field f, boolean firstFiled, boolean firstCondition) {
        String jsonName = this.generator.GetJSONFieldName(t, f);
        Generator.AnnotationInfo annotationInfo = this.generator.parseFieldAnnotation(f,t);
        boolean toggleFirstCondition = firstCondition;
        boolean noOmitEmpty = !this.generator.omitEmpty;
        if (noOmitEmpty) {
            out.println("{");
            toggleFirstCondition = false;
        } else {
            out.println("if(" + this.generator.notEmptyCheck(f, "in." + concatGetFiled(f.getName())) + "){");
        }
        if (firstCondition) {
            out.printf("String prefix = %s;\n",concatQuote(","+concatQuoteDouble(  jsonName )+ " :"));
            if (firstFiled) {
                if (this.generator.omitEmpty) {
                    out.println("   first = false");
                }
                out.println("out.RawString(prefix.substring(1));");
            } else {
                out.println("   if (first){");
                out.println("      first = false;");
                out.println("out.RawString(prefix.substring(1));");
                out.println("} else {");
                out.println("      out.RawString(prefix);");
                out.println("}");
            }
        } else {
            out.printf("String prefix = %s;\n",concatQuote( ","+concatQuoteDouble( jsonName )+ " :"));
            out.println("      out.RawString(prefix);");
        }
        genTypeEncoder(f.getType(), "in." + concatGetFiled(f.getName()), annotationInfo, 2, this.generator.omitEmpty);
        out.println("}");
        return toggleFirstCondition;
    }

    private void genTypeEncoder(Class t, String in, Generator.AnnotationInfo o, int indent, boolean assumeNonEmpty) {

        //todo 可扩展用户自定的序列化类
        genTypeEncoderNoCheck(t, in, o, indent, assumeNonEmpty);
    }

    private void genTypeEncoderNoCheck(Class t, String in, Generator.AnnotationInfo o, int indent, boolean assumeNonEmpty) {
        String ws = this.generator.addIndent(indent);
        if (PrimitiveEncoders.containsKey(t)) {
            out.printf(ws + PrimitiveEncoders.get(t) + "\n", in);
            return;
        }
        if (Collection.class.isAssignableFrom(t)) {
            String iVar = this.generator.uniqueVarName();
            if (!assumeNonEmpty) {
                out.println(ws + "if (" + in + " == null) {");
                out.println(ws + "  out.RawString(\"null\");");
                out.println(ws + "} else {");
            } else {
                out.println(ws + "{");
            }
            out.println(ws + "  out.RawByte('[');");
            out.println(ws + "  for (int "+ iVar + " = 0;" + iVar + "<" + in + ".size();" + iVar + "++){");
            out.println(ws + "    if (" + iVar + " > 0) {");
            out.println(ws + "      out.RawByte(',');");
            out.println(ws + "   }");

            genTypeEncoder(o.getlK(),
                    in + ".get(" + iVar + ")",
                    o,
                    indent + 2,
                    false);

            out.println(ws + "  }");
            out.println(ws + "  out.RawByte(']');");
            out.println(ws + "}");
            return;
        }
        if (Map.class.isAssignableFrom(t)) {
            Class key = o.getlK();
            Class value = o.getmV();

            String keyEnc = PrimitiveEncoders.get(key);

            String tmpVar = this.generator.uniqueVarName();

            if (!assumeNonEmpty) {
                out.println(ws + "if(" + in + " == null) {");
                out.println(ws + "  out.RawString(\"null\");");
                out.println(ws + "} else {");
            } else {
                out.println(ws + "{");
            }
            out.println(ws + "  out.RawByte('{');");
            out.println(ws + in + ".forEach((" + tmpVar + "Name," + tmpVar + "Value)-> {");


            if (keyEnc != null && keyEnc.length() > 0) {
                out.println(ws + "    " + String.format(keyEnc, tmpVar + "Name"));
            } else {
                genTypeEncoder(key, tmpVar + "Name", o, indent + 2, false);
            }

            out.println(ws + "    out.RawByte(':');");

            genTypeEncoder(value, tmpVar + "Value", o, indent + 2, false);
            out.println(ws+"out.RawByte(',');");
            out.println(ws + " });");
            // 截取最后一个字符
            out.println(ws+"out.subLastDot();");
            out.println(ws + "  }");
            out.println(ws + "  out.RawByte('}');");
            return;
        }
        if (t.isArray()) {
            Class lk = o.getlK();
            String iVar = this.generator.uniqueVarName();
            out.println(ws + "out.RawByte('[')");
            out.println(ws + "  for (;" + iVar + "<" + in + ".length;" + iVar + "++;){");
            out.println(ws + "    if (" + iVar + " > 0) {");
            out.println(ws + "      out.RawByte(',');");
            out.println(ws + "   }");
            genTypeEncoder(lk,
                    "(" + in + ")[" + iVar + "]",
                    o,
                    indent + 2,
                    false);
            out.println(ws + "}");
            out.println(ws + "out.RawByte(']');");
            return;
        }
        if (Date.class.isAssignableFrom(t)) {
            out.println(ws+"out.string(StdDateFormat.instance.format("+in+"));");
            return ;
        }
        if (Calendar.class.isAssignableFrom(t)) {
            out.println(ws+"out.string(StdDateFormat.instance.format("+in+".getTime()));");
            return;
        }
        if (t.isEnum()) {
            out.println(ws+"out.string("+in+".value());");
            return;
        }
        if (Object.class.isAssignableFrom(t)) {
            String encName = getEncoderName(t);
            this.generator.addType(t);
            out.println(ws + encName + "(out," + in + ");");
            return;
        }
        throw new IllegalArgumentException(String.format("%s", t.getName()));
    }


    private String concatGetFiled(String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1) + "()";
    }

    public String concatQuote(String str) {
        return "\"" + str + "\"";
    }
    public String concatQuoteDouble(String str) {
        return "\\\"" + str + "\\\"";
    }

    private static void init(){
        PrimitiveEncoders.put(Integer.class,"out.integer(%s);");
        PrimitiveEncoders.put(int.class,"out.integer(%s);");
        PrimitiveEncoders.put(String.class,"out.string(%s);");
        PrimitiveEncoders.put(Long.class,"out.Long(%s);");
        PrimitiveEncoders.put(long.class,"out.Long(%s);");
        PrimitiveEncoders.put(Float.class,"out.Float(%s);");
        PrimitiveEncoders.put(float.class,"out.float(%s);");
        PrimitiveEncoders.put(Double.class,"out.Double(%s);");
        PrimitiveEncoders.put(double.class,"out.double(%s);");
        PrimitiveEncoders.put(BigDecimal.class,"out.bigDecimal(%s);");
        PrimitiveEncoders.put(boolean.class,"out.bool(%s);");
        PrimitiveEncoders.put(Boolean.class,"out.Bool(%s);");
        PrimitiveEncoders.put(byte.class,"out.writebyte(%s);");
        PrimitiveEncoders.put(Byte.class,"out.writeByte(%s);");
        PrimitiveEncoders.put(short.class,"out.writeShort(%s);");

    }
}
