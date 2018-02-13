package org.aksw.simba.lemming.metrics.single;

import grph.Grph;
import grph.algo.MultiThreadProcessing;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;

import toools.set.IntHashSet;
import toools.set.IntSet;
import toools.set.IntSets;

/**
 * This metric is the number of triangles of the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class NumberOfSimpleTrianglesMetric extends AbstractMetric implements SingleValueMetric {

	public NumberOfSimpleTrianglesMetric() {
		super("#simpleTriangles");
	}

	@Override
	public double apply(ColouredGraph graph) {
		MultiThreadedTriangleCountingProcess process = new MultiThreadedTriangleCountingProcess(graph);
		return process.calculate();
	}

	private static class MultiThreadedTriangleCountingProcess {

		private ColouredGraph graph;
		private int trianglesSum = 0;
		private IntSet edges[];

		public MultiThreadedTriangleCountingProcess(ColouredGraph graph) {
			this.graph = graph;
			edges = new IntSet[graph.getGraph().getNumberOfVertices()];
		}

		protected double calculate() {
			Grph grph = graph.getGraph();
			for (int i = 0; i < edges.length; ++i) {
				edges[i] = grph.getOutEdges(i);
				edges[i].addAll(grph.getInEdges(i));
			}
			/*
			 * A triangle is handled by the thread which handles the node with the lowest id in that triangle.
			 */
			new MultiThreadProcessing(graph.getGraph().getVertices()) {
				@Override
				protected void run(int threadID, int sourceId) {
					int count = 0;
					int sourceEdges[] = edges[sourceId].toIntArray();
					IntSet connectedNodesSet = new IntHashSet();
                    int n;
                    for (int i = 0; i < sourceEdges.length; ++i) {
                        n = grph.getDirectedSimpleEdgeHead(sourceEdges[i]);
                        if (n == sourceId) {
                            n = grph.getDirectedSimpleEdgeTail(sourceEdges[i]);
                        }
                        if (n > sourceId) {
                            connectedNodesSet.add(n);
                        }
                    }
                    int connectedNodes[] = connectedNodesSet.toIntArray();
                    for (int i = 0; i < connectedNodes.length; i++) {
                        for (int j = i + 1; j < connectedNodes.length; j++) {
                            if(IntSets.intersection(edges[connectedNodes[i]], edges[connectedNodes[j]]).size() > 0) {
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
