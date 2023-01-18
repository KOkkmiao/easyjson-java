package com.easyjson.util;



import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.easyjson.gen.Generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author xpmiao
 * @date 2023/1/18
 */
public class GeneratorUtil {
    private List<Pair<String,Class>> encodeJson;

    private String fileName;
    private String filePath;

    public void add(String json,Class clazz){
        encodeJson.add(new Pair<>(json,clazz));
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public void build()  {
        try {
            Generator generator = new Generator(fileName);
            for (Pair<String, Class> classPair : encodeJson) {
                generator.add(JSON.parseObject(classPair.getKey(),classPair.getValue()));
            }
            generator.setFilePath(filePath);
            generator.run();
        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
    public static void main(String[] args) {

    }
}
