package org.aksw.simba.lemming;

/**
 * Interface of a decorator for a coloured graph.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface IColouredGraphDecorator extends IColouredGraph {

    /**
     * Returns the decorated object.
     * 
     * @return the decorated object.
     */
    IColouredGraph getDecorated();
}
