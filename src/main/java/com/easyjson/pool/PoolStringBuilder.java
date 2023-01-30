package com.easyjson.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xpmiao
 * @date 2023/1/29
 */
public class PoolStringBuilder {

    private static ConcurrentHashMap<Integer, CharCapLen> queue = new ConcurrentHashMap<>();

    static {
        init();
    }
    private static int poolSize = 512;
    private static int poolMaxSize = 32768;
    public static void init() {
        for (int i = poolSize; i < poolMaxSize; i *= 2) {
            queue.put(i, new CharCapLen(i));
        }
    }

    private void put(CharCapLen builder){
        if (builder.capacity < poolSize) {
            return;
        }
        queue.putIfAbsent(builder.capacity,builder);
    }

    private CharCapLen get(Integer capacity){
        if (capacity >= poolSize) {
            CharCapLen chars = queue.get(capacity);
            if (chars!=null) {
                return chars;
            }
        }
        return new CharCapLen(capacity);
    }

    CharCapLen value;

    int count;

    int capacity = 128;

    List<CharCapLen> collect = new ArrayList<>();


    public PoolStringBuilder(int capacity) {
        this.value = new CharCapLen(capacity);
        this.capacity = capacity;
    }

    public PoolStringBuilder() {
        this.value =  new CharCapLen(128);
    }

    public void ensureSpace(int s) {
        if (this.value.free() < s) {
            ensureSpaceSlow();
        }
    }

    private void ensureSpaceSlow(){
        //保存到collect中
        this.collect.add(value);
        int newCapacity = this.value.capacity * 2;
        this.value = get(newCapacity);
    }

    public void appendChar(char ch){
        ensureSpace(1);
        value.append(ch);
    }

    public void appendChar(char[] ch){
        if (ch.length <=this.value.free()) {
            value.append(ch);
        }else {
            appendBytesSlow(ch);
        }

    }
    public void appendString(String str){
        if (str.length()<=this.value.free()) {
            value.append(str);
        }else {
            appendBytesSlow(str);
        }
    }
    public void appendBytesSlow(char[] ch){
        int length = ch.length;
        int writeCount = 0;
        for (;length > 0;) {
            ensureSpace(1);
            int fori  = value.free();

            if (fori > length) {
                fori = length;
            }
            for (int i = 0; i< fori;i++){
                appendChar(ch[writeCount++]);
            }
            length = length - writeCount;
        }
    }
    public void appendBytesSlow(String ch){
        int length = ch.length();
        int writeCount = 0;
        for (;length > 0;) {
            ensureSpace(1);
            int fori  = value.free();

            if (fori > length) {
                fori = length;
            }
            for (int i = 0; i< fori;i++){
                appendChar(ch.charAt(writeCount++));
            }
            length = length - writeCount;
        }
    }
    public int size(){
        int len = this.value.len;
        for (CharCapLen charCapLen : collect) {
            len += charCapLen.len;
        }
        return len;
    }

    public char[] buildChar(){
        if (collect.isEmpty()){
            return this.value.value;
        }
        int size = size();
        char[] result = new char[size];
        int resultCount = 0;
        for (CharCapLen charCapLen : collect) {
            for (int i = 0; i < charCapLen.value.length; i++) {
                result[resultCount++] = charCapLen.value[i];
            }
            put(charCapLen);
        }
        return result;
    }
}
