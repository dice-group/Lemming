package org.aksw.simba.lemming.algo.refinement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.refinement.fitness.FitnessFunction;
import org.aksw.simba.lemming.algo.refinement.operator.RefinementOperator;
import org.aksw.simba.lemming.metrics.MetricUtils;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * This searcher tries to find an {@link Expression} that is characteristic for
 * a given set of graphs. This is achieved by using a {@link RefinementOperator}
 * to explore a tree of possible {@link Expression} instances by iteratively
 * refining the {@link Expression} with the best fitness based on a given
 * {@link FitnessFunction}.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class CharacteristicExpressionSearcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CharacteristicExpressionSearcher.class);

    /**
     * List of metrics that can be used by the refinement.
     */
    private List<SingleValueMetric> metrics;
    /**
     * The factory that is used to generate {@link RefinementNode}s.
     */
    private RefinementNodeFactory factory;
    /**
     * The refinement operator that is used to generate new {@link Expression}
     * instances.
     */
    private RefinementOperator refineOperator;
    /**
     * The fitness function that is used to assign fitness scores to the single
     * nodes.
     */
    private FitnessFunction fitnessFunc;
    /**
     * The minimum fitness score an {@link Expression} has to achieve to be
     * returned as solution.
     */
    private double minFitness;
    /**
     * The maximal number of iterations the operator will perform.
     */
    private int maxIterations;

    private boolean debug = false;

    /**
     * Constructor.
     * 
     * @param metrics
     *            List of metrics that can be used by the refinement.
     * @param refineOperator
     *            The refinement operator that is used to generate new
     *            {@link Expression} instances.
     * @param factory
     *            The factory that is used to generate {@link RefinementNode}s.
     * @param fitnessFunc
     *            The fitness function that is used to assign fitness scores to
     *            the single nodes.
     * @param minFitness
     *            The minimum fitness score an {@link Expression} has to achieve
     *            to be returned as solution.
     * @param maxIterations
     *            The maximal number of iterations the operator will perform.
     */
    public CharacteristicExpressionSearcher(List<SingleValueMetric> metrics, RefinementOperator refineOperator,
            RefinementNodeFactory factory, FitnessFunction fitnessFunc, double minFitness, int maxIterations) {
        this.metrics = metrics;
        this.refineOperator = refineOperator;
        this.factory = factory;
        this.fitnessFunc = fitnessFunc;
        this.minFitness = minFitness;
        this.maxIterations = maxIterations;
    }

    /**
     * Finds the k {@link Expression}s with the highest fitness for the given
     * graphs.
     * 
     * @param graphs
     *            an array of graphs for which the best expression is searched.
     * @param k
     *            the (minimum) number of {@link Expression}s that should be
     *            returned.
     * @return a {@link SortedSet} of {@link RefinementNode}s encapsulating
     *         expression that have fitness values <code>&lt; 
     *         {@link #minFitness}</code> or had the best fitness values before
     *         hitting the {@link #maxIterations} limit.
     */
    public SortedSet<RefinementNode> findExpression(ColouredGraph graphs[], int k) {
        SortedSet<RefinementNode> bestNodes = new TreeSet<RefinementNode>();
        // precalculate the metrics values
        ObjectDoubleOpenHashMap<String> graphVectors[] = MetricUtils.calculateGraphMetrics(graphs, metrics);
        if (debug) {
            LOGGER.warn("Refinement Tree:\n{}\n", printGraphMetrics(graphVectors));
        }
        // initialize the tree
        Set<RefinementNode> nodes = generateMetricNodes();
        RefinementTree tree = new RefinementTree(nodes);
        // initialize the queue
        SortedSet<RefinementNode> queue = new TreeSet<RefinementNode>();
        for (RefinementNode node : nodes) {
            node.setFitness(fitnessFunc.getFitness(node.getExpression(), graphVectors));
            if (Double.isNaN(node.getFitness())) {
                LOGGER.warn("Got a node with an undefined fitness: " + node.toString());
            } else {
                queue.add(node);
                addToBestNodes(bestNodes, node, k);
            }
        }
        // start refinement
        RefinementNode nextNode = queue.last();
        queue.remove(nextNode);
        bestNodes.add(nextNode);
        int iteration = 0;
        // While we haven't reached the maximum number of iterations and the
        // fitness of the worst best node is not good enough, refine the
        // expression
        while ((iteration < maxIterations) && (bestNodes.first().getFitness() < minFitness)) {
            // refine the best node
            nodes = refine(nextNode, tree);
            // calculate the fitness of all new nodes and add them to the queue
            for (RefinementNode node : nodes) {
                node.setFitness(fitnessFunc.getFitness(node.getExpression(), graphVectors));
                if (Double.isNaN(node.getFitness())) {
                    LOGGER.warn("Got a node with an undefined fitness: " + node.toString());
                } else {
                    queue.add(node);
                    addToBestNodes(bestNodes, node, k);
                }
            }
            // pick a new best node
            nextNode = queue.last();
            queue.remove(nextNode);
            addToBestNodes(bestNodes, nextNode, k);
            ++iteration;
        }
        // if (debug) {
        // LOGGER.warn("Refinement Tree:\n{}\n", printTree(tree));
        // }
        return bestNodes;
    }

    /**
     * Adds the given node to the given set of best nodes. The size of the set
     * is checked. If the size of the set is > k the set is reduced to k nodes
     * if the node with the lowest fitness does not have the same fitness as the
     * node with the best fitness.
     * 
     * @param bestNodes
     *            the set containing the k best nodes
     * @param node
     *            the node that might be added to the set of best nodes
     * @param k
     *            the goal size of the set
     */
    private void addToBestNodes(SortedSet<RefinementNode> bestNodes, RefinementNode node, int k) {
        // if we have not reached the minimum number of best nodes
        if (bestNodes.size() < k) {
            bestNodes.add(node);
        } else {
            // if the current node is better (or equal) than the
            // worst best node seen so far (this makes sure that we will always
            // return the best node, even if we hit the maximum number of
            // iterations)
            if (bestNodes.first().getFitness() <= node.getFitness()) {
                bestNodes.add(node);
                while ((bestNodes.size() > k) && (bestNodes.first().getFitness() < bestNodes.last().getFitness())) {
                    bestNodes.remove(bestNodes.first());
                }
            }
        }
    }

    /**
     * This method generates a set of {@link RefinementNode}s each comprising of
     * an {@link AtomicVariable} that encapsulates one of the {@link #metrics}.
     * 
     * @return a set of initial {@link RefinementNode}s
     */
    private Set<RefinementNode> generateMetricNodes() {
        Set<RefinementNode> metricNodes = new HashSet<RefinementNode>(2 * metrics.size());
        for (SingleValueMetric metric : metrics) {
            metricNodes.add(factory.createNode(new AtomicVariable(metric)));
        }
        return metricNodes;
    }

    /**
     * Refines the given node and adds the generated refinements to the tree and
     * the queue if they are not already existing.
     * 
     * @param node
     *            the node that should be refined
     * @param tree
     *            the tree containing all nodes created so far
     * @param queue
     *            queue containing nodes that could be visited in the future
     */
    private Set<RefinementNode> refine(RefinementNode node, RefinementTree tree) {
        Set<Expression> newExpressions = refineOperator.refine(node.getExpression());
        RefinementNode newNode;
        Set<RefinementNode> newNodes = new HashSet<RefinementNode>();
        for (Expression newExp : newExpressions) {
            newNode = factory.createNode(newExp);
            // if this node is not already inside the tree
            if ((newNode != null) && !tree.getNodes().contains(newNode)) {
                // add the node to the tree
                tree.getNodes().add(newNode);
                // connect the new node and its parent
                node.children.add(newNode);
                newNode.setParent(node);
                newNodes.add(newNode);
            }
        }
        return newNodes;
    }

    private String printTree(RefinementTree tree) {
        StringBuilder builder = new StringBuilder();
        for (RefinementNode n : tree.getFirstStageNodes()) {
            printNode(builder, n, 0);
        }
        return builder.toString();
    }

    private void printNode(StringBuilder builder, RefinementNode node, int depth) {
        builder.append('+');
        for (int i = 0; i < depth; ++i) {
            builder.append(' ');
        }
        builder.append(String.format("%.3f", node.fitness));
        builder.append("  ");
        builder.append(node.reducedExpression);
        builder.append('\n');

        for (RefinementNode n : node.children) {
            printNode(builder, n, depth + 1);
        }
    }

    private String printGraphMetrics(ObjectDoubleOpenHashMap<String>[] graphVectors) {
        StringBuilder builder = new StringBuilder();
        builder.append("Metric");
        for (int i = 0; i < graphVectors.length; ++i) {
            builder.append("\tgraph");
            builder.append(i);
        }
        builder.append('\n');
        String key;
        for (int i = 0; i < graphVectors[0].allocated.length; ++i) {
            if (graphVectors[0].allocated[i]) {
                key = (String) ((Object[]) graphVectors[0].keys)[i];
                builder.append(key);
                builder.append('\t');
                builder.append(Double.toString(graphVectors[0].values[i]));
                for (int j = 1; j < graphVectors.length; ++j) {
                    builder.append('\t');
                    if (graphVectors[j].containsKey(key)) {
                        builder.append(Double.toString(graphVectors[j].get(key)));
                    } else {
                        builder.append('?');
                    }
                }
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
