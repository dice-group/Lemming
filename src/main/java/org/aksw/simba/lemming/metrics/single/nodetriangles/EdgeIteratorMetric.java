package org.aksw.simba.lemming.metrics.single.nodetriangles;

import grph.Grph;
import it.unimi.dsi.fastutil.ints.IntSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.util.IntSetUtil;

import java.util.*;

/**
 * @author DANISH AHMED on 6/28/2018
 */
public class EdgeIteratorMetric extends AbstractMetric implements TriangleMetric {
    public EdgeIteratorMetric() {
        super("#nodetriangles");
    }

    @Override
    public double apply(IColouredGraph graph) {
        return countTriangles(graph);
    }

    protected double countTriangles(IColouredGraph graph) {
        IntSet[] edges = new IntSet[graph.getGraph().getNumberOfEdges()];
        IntSet[] vertexNeighbors = new IntSet[graph.getGraph().getNumberOfVertices()];
        HashSet<Triangle> visitedV = new HashSet<>();

        int triangleCount = 0;
        Grph grph = graph.getGraph();

        for (int i = 0; i < edges.length; ++i) {
            edges[i] = grph.getVerticesIncidentToEdge(i);
        }

        for (int i = 0; i < vertexNeighbors.length; i++) {
            vertexNeighbors[i] = grph.getInNeighbors(i);
            vertexNeighbors[i].addAll(grph.getOutNeighbors(i));
        }

        Triangle temp = new Triangle(0, 0, 0);
        for (int i = 0; i < edges.length; i++) {
            int[] verticesConnectedToEdge = edges[i].toIntArray();

            // An edge will always have only two vertices
            // In case of loop edge, it will have only 1 vertex
            if (verticesConnectedToEdge.length == 2) {

                for (int vertex : IntSetUtil.intersection(vertexNeighbors[verticesConnectedToEdge[0]],
                        vertexNeighbors[verticesConnectedToEdge[1]])) {
                    if (vertex == verticesConnectedToEdge[0] || vertex == verticesConnectedToEdge[1])
                        continue;
                    temp.set(vertex, verticesConnectedToEdge[0], verticesConnectedToEdge[1]);
                    if (visitedV.contains(temp))
                        continue;

                    try {
                        visitedV.add((Triangle) temp.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    triangleCount++;
                }
            }
        }
        return triangleCount;
    }

    @Override
    public double calculateComplexity(int edges, int vertices) {
        return (Math.pow(edges, 3) / Math.pow(vertices, 3));
    }

    public static class Triangle {
        private int a, b, c;

        public Triangle(int a, int b, int c) {
            set(a, b, c);
        }

        Triangle(Triangle t) {
            this.a = t.a;
            this.b = t.b;
            this.c = t.c;
        }

        public void set(int a, int b, int c) {
            if (a < b) {
                if (a < c) {
                    this.a = a;
                    if (b < c) {
                        this.b = b;
                        this.c = c;
                    } else {
                        this.b = c;
                        this.c = b;
                    }
                } else {
                    this.a = c;
                    this.b = a;
                    this.c = b;
                }
            } else {
                if (a < c) {
                    this.a = b;
                    this.b = a;
                    this.c = c;
                } else {
                    this.c = a;
                    if (b < c) {
                        this.a = b;
                        this.b = c;
                    } else {
                        this.a = c;
                        this.b = b;
                    }
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + a;
            result = prime * result + b;
            result = prime * result + c;
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Triangle other = (Triangle) obj;
            if (a != other.a)
                return false;
            if (b != other.b)
                return false;
            return c == other.c;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return new Triangle(this);
        }
    }
}
