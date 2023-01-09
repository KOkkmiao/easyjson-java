package com.easyjson.util;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @Author: author
 * @Description:
 * @Date: 2023/1/7 12:01 上午
 * @Version: 1.0
 */
public class FPrintStream extends PrintStream {

    public FPrintStream(String outName) throws FileNotFoundException {
        super(outName);
    }

    /**
     * format info and println
     * @param format
     * @param args
     */
    public void fPrintln(String format,Object ...args){
        super.printf(format,args);
        super.println();
    }
    public static void fPrintln(){

    }
}
