package org.aksw.simba.lemming.metrics.dist.multi;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.dist.IntDistribution;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;

public class ColouredOutDegreeDistributionMetricTest {

    private static final String EXPECTED_VERTEX_CLASSES[] = new String[] { "http://example.org/class1",
            "http://example.org/class1|http://example.org/class2" };
    private static final int EXPECTED_DEGREES[][] = new int[][] { { 3 }, { 2 } };
    private static final double EXPECTED_DEGREE_VALUES[][] = new double[][] { { 1 }, { 1 } };
    private static final int EXPECTED_DEGREES_OF_VERTEXES_WITHOUT_COLOUR[] = new int[] { 0 };
    private static final double EXPECTED_DEGREE_VALUES_OF_VERTEXES_WITHOUT_COLOUR[] = new double[] { 3 };
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

        MultipleIntDistributionMetric<BitSet> metric = new ColouredOutDegreeDistributionMetric();
        Map<BitSet, IntDistribution> distributions = metric.apply(graph);

        Map<BitSet, int[]> expectedSampleSpace = new HashMap<BitSet, int[]>();
        Map<BitSet, double[]> expectedCounts = new HashMap<BitSet, double[]>();
        ColourPalette palette = graph.getVertexPalette();
        BitSet colour;
        for (int i = 0; i < EXPECTED_VERTEX_CLASSES.length; ++i) {
            if (EXPECTED_VERTEX_CLASSES[i].contains("|")) {
                colour = palette.getColourMixture(EXPECTED_VERTEX_CLASSES[i].split("\\|"));
            } else {
                colour = palette.getColour(EXPECTED_VERTEX_CLASSES[i]);
            }
            Assert.assertTrue("There is no colour for " + EXPECTED_VERTEX_CLASSES[i], colour.cardinality() > 0);
            expectedSampleSpace.put(colour, EXPECTED_DEGREES[i]);
            expectedCounts.put(colour, EXPECTED_DEGREE_VALUES[i]);
        }
        colour = new BitSet();
        expectedSampleSpace.put(colour, EXPECTED_DEGREES_OF_VERTEXES_WITHOUT_COLOUR);
        expectedCounts.put(colour, EXPECTED_DEGREE_VALUES_OF_VERTEXES_WITHOUT_COLOUR);

        IntDistribution distribution;
        double expectedValues[];
        for (BitSet c : expectedSampleSpace.keySet()) {
            Assert.assertTrue("There is no distribution for colour " + c.toString(), distributions.containsKey(c));
            distribution = distributions.get(c);
            Assert.assertArrayEquals("Sampe space of colour " + c.toString() + " does not match.",
                    expectedSampleSpace.get(c), distribution.sampleSpace);
            expectedValues = expectedCounts.get(c);
            for (int i = 0; i < expectedValues.length; ++i) {
                Assert.assertEquals("Error for colour " + c.toString(), expectedValues[i], distribution.values[i],
                        DELTA);
            }
            Assert.assertEquals("Error for colour " + c.toString(), expectedValues.length, distribution.values.length);
        }
    }

}
