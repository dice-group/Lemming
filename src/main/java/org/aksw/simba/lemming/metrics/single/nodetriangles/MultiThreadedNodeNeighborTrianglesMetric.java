package org.aksw.simba.lemming.metrics.single.nodetriangles;

import grph.Grph;
import grph.algo.MultiThreadProcessing;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.util.IntSetUtil;

/**
 * This metric is the number of triangles of the graph.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MultiThreadedNodeNeighborTrianglesMetric extends AbstractMetric implements TriangleMetric {

    public MultiThreadedNodeNeighborTrianglesMetric() {
        super("#nodetriangles");
    }

    @Override
    public double apply(IColouredGraph graph) {
        MultiThreadedTriangleCountingProcess process = new MultiThreadedTriangleCountingProcess(graph);
        return process.calculate();
    }

    @Override
    public double calculateComplexity(int edges, int vertices) {
        return (Math.pow(edges, 2) / Math.pow(vertices, 2));
    }

    private static class MultiThreadedTriangleCountingProcess {

        private IColouredGraph graph;
        private int trianglesSum = 0;
        private IntSet edgesOfVertex[];

        MultiThreadedTriangleCountingProcess(IColouredGraph graph) {
            this.graph = graph;
            edgesOfVertex = new IntSet[graph.getGraph().getNumberOfVertices()];
        }

        protected double calculate() {
            Grph grph = graph.getGraph();
            for (int i = 0; i < edgesOfVertex.length; ++i) {
                edgesOfVertex[i] = grph.getOutEdges(i);
                edgesOfVertex[i].addAll(grph.getInEdges(i));
            }
            /*
             * A triangle is handled by the thread which handles the node with the lowest id
             * in that triangle.
             */
            new MultiThreadProcessing(graph.getGraph().getVertices()) {
                @Override
                protected void run(int threadID, int sourceId) {
                    int count = 0;
                    int sourceEdges[] = edgesOfVertex[sourceId].toIntArray();
                    IntSet connectedNodesSet = new IntOpenHashSet();
                    int n;

                    for (int sourceEdge : sourceEdges) {
                        n = grph.getDirectedSimpleEdgeHead(sourceEdge);
                        if (n > sourceId) {
                            connectedNodesSet.add(n);
                            continue;
                        }
                        if (n == sourceId) {
                            n = grph.getDirectedSimpleEdgeTail(sourceEdge);
                            if (n > sourceId)
                                connectedNodesSet.add(n);
                        }
                    }

                    int connectedNodes[] = connectedNodesSet.toIntArray();
                    for (int i = 0; i < connectedNodes.length; i++) {
                        for (int j = i + 1; j < connectedNodes.length; j++) {
                            if (IntSetUtil
                                    .intersection(edgesOfVertex[connectedNodes[i]], edgesOfVertex[connectedNodes[j]])
                                    .size() > 0) {
                                ++count;
                            }
                        }
                    }
                    addCount(count);
                }
            };
            return trianglesSum;
        }

        private synchronized void addCount(int count) {
            trianglesSum += count;
        }
    }

}
