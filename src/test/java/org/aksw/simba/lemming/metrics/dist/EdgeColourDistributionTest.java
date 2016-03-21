package org.aksw.simba.lemming.metrics.dist;

import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.dist.EdgeColourDistribution;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import junit.framework.Assert;

public class EdgeColourDistributionTest {

    private static final String EXPECTED_PROPERTY_URIS[] = new String[] {
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://example.org/relation1" };
    private static final int EXPECTED_PROPERTY_COUNTS[] = new int[] { 3, 2 };
    private static final String GRAPH_FILE = "graph1.n3";

    @Test
    public void test() {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(GRAPH_FILE);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);

        EdgeColourDistribution distribution = new EdgeColourDistribution();
        distribution.apply(graph);

        ObjectDoubleOpenHashMap<BitSet> expectedCounts = new ObjectDoubleOpenHashMap<BitSet>();
        ColourPalette palette = graph.getEdgePalette();
        for (int i = 0; i < EXPECTED_PROPERTY_URIS.length; ++i) {
            expectedCounts.put(palette.getColour(EXPECTED_PROPERTY_URIS[i]), EXPECTED_PROPERTY_COUNTS[i]);
        }

        Object sampleSpace[] = distribution.getSampleSpace();
        for (int i = 0; i < sampleSpace.length; ++i) {
            Assert.assertTrue(expectedCounts.containsKey((BitSet) sampleSpace[i]));
            Assert.assertEquals(expectedCounts.get((BitSet) sampleSpace[i]), distribution.getDistribution()[i]);
        }
        Assert.assertEquals(expectedCounts.size(), sampleSpace.length);
    }
}
