package org.aksw.simba.lemming.metrics.dist.multi;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.dist.IntDistribution;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.Grph;

/**
 * The distribution of out degrees for every colour. The sample spaces of the
 * single distributions are sorted ascending.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class ColouredOutDegreeDistributionMetric extends AbstractMetric
        implements MultipleIntDistributionMetric<BitSet> {

    public ColouredOutDegreeDistributionMetric() {
        super("colouredOutDegreeDists");
    }

    @Override
    public Map<BitSet, IntDistribution> apply(ColouredGraph graph) {
        ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap> colourCountsMap = new ObjectObjectOpenHashMap<BitSet, IntIntOpenHashMap>();
        IntIntOpenHashMap counts;
        Grph g = graph.getGraph();
        IntArrayList outDegrees = g.getAllOutEdgeDegrees();
        BitSet colour;
        for (int i = 0; i < outDegrees.elementsCount; ++i) {
            colour = graph.getVertexColour(i);
            if (colourCountsMap.containsKey(colour)) {
                counts = colourCountsMap.lget();
            } else {
                counts = new IntIntOpenHashMap();
                colourCountsMap.put(colour, counts);
            }
            counts.putOrAdd(outDegrees.buffer[i], 1, 1);
        }

        Map<BitSet, IntDistribution> result = new HashMap<BitSet, IntDistribution>(2 * colourCountsMap.assigned);
        for (int i = 0; i < colourCountsMap.allocated.length; ++i) {
            if (colourCountsMap.allocated[i]) {
                result.put((BitSet) ((Object[]) colourCountsMap.keys)[i],
                        IntDistribution.fromMap((IntIntOpenHashMap) ((Object[]) colourCountsMap.values)[i]));
            }
        }
        return result;
    }
}
