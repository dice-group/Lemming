package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.IntArrayList;

import grph.Grph.DIRECTION;

public class StdDevVertexDegree extends AvgVertexDegreeMetric {

    protected DIRECTION direction;

    public StdDevVertexDegree(DIRECTION direction) {
        super(direction == DIRECTION.in ? "stdDevInDegree" : "stdDevOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {
        IntArrayList degrees = null;
        if (direction == DIRECTION.in) {
            degrees = graph.getGraph().getAllInEdgeDegrees();
        } else {
            degrees = graph.getGraph().getAllOutEdgeDegrees();
        }
        return calculateStdDev(degrees, calculateAvg(degrees));
    }

    protected double calculateStdDev(IntArrayList degrees, double avg) {
        double temp, sum = 0;
        for (int i = 0; i < degrees.elementsCount; ++i) {
            temp = avg - degrees.buffer[i];
            temp *= temp;
            sum += temp;
        }
        return Math.sqrt(sum / degrees.elementsCount);
    }
}
