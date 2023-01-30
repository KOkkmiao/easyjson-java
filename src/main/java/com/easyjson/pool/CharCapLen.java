package com.easyjson.pool;

public class CharCapLen {
    char[] value;
    int capacity;
    int len;
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
        for (char c : ch) {
            this.value[this.len++] = c;
        }
    }
    public void append(String str) {
        for (int i = 0; i < str.length(); i++) {
            this.value[this.len++] = str.charAt(i);
        }
    }
}