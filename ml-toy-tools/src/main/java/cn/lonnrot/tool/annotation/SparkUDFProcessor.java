package cn.lonnrot.tool.annotation;

import cn.lonnrot.tool.constant.LanguageConstant;
import cn.lonnrot.tool.utils.FileUtil;
import cn.lonnrot.tool.utils.NodeUtil;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * <a href="https://www.cnblogs.com/intotw/p/13815793.html">Java编译期注解处理器详细使用方法</a>
 */

// 指定解析的注解
@SupportedAnnotationTypes("cn.lonnrot.tool.annotation.SparkUDF")
// 指定支持的版本，向下兼容
@SupportedSourceVersion(SourceVersion.RELEASE_8)
// 自动生成 SPI 文件，注解使用的是 SPI 机制
@AutoService(Processor.class)
public class SparkUDFProcessor extends AbstractProcessor {

  // 将 Element 类型转为 JCTree 类型
  private JavacElements elementUtils;
  // 构建 JCTree Node，JCTree 不能直接使用
  private TreeMaker treeMaker;
  // 辅助生成工具，String 转成 Name 节点
  private Names names;
  // 打印编译期的信息，但是这个不保证能够输出到标准输出
  private Messager message;

  // Spark UDF
  private final static String interfaceMethodName = "call";
  private final static String UDF = "UDF";

  private final static String UDF_CONF = "UDFConf";


  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    final Context context = ((JavacProcessingEnvironment) processingEnvironment).getContext();
    this.elementUtils = (JavacElements) processingEnvironment.getElementUtils();
    this.treeMaker = TreeMaker.instance(context);
    this.names = Names.instance(context);
    this.message = processingEnvironment.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // 记录 Spark 需要注册的 UDF 配置
    StringBuilder udfConf = new StringBuilder("{");

    String udfConfPkg = "";

    // 1. 获取被注解元素
    Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(SparkUDF.class);
    repetitionDetection(elements);  // 重复函数名检查
    // 2. 注解的逻辑，对每一个注解的元素处理
    for (Element element : elements) {
      SparkUDF annotation = element.getAnnotation(SparkUDF.class);

      // 新生成类所在包名
      String pkgName = annotation.packageName();
      udfConfPkg = pkgName;
      String subPkgName = annotation.subPackageName();
      if (!subPkgName.equals("")) {
        pkgName += (LanguageConstant.DOT_FLAG + subPkgName);
      }
      // 注解的方法名，也是新生成的类名
      String methodName = element.getSimpleName().toString();
      String className = element.getEnclosingElement().toString();

      // 2.1 注解方法的 JCTree 结构
      JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
      // 2.2 生成新的方法，主要是方法名不一样，参数类型基本类转成包装类型
      JCTree.JCMethodDecl newMethodDecl = treeMaker.MethodDef(
              // 修饰
              treeMaker.Modifiers(Flags.PUBLIC, List.of(NodeUtil.overrideAnnotation(treeMaker, names))),
              // 方法名称
              names.fromString(interfaceMethodName),
              // 返回类型
              NodeUtil.toBasicType(treeMaker, names, LanguageConstant.wrapBasicType(methodDecl.getReturnType().type.toString())),
              // 范型参数
              List.nil(),
              // 方法参数
              wrapParams(methodDecl.params),
              // 异常
              List.nil(),
              // 方法体
              treeMaker.Block(0, List.of(
                      treeMaker.Return(
                              treeMaker.Select(treeMaker.Ident(names.fromString(className)),
                                      names.fromString(methodName+"("+ methodDecl.params.stream().map(p -> p.getName().toString())
                                              .collect(Collectors.joining(", ")) +")"))
                      )
              )),
//              methodDecl.body,
              null
      );
      // 2.3 生成类
      JCTree.JCClassDecl classDecl = treeMaker.ClassDef(
              // 修饰
              treeMaker.Modifiers(Flags.PUBLIC),
              // 类名
              names.fromString(methodName),
              // 范型参数
              List.nil(),
              // 父类
              null,
              // 接口
              List.of(constructImplement(methodDecl)),
              // 定义语句：字段，方法等
              List.of(newMethodDecl)
      );

      // 3. 生成代码
      String code = LanguageConstant.IMPORT + " " + PackageSupportEnum.UDF_ALL + LanguageConstant.END_FLAG + "\n" +
              classDecl.toString();
      // 4. 写入文件
      FileUtil.writeJava(processingEnv, pkgName, methodName, code);

      // 5. 构建 udf 注册配置，json 格式
      udfConf.append("\"").append(methodName).append("\": {")
              .append("\"class\": \"").append(pkgName).append(LanguageConstant.DOT_FLAG).append(methodName).append("\",")
              .append("\"returnType\": \"").append(methodDecl.getReturnType().type.toString()).append("\"},");
    }

