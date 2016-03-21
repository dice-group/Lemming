package org.aksw.simba.lemming.metrics.dist;

import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

public class OutDegreeDistributionMetricTest {

    private static final int EXPECTED_DEGREES[] = new int[] { 0, 2, 3 };
    private static final double EXPECTED_DEGREE_VALUES[] = new double[] { 3, 1, 1 };
    private static final String GRAPH_FILE = "graph1.n3";
    private static final double DELTA = 0.000001;

    @Test
    public void test() {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);

        OutDegreeDistributionMetric metric = new OutDegreeDistributionMetric();
        IntDistribution distribution = metric.apply(graph);

        Assert.assertArrayEquals(EXPECTED_DEGREES, distribution.sampleSpace);
        for (int i = 0; i < EXPECTED_DEGREE_VALUES.length; ++i) {
            Assert.assertEquals(EXPECTED_DEGREE_VALUES[i], distribution.values[i], DELTA);
        }
        Assert.assertEquals(EXPECTED_DEGREE_VALUES.length, distribution.values.length);
    }
}
