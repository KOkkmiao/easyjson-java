package com.easyjson.jwriter;

import com.easyjson.pool.PoolStringBuilder;
import com.fasterxml.jackson.core.io.JsonStringEncoder;

import java.math.BigDecimal;

/**
 * @Author: author
 * @Description:
 * @Date: 2023/1/8 8:46 下午
 * @Version: 1.0
 */
public class Writer {
    protected final static int MAX_BIG_DECIMAL_SCALE = 9999;
    PoolStringBuilder buffer = new PoolStringBuilder();

    public void integer(Integer target) {
        this.buffer.appendInteger(target);
    }

    public void integerStr(Integer target) {
        this.buffer.appendChar('"');
        this.buffer.appendInteger(target);
        this.buffer.appendChar('"');
    }

    public void string(String target) {
        this.buffer.appendChar('"');
        this.buffer.appendChar(JsonStringEncoder.getInstance().quoteAsString(target));
        this.buffer.appendChar('"');
    }

    public void Long(Long target) {
        this.buffer.appendLong(target);
    }

    public void LongStr(Long target) {
        this.buffer.appendChar('"');
        this.buffer.appendLong(target);
        this.buffer.appendChar('"');
    }
    public void objectStr(Object o){
        this.buffer.appendChar('"');
        this.buffer.appendString(String.valueOf(o));
        this.buffer.appendChar('"');
    }
    public void object(Object o){
        this.buffer.appendString(String.valueOf(o));
    }
    public void Float(Float target) {
        object(target);
    }

    public void FloatStr(Float target) {
        objectStr(target);
    }

    public void Double(Double target) {
        object(target);
    }

    public void DoubleStr(Double target) {
        objectStr(target);
    }

    public void bigDecimal(BigDecimal target) {
      this.buffer.appendString(_asString(target));
    }
    public void bigDecimalStr(BigDecimal target) {
        this.buffer.appendChar('"');
        this.buffer.appendString(_asString(target));
        this.buffer.appendChar('"');
    }

    public void RawChar(char target){
        this.buffer.appendChar(target);
    }
    public void RawString(String target){
        this.buffer.appendString(target);
    }
    public void subLastDot(){
        this.buffer.deleteLast();
    }

    public void bool(Boolean bool){
        if (bool) {
            this.buffer.appendChar('t');
            this.buffer.appendChar('r');
            this.buffer.appendChar('u');
            this.buffer.appendChar('e');
        }else {
            this.buffer.appendChar('f');
            this.buffer.appendChar('a');
            this.buffer.appendChar('l');
            this.buffer.appendChar('s');
            this.buffer.appendChar('e');
        }
    }
    public void writeByte(Byte b){
        this.buffer.appendString(String.valueOf(b));
    }
    public void writeByteStr(Byte b){
        this.buffer.appendChar('"');
        this.buffer.appendString(String.valueOf(b));
        this.buffer.appendChar('"');
    }
    public void writeShort(Short b){
        this.buffer.appendShort(b);
    }
    public void writeShortStr(Short b){
        this.buffer.appendChar('"');
        this.buffer.appendShort(b);
        this.buffer.appendChar('"');
    }
    public String toJson(){
        return new String(this.buffer.buildChar());
    }

    protected String _asString(BigDecimal value) {
        if (true) {
            // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector
            int scale = value.scale();
            if ((scale < -MAX_BIG_DECIMAL_SCALE) || (scale > MAX_BIG_DECIMAL_SCALE)) {
                throw new RuntimeException(String.format(
                        "Attempt to write plain `java.math.BigDecimal` (see JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN) with illegal scale (%d): needs to be between [-%d, %d]",
                        scale, MAX_BIG_DECIMAL_SCALE, MAX_BIG_DECIMAL_SCALE));
            }
            return value.toPlainString();
        }
        return value.toString();
    }
}
