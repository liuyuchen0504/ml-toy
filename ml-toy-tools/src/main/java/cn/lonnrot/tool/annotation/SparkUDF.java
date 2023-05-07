package cn.lonnrot.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 本注解作用于方法
 * 注解的作用是将注解的方法生成一个同名的类
 * 这个类继承 Spark 的 UDF 接口
 * 让这个方法可以注册到 Spark 中，让 Spark 任务使用
 *
 * NOTICE:
 *    此注解的方法生成的 UDF 不支持多态，因此最好是在方法内实现多态
 *    修饰的方法必须是 static 方法
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface SparkUDF {
  /**
   * 生成的 udf 存放的目录
   *  1. 如果 subPackageName 为空，则被标注的方法生成的 udf 存放的包为 <packageName>
   *  2. 如果设置 subPackageName，则被标注的方法生成的 udf 存放的包为 <packageName>.<subPackageName>
   *  3. udf 配置存放在  <packageName> 下 UDFConf.getUDFConf()
   */
  String packageName() default "";
  String subPackageName() default "";
}
