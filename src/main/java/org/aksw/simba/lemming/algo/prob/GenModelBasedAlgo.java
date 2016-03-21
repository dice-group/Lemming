package org.aksw.simba.lemming.algo.prob;

import java.util.Random;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.EdgeColourDistributionMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.VertexColourDistributionMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

import grph.Grph;

/**
 * This algorithm generates a graph based on a given source graph and a number
 * of vertices. It is based on probability distributions that are gathered from
 * the source graph and used to randomly generate a new graph.
 * 
 * FIXME add the usage of colour-based in and out degree distributions.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class GenModelBasedAlgo {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenModelBasedAlgo.class);

    private long seed;

    public GenModelBasedAlgo() {
        this(System.currentTimeMillis());
    }

    public GenModelBasedAlgo(long seed) {
        this.seed = seed;
    }

    public ColouredGraph generateGraph(ColouredGraph source, int numberOfVertices) {
        // determine the number of edges
        double edgesPerNode = (double) source.getGraph().getNumberOfEdges()
                / (double) source.getGraph().getNumberOfVertices();
        int numberOfEdges = (int) Math.round(edgesPerNode * numberOfVertices);

        ColouredGraph target = new ColouredGraph(source.getVertexPalette(), source.getEdgePalette());

        Random random = new Random(seed);
        ColouringSheme vertexColouringSheme = sampleVertices(random, source, target, numberOfVertices);
        ColouringSheme edgeColouringSheme = sampleEdgeColours(random, source, target, numberOfEdges);
        sampleEdges(random, source, target, vertexColouringSheme, edgeColouringSheme);
        return target;
    }

    private ColouringSheme sampleVertices(Random random, ColouredGraph source, ColouredGraph target,
            int numberOfVertices) {
        // Determine the colour distribution
        VertexColourDistributionMetric vertexColourMetric = new VertexColourDistributionMetric();
        ObjectDistribution<BitSet> dist = vertexColourMetric.apply(source);
        Object colours[] = dist.getSampleSpace();
        ColouringSheme sheme = new ColouringSheme(colours);
        double probs[] = dist.getValues();
        double sum = 0;
        for (int i = 0; i < probs.length; ++i) {
            sum += probs[i];
        }
        // Sample the colours of the single nodes
        double samples[] = random.doubles(numberOfVertices, 0, sum).toArray();
        double sample;
        int colourId;
        for (int i = 0; i < samples.length; ++i) {
            sample = samples[i];
            colourId = 0;
            while (sample > 0) {
                sample -= probs[colourId];
                ++colourId;
                if (colourId >= probs.length) {
                    String msg = "Got a sample (" + samples[i] + ") that seems to be larger than the probability sum ("
                            + sum + "). Aborting.";
                    LOGGER.error(msg);
                    throw new IllegalStateException(msg);
                }
            }
            if (sheme.colourToIdMapping[colourId] == null) {
                sheme.colourToIdMapping[colourId] = new IntArrayList();
            }
            sheme.colourToIdMapping[colourId].add(i);
            target.addVertex((BitSet) colours[colourId]);
        }
        return sheme;
    }

    private ColouringSheme sampleEdgeColours(Random random, ColouredGraph source, ColouredGraph target,
            int numberOfEdges) {
        // Determine the colour distribution
        EdgeColourDistributionMetric edgeColourMetric = new EdgeColourDistributionMetric();
        ObjectDistribution<BitSet> dist = edgeColourMetric.apply(source);
        Object colours[] = dist.getSampleSpace();
        ColouringSheme sheme = new ColouringSheme(colours);
        double probs[] = dist.getValues();
        double sum = 0;
        for (int i = 0; i < probs.length; ++i) {
            sum += probs[i];
        }
        // Sample the colours of the single edges
        double samples[] = random.doubles(numberOfEdges, 0, sum).toArray();
        double sample;
        int colourId;
        for (int i = 0; i < samples.length; ++i) {
            sample = samples[i];
            colourId = 0;
            while (sample > 0) {
                sample -= probs[colourId];
                ++colourId;
                if (colourId >= probs.length) {
                    String msg = "Got a sample (" + samples[i] + ") that seems to be larger than the probability sum ("
                            + sum + "). Aborting.";
                    LOGGER.error(msg);
                    throw new IllegalStateException(msg);
                }
            }
            if (sheme.colourToIdMapping[colourId] == null) {
                sheme.colourToIdMapping[colourId] = new IntArrayList();
            }
            sheme.colourToIdMapping[colourId].add(i);
        }
        return sheme;
    }

    private void sampleEdges(Random random, ColouredGraph source, ColouredGraph target,
            ColouringSheme vertexColouringSheme, ColouringSheme edgeColouringSheme) {
        // TODO find an implementation that has a better performance than this
        // one...
        ObjectArrayList<BitSet> sourceVertexColours = source.getVertexColours();
        ObjectArrayList<BitSet> sourceEdgeColours = source.getEdgeColours();
        Grph sourceGraph = source.getGraph();
        BitSet colour;
        int count, headId, tailId;
        ObjectIntOpenHashMap<BitSet> headDistribution = new ObjectIntOpenHashMap<BitSet>();
        ObjectIntOpenHashMap<BitSet> tailDistribution = new ObjectIntOpenHashMap<BitSet>();
        for (int i = 0; i < edgeColouringSheme.colours.length; ++i) {
            // For every colour for which we have to create at least on edge
            if (edgeColouringSheme.colourToIdMapping[i] != null) {
                colour = (BitSet) edgeColouringSheme.colours[i];

                // Determine the head and tail distributions
                count = 0;
                headDistribution.clear();
                tailDistribution.clear();
                for (int j = 0; j < sourceEdgeColours.buffer.length; ++j) {
                    if (sourceEdgeColours.buffer[j].equals(colour)) {
                        headId = sourceGraph.getDirectedSimpleEdgeHead(j);
                        headDistribution.putOrAdd((BitSet) ((Object[]) sourceVertexColours.buffer)[headId], 1, 1);
                        tailId = sourceGraph.getDirectedSimpleEdgeTail(j);
                        tailDistribution.putOrAdd((BitSet) ((Object[]) sourceVertexColours.buffer)[tailId], 1, 1);
                        ++count;
                    }
                }

                // Sample the head and tail of the edges that have the current
                // colour
                for (int j = 0; j < edgeColouringSheme.colourToIdMapping[i].elementsCount; ++j) {
                    headId = tailId = 0;
                    while (headId == tailId) {
                        // sample the head
                        headId = sampleVertex(random, vertexColouringSheme, sourceVertexColours, headDistribution,
                                count);
                        // sample the tail
                        tailId = sampleVertex(random, vertexColouringSheme, sourceVertexColours, headDistribution,
                                count);
                        // TODO add handling of cases in which there is no
                        // solution - even after several tries.
                    }
                    // Add the edge to the target graph
                    target.addEdge(tailId, headId, colour);
                }
            }
        }
        source.getEdgeColours();
    }

    private int sampleVertex(Random random, ColouringSheme vertexColouringSheme,
            ObjectArrayList<BitSet> sourceVertexColours, ObjectIntOpenHashMap<BitSet> headDistribution,
            int numberOfDistSamples) {
        BitSet colour;
        int colourId, vertexId = -1;
        while (vertexId < 0) {
            // sample a colour of the vertex
            colour = sampleEdgeEndColour(random, headDistribution, numberOfDistSamples);
            // search for this colour in the target graph
            colourId = 0;
            while ((colourId < vertexColouringSheme.colours.length)
                    && (!vertexColouringSheme.colours[colourId].equals(colour))) {
                ++colourId;
            }
            // if we could find the colour and their are vertices of this colour
            // inside the target graph
            if (colourId < vertexColouringSheme.colours.length) {
                if ((vertexColouringSheme.colourToIdMapping[colourId] != null)) {
                    if (vertexColouringSheme.colourToIdMapping[colourId].elementsCount == 1) {
                        vertexId = vertexColouringSheme.colourToIdMapping[colourId].buffer[0];
                    } else {
                        vertexId = vertexColouringSheme.colourToIdMapping[colourId].buffer[random
                                .nextInt(vertexColouringSheme.colourToIdMapping.length)];
                    }
                }
            }
        }
        return vertexId;
    }

    private BitSet sampleEdgeEndColour(Random random, ObjectIntOpenHashMap<BitSet> headDistribution,
            int numberOfDistSamples) {
        int colourId = random.nextInt(numberOfDistSamples);
        // find the first colour
        while (!headDistribution.allocated[colourId]) {
            ++colourId;
        }
        int temp = colourId;
        while (temp > 0) {
            temp -= headDistribution.values[colourId];
            while ((colourId < headDistribution.allocated.length) && (!headDistribution.allocated[colourId])) {
                ++colourId;
            }
            if (colourId >= headDistribution.allocated.length) {
                String msg = "Got a sample (" + colourId + ") that seems to be larger than the probability sum ("
                        + numberOfDistSamples + "). Aborting.";
                LOGGER.error(msg);
                throw new IllegalStateException(msg);
            }
        }
        return (BitSet) ((Object[]) headDistribution.keys)[colourId];
    }

    protected static class ColouringSheme {
        public Object colours[];
        public IntArrayList colourToIdMapping[];

        public ColouringSheme(Object colours[]) {
            this.colours = colours;
            colourToIdMapping = new IntArrayList[colours.length];
        }
    }
}
