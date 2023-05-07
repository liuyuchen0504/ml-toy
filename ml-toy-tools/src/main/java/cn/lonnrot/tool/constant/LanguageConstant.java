package cn.lonnrot.tool.constant;


/**
 * Java 语言层面的常量
 */

public class LanguageConstant {

  public static final String END_FLAG = ";";
  public static final String DOT_FLAG = ".";

  public static final String PACKAGE = "package";
  public static final String IMPORT = "import";

  // basic type
  public static final String BOOLEAN = "boolean";
  public static final String SHORT = "short";
  public static final String INT = "int";
  public static final String LONG = "long";
  public static final String FLOAT = "float";
  public static final String DOUBLE = "double";
  public static final String CHAR = "char";
  public static final String STRING = "String";

  public static final String BOOLEAN_TYPE = "Boolean";
  public static final String SHORT_TYPE = "Short";
  public static final String INTEGER_TYPE = "Integer";
  public static final String LONG_TYPE = "Long";
  public static final String FLOAT_TYPE = "Float";
  public static final String DOUBLE_TYPE = "Double";
  public static final String CHARACTER_TYPE = "Character";
  public static final String STRING_TYPE = "String";

  //
  public static final String OVERRIDE = "Override";

  public static String wrapBasicType(String basic) {
    switch (basic) {
      case BOOLEAN:
        return BOOLEAN_TYPE;
      case SHORT:
        return SHORT_TYPE;
      case INT:
        return INTEGER_TYPE;
      case LONG:
        return LONG_TYPE;
      case FLOAT:
        return FLOAT_TYPE;
      case DOUBLE:
        return DOUBLE_TYPE;
      case CHAR:
        return CHARACTER_TYPE;
      default:
        return basic;
    }
  }


}
