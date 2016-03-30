package org.aksw.simba.lemming.algo.expression;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import com.carrotsearch.hppc.BitSet;

/**
 * This is a pre-order iterator that stores the route that it took to the
 * current node.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class ExpressionIterator implements Iterator<Expression> {

    public ExpressionIterator(Expression root) {
        next = root;
    }

    /**
     * The parent nodes of the current Expression.
     */
    private Deque<Expression> parents = new ArrayDeque<Expression>();
    /**
     * The route that the iterator has taken. Contains a 0 if the iterator has
     * visited the left child of a node or a 1 for the right child.
     */
    private BitSet route = new BitSet();
    /**
     * The next {@link Expression} instance that will be returned.
     */
    private Expression next;

    @Override
    public boolean hasNext() {
        if (next == null) {
            retrieveNext();
        }
        return next != null;
    }

    @Override
    public Expression next() {
        if (next == null) {
            retrieveNext();
        }
        parents.add(next);
        Expression result = next;
        next = null;
        return result;
    }

    private void retrieveNext() {
        Expression last = parents.peekLast();
        // if the last Expression was an operation, the next node is its left
        // child
        if (last.isOperation()) {
            next = last.getLeft();
        }
        // else, the last Expression was a leaf node and has to be removed
        // from now on, we go up and are working with already visited parent
        // nodes
        while (next == null) {
            parents.removeLast();
            route.clear(parents.size());
            // if we have reached the end
            if (parents.size() == 0) {
                return;
            }
            // if we have seen the left child of this parent, its right child is
            // the next node
            if (!route.get(parents.size() - 1)) {
                next = parents.peekLast().getRight();
                route.set(parents.size() - 1);
            }
        }
    }

    /**
     * Returns the route to the current Expression node. Note, that this is the
     * last node that has been returned as long as {@link #hasNext()} has not
     * been called. After calling {@link #hasNext()} and before calling
     * {@link #next()} it is the route to the next Expression node.
     * 
     * @return
     */
    public BitSet getRoute() {
        return (BitSet) route.clone();
    }

    public int getRouteLength() {
        return parents.size();
    }
}
