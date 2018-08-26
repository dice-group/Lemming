package org.aksw.simba.lemming.metrics.single.metricdecider;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.metricselection.ComplexityBasedMetricDecider;
import org.aksw.simba.lemming.metrics.single.NumberOfTrianglesMetricTest;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.junit.Test;

/**
 * @author DANISH AHMED on 8/26/2018
 */
public class MetricDeciderTest  extends NumberOfTrianglesMetricTest {
    @Test
    public void complexityBasedNodeMetric() {
        ColouredGraph graph = getColouredGraph("email-Eu-core.n3");
        ComplexityBasedMetricDecider complexityBasedMetricDecider = new ComplexityBasedMetricDecider(graph);
        SingleValueMetric nodeMetric = complexityBasedMetricDecider.getMinComplexityForNodeMetric();
        nodeMetric.apply(complexityBasedMetricDecider.getGraph());
    }
}
