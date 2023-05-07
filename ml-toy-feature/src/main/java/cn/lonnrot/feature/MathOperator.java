package cn.lonnrot.feature;

import cn.lonnrot.tool.annotation.SparkUDF;

import java.util.ArrayList;
import java.util.List;

/**
 * 算子设计参考
 * <p>
 * 1. 函数参数尽量使用 Object，如果使用具体类型 UDF 使用可能存在类型转换问题
 * 2. 返回值不能使用 Object，UDF 返回类型只支持如下类型
 *    - Short Integer Long
 *    - Float Double
 *    - String
 *    - Date
 *    - List<>
 *    - Map<>
 * 3. 所以一般来说对于数值型最好设计两种，Long / Double 来分别处理整型和小数型
 * 4. UDF 不支持多态，因此算子最好不要用多态，而是使用不同的名称
 *    一般来说，如果遵守上面三条规则，则第 4 点不用注意，因为参数都是用 Object 就避免了使用多态
 *    SparkUDF 注解有重复函数检查
 * 5. 方法必须是 static 类型
 * </p>
 */
public class MathOperator {

  // 不推荐这种设计，原因参考上面算子设计
  @SparkUDF(packageName = Constant.UDF_PACKAGE)
  public static Integer add(Integer a, Integer b) {
    return a + b;
  }

  @SparkUDF(packageName = Constant.UDF_PACKAGE)
  public static List<Integer> pointMultiply(List<Integer> x, List<Integer> y) {
    assert x.size() == y.size();
    List<Integer> z = new ArrayList<>(x.size());
    for (int i=0; i<x.size(); i++) {
      z.add(x.get(i) + y.get(i));
    }
    return z;
  }

  // 推荐这种设计
  @SparkUDF(packageName = Constant.UDF_PACKAGE)
  public static Double multiplyFrac(Object x, Object y) {
    return toDouble(x) * toDouble(y);
  }

  @SparkUDF(packageName = Constant.UDF_PACKAGE)
  public static Long multiplyInt(Object x, Object y) {
    return toLong(x) * toLong(y);
  }

  private static Double toDouble(Object num) {
    return num instanceof Double ? (Double) num : Double.parseDouble(String.valueOf(num));
  }

  private static Long toLong(Object num) {
    return num instanceof Long ? (Long) num : Long.parseLong(String.valueOf(num));
  }

}
