package com.easyjson.pool;

import com.fasterxml.jackson.core.io.NumberOutput;

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
    public void appendInteger(Integer target) {
        ensureSpace(11);
        this.value.len = NumberOutput.outputInt(target, this.value.value, this.value.len);
    }
    public void appendLong(Long target) {
        ensureSpace(21);
        this.value.len = NumberOutput.outputLong(target, this.value.value, this.value.len);
    }
    public void deleteLast() {
        this.value.len -= 1;
    }
    public void appendShort(Short target) {
        ensureSpace(6);
        this.value.len = NumberOutput.outputInt(target, this.value.value, this.value.len);
    }

    private void put(CharCapLen builder) {
        if (builder.capacity < poolSize) {
            return;
        }
        builder.len = 0;
        queue.putIfAbsent(builder.capacity, builder);
    }

    private CharCapLen get(Integer capacity) {
        if (capacity >= poolSize) {
            CharCapLen chars = queue.remove(capacity);
            if (chars != null) {
                return chars;
            }
        }
        if (capacity == Integer.MAX_VALUE) {
            capacity = poolMaxSize;
        }
        return new CharCapLen(capacity);
    }

    private volatile CharCapLen value;

    List<CharCapLen> collect = new ArrayList<>();


    public PoolStringBuilder(int capacity) {
        this.value = new CharCapLen(capacity);
    }

    public PoolStringBuilder() {
        this.value = new CharCapLen(128);
    }

    public void ensureSpace(int s) {
        if (this.value.free() < s) {
            ensureSpaceSlow();
        }
    }

    private void ensureSpaceSlow() {
        //保存到collect中
        this.collect.add(value);
        int newCapacity = this.value.capacity * 2;
        this.value = get(newCapacity);
    }

    public void appendChar(char ch) {
        ensureSpace(1);
        value.append(ch);
    }

    public void appendChar(char[] ch) {
        if (ch.length <= this.value.free()) {
            value.append(ch);
        } else {
            appendBytesSlow(ch);
        }

    }
    public void appendString(String str) {
        if (str.length() <= this.value.free()) {
            value.append(str);
        } else {
            appendBytesSlow(str.toCharArray());
        }
    }
    public void appendBytesSlow(char[] ch) {
        for ( int writeCount = 0, length = ch.length; length > 0; length -= writeCount) {
            ensureSpace(1);
            int fori = value.free();

            if (fori > length) {
                fori = length;
            }
            value.append(ch,writeCount,fori);
            writeCount +=fori;
        }
    }
    public int size() {
        int len = this.value.len;
        for (CharCapLen charCapLen : collect) {
            len += charCapLen.len;
        }
        return len;
    }

    public char[] buildChar() {
        if (collect.isEmpty()) {
            return this.value.value;
        }
        int size = size();
        char[] result = new char[size];
        int resultCount = 0;
        for (CharCapLen charCapLen : collect) {
            System.arraycopy(charCapLen.value, 0, result, resultCount, charCapLen.len);
            resultCount += charCapLen.len;
            put(charCapLen);
        }
        System.arraycopy(this.value.value, 0, result, resultCount, this.value.len);
        resultCount += this.value.len;
        put(this.value);
        return result;
    }
}

