package org.aksw.simba.lemming.metrics.dist;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

public class EdgeColourDistribution extends AbstractDistributionalMetric {

    public EdgeColourDistribution() {
        super("vertexColourDist");
    }

    @Override
    public void apply(ColouredGraph graph) {
        ObjectArrayList<BitSet> colours = graph.getEdgeColours();

        ObjectIntOpenHashMap<BitSet> counts = new ObjectIntOpenHashMap<BitSet>();
        for (int i = 0; i < colours.elementsCount; ++i) {
            counts.putOrAdd((BitSet) ((Object[]) colours.buffer)[i], 1, 1);
        }

        sampleSpace = new Object[counts.assigned];
        distribution = new double[counts.assigned];
        int pos = 0;
        for (int i = 0; i < counts.allocated.length; ++i) {
            if (counts.allocated[i]) {
                sampleSpace[pos] = ((Object[]) counts.keys)[i];
                distribution[pos] = counts.values[i];
                ++pos;
            }
        }
    }
}
