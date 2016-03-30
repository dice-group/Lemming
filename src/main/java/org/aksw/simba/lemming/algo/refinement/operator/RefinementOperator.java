package org.aksw.simba.lemming.algo.refinement.operator;

import java.util.Set;

import org.aksw.simba.lemming.algo.expression.Expression;

/**
 * Interface of a refinement operator that can refine a given {@link Expression}
 * instance.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface RefinementOperator {

    /**
     * Refines the given {@link Expression} and returns a set of refined
     * {@link Expression} instances.
     * 
     * @param expression
     *            the {@link Expression} that should be refined
     * @return the set of refined {@link Expression} instances
     */
    public Set<Expression> refine(Expression expression);

}
