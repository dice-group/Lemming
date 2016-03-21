package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;

import grph.Grph;

/**
 * The distribution of in degrees. The sample space is sorted ascending.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class InDegreeDistributionMetric extends AbstractMetric implements IntDistributionMetric {

    public InDegreeDistributionMetric() {
        super("inDegreeDist");
    }

    @Override
    public IntDistribution apply(ColouredGraph graph) {
        IntIntOpenHashMap counts = new IntIntOpenHashMap();
        Grph g = graph.getGraph();
        IntArrayList inDegrees = g.getAllInEdgeDegrees();
        for (int i = 0; i < inDegrees.elementsCount; ++i) {
            counts.putOrAdd(inDegrees.buffer[i], 1, 1);
        }
        return IntDistribution.fromMap(counts);
    }
}
