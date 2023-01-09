package com.easyjson.gen;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
    private static void init(){
        PrimitiveEncoders.put(Integer.class,"out.integer(%s);");
        PrimitiveEncoders.put(int.class,"out.integer(%s);");
        PrimitiveEncoders.put(String.class,"out.string(%s);");
        PrimitiveEncoders.put(Long.class,"out.Long(%s);");
        PrimitiveEncoders.put(long.class,"out.long(%s);");
        PrimitiveEncoders.put(Float.class,"out.Float(%s);");
        PrimitiveEncoders.put(float.class,"out.float(%s);");
        PrimitiveEncoders.put(Double.class,"out.Double(%s);");
        PrimitiveEncoders.put(double.class,"out.double(%s);");
        PrimitiveEncoders.put(BigDecimal.class,"out.bigDecimal(%s);");

    }
}