    // 5. 生成配置
    udfConf.deleteCharAt(udfConf.length()-1).append("}");
    FileUtil.writeJava(processingEnv, udfConfPkg, UDF_CONF, udfConfClass(udfConf.toString()).toString());

    return false;
  }

  private JCTree.JCTypeApply constructImplement(JCTree.JCMethodDecl methodDecl) {
    java.util.List<String> variableTypeNames = methodDecl.params.stream().map(var ->
      // 类型全名，基础类型转包装类型
      LanguageConstant.wrapBasicType(var.vartype.type.toString())
    ).collect(Collectors.toList());
    int paramsSize = variableTypeNames.size();
    variableTypeNames.add(LanguageConstant.wrapBasicType(methodDecl.getReturnType().type.toString()));
    return treeMaker.TypeApply(
            NodeUtil.JCIdent(treeMaker, names, UDF+paramsSize),
            NodeUtil.toBasicTypes(treeMaker, names, variableTypeNames));
  }

  private List<JCTree.JCVariableDecl> wrapParams(List<JCTree.JCVariableDecl> params) {
    return List.from(params.stream().map(var ->
      NodeUtil.constructVariable(treeMaker, var.name,
              treeMaker.Ident(names.fromString(LanguageConstant.wrapBasicType(var.vartype.type.toString()))))
    ).collect(Collectors.toList()));
  }

  private JCTree.JCClassDecl udfConfClass(String conf) {
    JCTree.JCMethodDecl newMethodDecl = treeMaker.MethodDef(
            // 修饰
            treeMaker.Modifiers(Flags.PUBLIC | Flags.STATIC),
            // 方法名称
            names.fromString("getUDFConf"),
            // 返回类型
            NodeUtil.toBasicType(treeMaker, names, "String"),
            // 范型参数
            List.nil(),
            // 方法参数
            List.nil(),
            // 异常
            List.nil(),
            // 方法体
            treeMaker.Block(0, List.of(treeMaker.Return(treeMaker.Literal(conf)))),
            null
    );
    return treeMaker.ClassDef(
            // 修饰
            treeMaker.Modifiers(Flags.PUBLIC),
            // 类名
            names.fromString(UDF_CONF),
            // 范型参数
            List.nil(),
            // 父类
            null,
            // 接口
            List.nil(),
            // 定义语句：字段，方法等
            List.of(newMethodDecl)
    );
  }

  private void repetitionDetection(Set<? extends Element> elements) {
    if (elements.stream().map(element -> element.getSimpleName().toString()).distinct().count() < elements.size()) {
      throw new IllegalStateException("UDF method repetition.");
    }
  }

  private enum PackageSupportEnum {
    UDF_ALL("org.apache.spark.sql.api.java", "*");


    private final String packageName;
    private final String className;

    PackageSupportEnum(String packageName, String className) {
      this.packageName = packageName;
      this.className = className;
    }

    public String getPackageName() {
      return packageName;
    }

    public String getClassName() {
      return className;
    }

    @Override
    public String toString() {
      return packageName + LanguageConstant.DOT_FLAG + className;
    }
  }

}
