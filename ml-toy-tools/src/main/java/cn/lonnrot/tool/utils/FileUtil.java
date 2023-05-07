package cn.lonnrot.tool.utils;

import cn.lonnrot.tool.constant.LanguageConstant;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.*;

public class FileUtil {

  private FileUtil() {}

  /**
   * 生成 java class 文件
   *
   * @param processingEnv ProcessingEnvironment
   * @param packageName   包名
   * @param className     类名
   * @param code          代码，不用包含 package
   */
  public static void writeJava(ProcessingEnvironment processingEnv, String packageName, String className, String code) {
    String pkgCode = LanguageConstant.PACKAGE + " " + packageName + LanguageConstant.END_FLAG + "\n";
    try (Writer writer = processingEnv.getFiler().createSourceFile(packageName + LanguageConstant.DOT_FLAG + className).openWriter()) {
      PrintWriter printWriter = new PrintWriter(writer);
      printWriter.println(pkgCode + code);
      printWriter.flush();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

}
