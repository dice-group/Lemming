package org.aksw.simba.lemming.metrics.single.edgetriangles;

import grph.Grph;
import grph.algo.MultiThreadProcessing;
import it.unimi.dsi.fastutil.ints.IntSet;

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
public class MultiThreadedNodeNeighborsCommonEdgesMetric extends AbstractMetric implements TriangleMetric {

    public MultiThreadedNodeNeighborsCommonEdgesMetric() {
        super("#edgetriangles");
    }

    @Override
    public double apply(IColouredGraph graph) {
        MultiThreadedTriangleCountingProcess process = new MultiThreadedTriangleCountingProcess(graph);
        return process.calculate();
    }

    @Override
    public double calculateComplexity(int edges, int vertices) {
        return (Math.pow(vertices, 2) * edges);
    }

    private static class MultiThreadedTriangleCountingProcess {

        private IColouredGraph graph;
        private int trianglesSum = 0;
        private IntSet edgesOfVertex[];

        public MultiThreadedTriangleCountingProcess(IColouredGraph graph) {
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
                    int n_1, n_2;
                    for (int i = 0; i < sourceEdges.length; ++i) {
                        n_1 = grph.getDirectedSimpleEdgeHead(sourceEdges[i]);
                        if (n_1 == sourceId) {
                            n_1 = grph.getDirectedSimpleEdgeTail(sourceEdges[i]);
                        }
                        // If this edge is not handled by another thread
                        if (n_1 > sourceId) {
                            for (int j = i + 1; j < sourceEdges.length; ++j) {
                                n_2 = grph.getDirectedSimpleEdgeHead(sourceEdges[j]);
                                if (n_2 == sourceId) {
                                    n_2 = grph.getDirectedSimpleEdgeTail(sourceEdges[j]);
                                }
                                // make sure that n_2 is larger than the sourceId (so no other thread is
                                // handling this triangle). Note that n_2 is allowed to be smaller than n_1
                                if ((n_2 > sourceId) && (n_2 != n_1)) {
                                    count += IntSetUtil.intersection(edgesOfVertex[n_1], edgesOfVertex[n_2]).size();
                                }
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
