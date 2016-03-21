package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;

import grph.Grph;

/**
 * The distribution of out degrees. The sample space is sorted ascending.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class OutDegreeDistributionMetric extends AbstractMetric implements IntDistributionMetric {

    public OutDegreeDistributionMetric() {
        super("outDegreeDist");
    }

    @Override
    public IntDistribution apply(ColouredGraph graph) {
        IntIntOpenHashMap counts = new IntIntOpenHashMap();
        Grph g = graph.getGraph();
        IntArrayList outDegrees = g.getAllOutEdgeDegrees();
        for (int i = 0; i < outDegrees.elementsCount; ++i) {
            counts.putOrAdd(outDegrees.buffer[i], 1, 1);
        }
        return IntDistribution.fromMap(counts);
    }
}
