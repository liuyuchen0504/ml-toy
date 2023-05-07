package cn.lonnrot.feature.operate;

import cn.lonnrot.tool.annotation.SparkUDF;

import java.util.ArrayList;
import java.util.List;

public class Math implements Operate<Integer, Integer> {
  private Math() {}

  public static final String BOOLEAN = "boolean";

  @SparkUDF(packageName = "cn.lonnrot.feature.operate.udf")
  public static Integer add(Integer a, Integer b) {
    return a + b;
  }

  @SparkUDF(packageName = "cn.lonnrot.feature.operate.udf")
  public static int addTriple(Integer a, Integer b, int c) {
    String tmp = "test";
    return a + b + c;
  }

  @SparkUDF(packageName = "cn.lonnrot.feature.operate.udf")
  public static List<Integer> pointMultiply(List<Integer> x, List<Integer> y) {
    assert x.size() == y.size();
    List<Integer> z = new ArrayList<>(x.size());
    for (int i=0; i<x.size(); i++) {
      z.add(x.get(i) + y.get(i));
    }
    return z;
  }

  @Override
  public Integer call(Integer a, Integer b) {
    return add(a, b);
  }

}
