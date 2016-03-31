package org.aksw.simba.lemming.algo.refinement;

import java.util.ArrayList;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.refinement.fitness.FitnessFunction;
import org.aksw.simba.lemming.algo.refinement.fitness.MinSquaredError;
import org.aksw.simba.lemming.algo.refinement.operator.LeaveNodeReplacingRefinementOperator;
import org.aksw.simba.lemming.algo.refinement.redberry.RedberryBasedFactory;
import org.aksw.simba.lemming.metrics.single.DiameterMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.junit.Ignore;

import grph.Grph;
import grph.algo.topology.GridTopologyGenerator;
import grph.in_memory.InMemoryGrph;

@Ignore
public class RefinementTest {

    private static final double MIN_FITNESS = 1.0;
    private static final int MAX_ITERATIONS = 20;

    public static void main(String[] args) {
        List<SingleValueMetric> metrics = new ArrayList<>();
        // metrics.add(new AvgClusteringCoefficientMetric());
//        metrics.add(new AvgVertexDegreeMetric());
        metrics.add(new DiameterMetric());
        metrics.add(new NumberOfEdgesMetric());
        metrics.add(new NumberOfVerticesMetric());

        FitnessFunction fitnessFunc = new MinSquaredError();

        CharacteristicExpressionSearcher searcher = new CharacteristicExpressionSearcher(metrics,
                new LeaveNodeReplacingRefinementOperator(metrics), new RedberryBasedFactory(), fitnessFunc, MIN_FITNESS,
                MAX_ITERATIONS);

        ColouredGraph graphs[] = new ColouredGraph[20];
        // StarTopologyGenerator generator = new StarTopologyGenerator();
        GridTopologyGenerator generator = new GridTopologyGenerator();
        // RandomNewmanWattsStrogatzTopologyGenerator generator = new
        // RandomNewmanWattsStrogatzTopologyGenerator();
        Grph temp;
        for (int i = 0; i < graphs.length; ++i) {
            temp = new InMemoryGrph();
            generator.setWidth(i + 2);
            generator.setHeight(i + 1);
            temp.addNVertices((i + 2) * (i + 1));
            // generator.setNumberOfEdges((i + 2) * (i + 1));
            // generator.setK(3);
            generator.compute(temp);
            graphs[i] = new ColouredGraph(temp, null, null);
        }

        RefinementNode n = searcher.findExpression(graphs);
        System.out.println(n.toString());
        System.out.println(n.getFitness());
    }
}
