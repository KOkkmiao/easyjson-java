package com.easyjson.bootstrap;

import com.google.common.base.Strings;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * @Author: author
 * @Description:
 * @Date: 2023/1/6 11:48 下午
 * @Version: 1.0
 */
public class Generator {
    private String PkgPath, PkgName;
    private String[] Types;

    private boolean NoStdMarshalers;
    private boolean SnakeCase;
    private boolean LowerCamelCase;
    private boolean OmitEmpty;
    private boolean DisallowUnknownFields;
    private boolean SkipMemberNameUnescaping;

    private String OutName;
    private String BuildTags;
    private String GenBuildFlags;

    private boolean StubsOnly;
    private boolean LeaveTemps;
    private boolean NoFormat;
    private boolean SimpleBytes;

    /**
     * 固定的输出方式
     * easy json 序列化序列化
     */
    public void writeStub() throws FileNotFoundException {


        PrintStream outFile = new PrintStream(this.OutName);
        if (Strings.isNullOrEmpty(this.BuildTags)) {
            outFile.println("// +build "+this.BuildTags);
        }
        outFile.println("// TEMPORARY AUTOGENERATED FILE: easyjson stub code to make the package");
        outFile.println("// compilable during generation.");
        outFile.println();
        outFile.println("package "+this.PkgName);
        if (this.Types!=null&& this.Types.length>0) {
            outFile.println();
            outFile.println("import ");
//            outFile.println( `  "`+pkgWriter+`"`);
//            outFile.println( `  "`+pkgLexer+`"`); 导入默认依赖的包 todo
            outFile.println();
        }

        this.Types = (String[]) Arrays.stream(this.Types).sorted().toArray();

        for (int i =0; i<this.Types.length; i++) {

        }
    }
    /**
     * core print encode decode
     */
    public void writeMain() {

    }

    public static void main(String[] args) {
    }
}
