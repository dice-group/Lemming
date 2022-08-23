package org.aksw.simba.lemming.tools;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MinVertexOutDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.util.IOHelper;

public class GraphAnalysisTool {

    private static String[] graphFiles = new String[] { //"sider_nt/org_sider_names.nt",
//            "sider_nt/synth_sider_names.nt",
//            "sider_nt/synth_sider_ids.nt",
//            "sider_nt/synth_sider_ids_as_RDF.nt",
//            "sider_nt/synth_sider_clean.nt"
            "org_dogfood_OhneLtr.nt"};

    public static void main(String[] args) {
        // MultiThreadProcessing.defaultNumberOfThreads = 1;

        // For this test, we do not need assertions
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);

        List<SingleValueMetric> metrics = new ArrayList<>();
        metrics.add(new NumberOfVerticesMetric());
        metrics.add(new NumberOfEdgesMetric());
        metrics.add(new AvgVertexDegreeMetric());
        metrics.add(new MinVertexOutDegreeMetric());
//        metrics.add(new MaxVertexDegreeMetric());
        // metrics.add(new DiameterMetric());
        // metrics.add(new AvgClusteringCoefficientMetric());
        // metrics.add(new NumberOfTrianglesMetric());

        for (int i = 0; i < graphFiles.length; i++) {
            System.out.println("Analyzing " + graphFiles[i]);
            ColouredGraph graph = IOHelper.readGraphFromFile(graphFiles[i], "N3");
            for (SingleValueMetric m : metrics) {
                System.out.println(m.getName() + " = " + m.apply(graph));
            }
        }
    }
}
