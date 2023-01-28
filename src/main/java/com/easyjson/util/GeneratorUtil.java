package com.easyjson.util;



import cn.hutool.core.lang.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xpmiao
 * @date 2023/1/18
 */
public class GeneratorUtil {
    private List<Pair<String,Class>> encodeJson = new ArrayList();

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

    }
    public static void main(String[] args) {

    }
}
