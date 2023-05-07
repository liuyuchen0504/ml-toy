# 特征模块
特征模块分为：特征和算子两个模块

## 特征模块
> To Be Continue...

## 算子模块

算子模块要解决的核心问题是线上线下算子共享的问题。核心思路是：**Java 开发的算子函数，通过注册成 UDF 提供给离线使用**。

### SparkUDF 注解
1. 给你的函数添加 `@SparkUDF` 注解
    > 自动生成 `UDF` 函数，以及注册信息  
      具体可以参考[MathOperator](https://github.com/Inforg0504/ml-toy/blob/master/ml-toy-feature/src/main/java/cn/lonnrot/feature/MathOperator.java)
    
    ```java
    class Math {
      
      @SparkUDF(packageName = "cn.lonnrot.feature.operate.udf")
      public static Integer add(Integer a, Integer b) {
        return a + b;
      }
      
    }
    ```
   
    > 注解位于 `ml-toy-tools` 模块，请将算子模块依赖该模块  
      未来会提供 Maven 包

2. 算子打包，jar 放在离线 PySpark 项目中
3. 注册 UDF 到 Spark 中
    > 具体可以参考[算子注册](https://github.com/Inforg0504/ml-toy-offline/blob/master/tests/feature/test_operator.py)
    ```python
    def register_java_udf():
        config = get_udf_conf("cn.lonnrot.feature.operate.udf.UDFConf", jar_path)
        for k, f in config.items():
            register_java_udf(spark, k, f["class"], f["returnType"])
    ```

### 算子设计建议

```text
1. 函数参数尽量使用 Object，如果使用具体类型 UDF 使用可能存在类型转换问题
2. 返回值不能使用 Object，UDF 返回类型只支持如下类型
    - Short / Integer / Long
    - Float / Double
    - String
    - Date
    - List
    - Map
3. 所以一般来说对于数值型最好设计两种，Long / Double 来分别处理整型和小数型
4. UDF 不支持多态，因此算子最好不要用多态，而是使用不同的名称
   一般来说，如果遵守上面三条规则，则第 4 点不用注意，因为参数都是用 Object 就避免了使用多态
   SparkUDF 注解有重复函数检查
5. 方法必须是 static 类型
```

### SparkUDF 注解原理

#### 原生构建 SparkUDF 过程

1. 实现 `UDF` 接口
    ```java
    package cn.lonnrot.feature.operate.udf;
   
    public class add implements UDF2<Integer, Integer, Integer> {
        @Override()
        public Integer call(Integer a, Integer b) {
            return MathOperator.add(a, b);
        }
    }
    ```
2. 手动注册到 PySpark 中
    ```python
    # PySpark >= 2.3 为例
    spark.udf.registerJavaFunction(
        "add",                                 # 注册的名字
        "cn.lonnrot.feature.operate.udf.add",  # 对应的 UDF 类
        IntegerType()                          # 返回类型
    )
    ```

#### SparkUDF 注解做了什么

使用编译器修改 Java AST 技术，在编译期间，获取 `@SparkUDF` 注解的方法，然后使用 TreeMaker 工具生成：
1. 被注解的方法生成对应的 Spark 类（如对 add 方法，生成实现 UDF2 接口的类）
2. 生成对应的 PySpark 注册 UDF 需要的信息（名字，UDF 类，返回类型）
    > 生成的注册信息通过注解 packageName 包下的 `UDFConf.getUDFConf()` 获取；  
      Json 格式

如果大家对 SparkUDF 注解感兴趣，可以阅读位于 ml-toy-tools 中的 [SparkUDFProcessor.class](https://github.com/Inforg0504/ml-toy/blob/master/ml-toy-tools/src/main/java/cn/lonnrot/tool/annotation/SparkUDFProcessor.java)

