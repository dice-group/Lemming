package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * The distribution of vertex colours of the graph. The sample space is an array
 * of {@link BitSet} objects.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class VertexColourDistributionMetric extends AbstractMetric implements ObjectDistributionMetric<BitSet> {

    public VertexColourDistributionMetric() {
        super("vertexColourDist");
    }

    @Override
    public ObjectDistribution<BitSet> apply(ColouredGraph graph) {
        ObjectArrayList<BitSet> colours = graph.getVertexColours();

        ObjectIntOpenHashMap<BitSet> counts = new ObjectIntOpenHashMap<BitSet>();
        for (int i = 0; i < colours.elementsCount; ++i) {
            counts.putOrAdd((BitSet) ((Object[]) colours.buffer)[i], 1, 1);
        }

        BitSet sampleSpace[] = new BitSet[counts.assigned];
        double distribution[] = new double[counts.assigned];
        int pos = 0;
        for (int i = 0; i < counts.allocated.length; ++i) {
            if (counts.allocated[i]) {
                sampleSpace[pos] = (BitSet) ((Object[]) counts.keys)[i];
                distribution[pos] = counts.values[i];
                ++pos;
            }
        }
        return new ObjectDistribution<BitSet>(sampleSpace, distribution);
    }
}
