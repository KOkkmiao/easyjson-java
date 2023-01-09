package com.easyjson.jwriter;

import java.math.BigDecimal;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * @Author: author
 * @Description:
 * @Date: 2023/1/8 8:46 下午
 * @Version: 1.0
 */
public class Writer {
    StringBuilder buffer = new StringBuilder();

    public void integer(Integer target) {
        this.buffer.append(target);
    }

    public void integer(int target) {
        this.buffer.append(target);
    }

    public void string(String target) {
        this.buffer.append(target);
    }

    public void Long(Long target) {
        this.buffer.append(target);
    }

    public void Long(long target) {
        this.buffer.append(target);
    }

    public void Float(Float target) {
        this.buffer.append(target);
    }

    public void Float(float target) {
        this.buffer.append(target);
    }

    public void Double(Double target) {
        this.buffer.append(target);
    }

    public void Double(double target) {
        this.buffer.append(target);
    }

    public void bigDecimal(BigDecimal target) {
        this.buffer.append(target);
    }

    public void RawByte(char target){
        this.buffer.append(target);
    }
    public void RawString(String target){
        string(target);
    }
    public String toJson(){
        return this.buffer.toString();
    }
}
