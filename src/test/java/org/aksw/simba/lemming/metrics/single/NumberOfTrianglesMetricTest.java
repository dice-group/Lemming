package org.aksw.simba.lemming.metrics.single;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public abstract class NumberOfTrianglesMetricTest {
    
    private static final double DOUBLE_COMPARISON_DELTA = 0.000001;

    protected SingleValueMetric metric;
    protected ColouredGraph graph;
    protected int expectedTriangles;

    public NumberOfTrianglesMetricTest(String graphFile, int expectedTriangles) {
        this.graph = getColouredGraph(graphFile);
        this.expectedTriangles = expectedTriangles;
    }

    protected void test() {
        Assert.assertNotNull(graph);
        double countedTriangles = metric.apply(graph);
        Assert.assertEquals(expectedTriangles, countedTriangles, DOUBLE_COMPARISON_DELTA);
    }
    
    public static ColouredGraph getColouredGraph(String graphFile) {
        Model model = ModelFactory.createDefaultModel();
        try (InputStream is = NumberOfTrianglesMetricTest.class.getClassLoader().getResourceAsStream(graphFile)) {
            model.read(is, null, "N3");
        } catch (Exception e) {
            throw new RuntimeException("Couldn't read model from resource \"" + graphFile + "\".", e);
        }

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        return graph;
    }

}
