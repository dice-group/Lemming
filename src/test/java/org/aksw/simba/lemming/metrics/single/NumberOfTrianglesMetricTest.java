package org.aksw.simba.lemming.metrics.single;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NumberOfTrianglesMetricTest {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { "graph1.n3", 1 });
        testConfigs.add(new Object[] { "graph_loop.n3", 2 });
        testConfigs.add(new Object[] { "graph_loop_2.n3", 5 });
        testConfigs.add(new Object[] { "email-Eu-core.n3", 105461 });

        return testConfigs;
    }
    
    private String graphFile;
    private int expectedTriangles;
    
    public NumberOfTrianglesMetricTest(String graphFile, int expectedTriangles) {
        super();
        this.graphFile = graphFile;
        this.expectedTriangles = expectedTriangles;
    }

    @Test
    public void test() {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(graphFile);
        model.read(is, null, "N3");
        IOUtils.closeQuietly(is);

        GraphCreator creator = new GraphCreator();
        ColouredGraph graph = creator.processModel(model);
        Assert.assertNotNull(graph);

        NumberOfTrianglesMetric metric = new NumberOfTrianglesMetric();
        double countedTriangles = metric.apply(graph);

        Assert.assertEquals(expectedTriangles, countedTriangles, 0.000001);
    }

}
