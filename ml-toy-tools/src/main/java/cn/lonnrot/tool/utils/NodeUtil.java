package cn.lonnrot.tool.utils;

import cn.lonnrot.tool.constant.LanguageConstant;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 构建 JCTree 节点工具包
 */
public class NodeUtil {
  private NodeUtil() {}

  // ----------- 获取节点信息 -----------
  public static String getElementFullName(Element element) {
    assert element instanceof TypeElement;
    return ((TypeElement) element).getQualifiedName().toString();
  }

  /**
   * 获取上一级节点
   *
   * @param element Element
   * @return Element
   */
  public static Element getUpLevelElement(Element element) {
    return element.getEnclosingElement();
  }



  // ----------- 生成节点信息 -----------
  public static JCTree.JCExpression JCIdent(TreeMaker treeMaker, Names names, String typeName) {
    return treeMaker.Ident(names.fromString(typeName));
  }

  public static JCTree.JCExpression JCLiteral(TreeMaker treeMaker, String literal) {
    return treeMaker.Literal(literal);
  }

  public static JCTree.JCExpression toBasicType(TreeMaker treeMaker, Names names, String typeName) {
    return JCIdent(treeMaker, names, typeName);
  }

  public static List<JCTree.JCExpression> toBasicTypes(TreeMaker treeMaker, Names names, String... typeNames) {
    return toBasicTypes(treeMaker, names, Arrays.asList(typeNames));
  }

  public static List<JCTree.JCExpression> toBasicTypes(TreeMaker treeMaker, Names names, java.util.List<String> typeNames) {
    return List.from(typeNames.stream().map(name -> toBasicType(treeMaker, names, name))
            .collect(Collectors.toList()));
  }

  /**
   * 构造方法或者无初始化的变量类型
   *
   * @param treeMaker treeMaker
   * @param name      变量名称
   * @param type      变量类型
   * @return  JCVariableDecl
   */
  public static JCTree.JCVariableDecl constructVariable(TreeMaker treeMaker, Name name, JCTree.JCExpression type) {
    return treeMaker.VarDef(
            treeMaker.Modifiers(Flags.PARAMETER),
            name, type,null
    );
  }

  /**
   * 生成无参注解
   *
   * @param treeMaker       TreeMaker
   * @param names           Names
   * @param annotationName  注解名，自定义的需要使用全名，或者配置 Import 可以使用注解名
   * @return  JCAnnotation
   */
  public static JCTree.JCAnnotation notParamAnnotation(TreeMaker treeMaker, Names names, String annotationName) {
    return treeMaker.Annotation(treeMaker.Ident(names.fromString(annotationName)), List.nil());
  }

  public static JCTree.JCAnnotation overrideAnnotation(TreeMaker treeMaker, Names names) {
    return notParamAnnotation(treeMaker, names, LanguageConstant.OVERRIDE);
  }

  public static JCTree.JCImport constructImport(TreeMaker treeMaker, Names names, String packageName, String className) {
    return treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString(packageName)),
            names.fromString(className)), false);
  }

}
