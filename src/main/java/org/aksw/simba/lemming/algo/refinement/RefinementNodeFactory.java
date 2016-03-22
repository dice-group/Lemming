package org.aksw.simba.lemming.algo.refinement;

import org.aksw.simba.lemming.algo.expression.Expression;

/**
 * Factory class for creating {@link RefinementNode} instances.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface RefinementNodeFactory {

    /**
     * Creates a {@link RefinementNode} instance using the given expression.
     * 
     * @param expression
     *            the expression that should be encapsulated by a
     *            {@link RefinementNode}
     * @return the created node
     */
    public RefinementNode createNode(Expression expression);

}
