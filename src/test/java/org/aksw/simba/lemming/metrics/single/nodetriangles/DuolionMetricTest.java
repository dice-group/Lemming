package org.aksw.simba.lemming.metrics.single.nodetriangles;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.nodetriangles.forward.ForwardNodeTriangleMetric;
import org.aksw.simba.lemming.util.ColouredGraphConverter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class DuolionMetricTest {

    @Test
    public void duolionMetric() {
        ColouredGraph graph = ColouredGraphConverter.convertFileToGraph("email-Eu-core.n3");
        double expectedNumOfTriangles = 105461;
        Assert.assertNotNull(graph);

        final double edgeSurvivalProbability = 0.9;
        DuolionMetric metric = new DuolionMetric(new ForwardNodeTriangleMetric(), edgeSurvivalProbability,
                new Random().nextLong());
        double countedTriangles = metric.apply(graph);

        double range = 0.25;
        double minRange = expectedNumOfTriangles - (expectedNumOfTriangles * range);
        double maxRange = expectedNumOfTriangles + (expectedNumOfTriangles * range);
        Assert.assertTrue(countedTriangles >= minRange && countedTriangles <= maxRange);
    }
}
