package org.aksw.simba.lemming.metrics.single.triangle;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)

@Suite.SuiteClasses({ AyzNumberOfTrianglesMetricTest.class, DuolionNumberOfTrianglesMetricTest.class,
      EdgeIteratorNumberOfTrianglesMetricTest.class, ForwardNumberOfTriangleMetricTest.class,
      MatrixMultiplicationNumberOfTrianglesMetricTest.class, NodeIteratorCoreNumberOfTrianglesMetricTest.class,
      NodeIteratorNumberOfTrianglesMetricTest.class })
public class NumberOfTrianglesMetricTestSuite {

}
