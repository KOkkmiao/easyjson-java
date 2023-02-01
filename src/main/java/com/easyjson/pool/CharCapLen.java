package com.easyjson.pool;

import java.lang.reflect.Array;

public class CharCapLen {
    char[] value;
    volatile int capacity;
    volatile int len;
    public CharCapLen(int capacity) {
        this.value = new char[capacity];
        this.capacity = capacity;
    }
    public int free() {
        return capacity - len;
    }

    public void append(char ch) {
        this.value[this.len++] = ch;
    }
    public void append(char[] ch) {
        System.arraycopy(ch, 0, this.value, len, ch.length);
        this.len += ch.length;
    }
    public void append(char[] ch,int src,int srcLen) {
        System.arraycopy(ch, src, this.value, len,srcLen);
        this.len += srcLen;
    }
    public void append(String str) {
        str.getChars(0, str.length(), this.value, len);
        this.len += str.length();
    }
}