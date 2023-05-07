package cn.lonnrot.feature;

import org.junit.Assert;
import org.junit.Test;


public class MathOperatorTest {

  @Test
  public void testAdd() {
    Assert.assertEquals(2, (int) MathOperator.add(1, 1));
  }

  @Test
  public void testMultiplyFrac() {
    Assert.assertEquals(2.0, MathOperator.multiplyFrac(1, 2), 1E-6);
    Assert.assertEquals(2.0, MathOperator.multiplyFrac(1, 2.0), 1E-6);
    Assert.assertEquals(2.0, MathOperator.multiplyFrac(1.0, 2.0), 1E-6);
    Assert.assertEquals(2.0, MathOperator.multiplyFrac(1L, 2), 1E-6);
    Assert.assertEquals(2.0, MathOperator.multiplyFrac(1L, 2L), 1E-6);
    Assert.assertEquals(2.0, MathOperator.multiplyFrac(1F, 2.0), 1E-6);
    Assert.assertEquals(2.0, MathOperator.multiplyFrac(1D, 2.0), 1E-6);
  }

  @Test
  public void testMultiplyInt() {
    Assert.assertEquals(1L, (long) MathOperator.multiplyInt(1L, 1));
  }

}
