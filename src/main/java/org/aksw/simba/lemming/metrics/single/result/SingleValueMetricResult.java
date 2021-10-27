package org.aksw.simba.lemming.metrics.single.result;

public interface SingleValueMetricResult {

    /* get metric value from the result*/
    public double metric();

    /* update metric value*/
    public void update();
}
