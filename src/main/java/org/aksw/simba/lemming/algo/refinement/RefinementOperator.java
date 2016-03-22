package org.aksw.simba.lemming.algo.refinement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

/**
 * Refinement operator that explores a tree of possible {@link Expression}
 * instances and tries to refine them to get the instance with the best fitness.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class RefinementOperator {

    /**
     * List of metrics that can be used by the refinement.
     */
    private List<SingleValueMetric> metrics;
    /**
     * The factory that is used to generate {@link RefinementNode}s.
     */
    private RefinementNodeFactory factory;
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

    /**
     * Constructor.
     * 
     * @param metrics
     *            List of metrics that can be used by the refinement.
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
    public RefinementOperator(List<SingleValueMetric> metrics, RefinementNodeFactory factory,
            FitnessFunction fitnessFunc, double minFitness, int maxIterations) {
        this.metrics = metrics;
        this.factory = factory;
        this.fitnessFunc = fitnessFunc;
        this.minFitness = minFitness;
        this.maxIterations = maxIterations;
    }

    /**
     * Finds the {@link Expression} with the highest fitness for the given
     * graphs.
     * 
     * @param graphs
     *            an array of graphs for which the best expression is searched.
     * @return an {@link RefinementNode} encapsulating an expression which has a
     *         fitness value <code>&lt; {@link #minFitness}</code> or had the
     *         best fitness value before hitting the {@link #maxIterations}
     *         limit.
     */
    public RefinementNode findExpression(ColouredGraph graphs[]) {
        // precalculate the metrics values
        ObjectDoubleOpenHashMap<String> graphVectors[] = calculateGraphMetrics(graphs);
        // initialize the tree
        Set<RefinementNode> nodes = generateMetricNodes();
        RefinementTree tree = new RefinementTree(nodes);
        // initialize the queue
        SortedSet<RefinementNode> queue = new TreeSet<RefinementNode>();
        for (RefinementNode node : nodes) {
            node.setFitness(fitnessFunc.getFitness(node, graphVectors));
        }
        // start refinement
        RefinementNode firstNode = queue.first();
        RefinementNode bestNode = firstNode;
        int iteration = 0;
        // While we haven't reached the maximum number of iterations and the
        // fitness of the best node is not good enough, refine the expression
        while ((iteration < maxIterations) && (bestNode.getFitness() < minFitness)) {
            // refine the best node
            nodes = refine(firstNode, tree);
            // calculate the fitness of all new nodes and add them to the queue
            for (RefinementNode node : nodes) {
                node.setFitness(fitnessFunc.getFitness(node, graphVectors));
            }
            // pick a new best node
            firstNode = queue.first();
            // if the current node is the best node seen so far (this makes sure
            // that we will alwas return the best node, even if we hit the
            // maximum number of iterations)
            if (bestNode.getFitness() < firstNode.getFitness()) {
                bestNode = firstNode;
            }
            ++iteration;
        }
        return bestNode;
    }

    /**
     * For every graph, the values of the metrics are calculated, added to a map
     * and stored in an array. The i-th map of the result array contains the
     * values for the i-th graph.
     * 
     * @param graphs
     *            {@link ColouredGraph} for which the values should be
     *            calculated.
     * @return array containing the mappings from metric name to metric value
     *         for the single graphs
     */
    private ObjectDoubleOpenHashMap<String>[] calculateGraphMetrics(ColouredGraph[] graphs) {
        @SuppressWarnings("unchecked")
        ObjectDoubleOpenHashMap<String>[] vectors = new ObjectDoubleOpenHashMap[graphs.length];
        for (int i = 0; i < vectors.length; ++i) {
            vectors[i] = calculateGraphMetrics(graphs[i]);
        }
        return vectors;
    }

    /**
     * The values of the metrics are calculated for the given graph and put into
     * a map.
     * 
     * @param graph
     *            {@link ColouredGraph} for which the values should be
     *            calculated.
     * @return a mapping from metric name to metric value for the given graph
     */
    private ObjectDoubleOpenHashMap<String> calculateGraphMetrics(ColouredGraph graph) {
        ObjectDoubleOpenHashMap<String> vector = new ObjectDoubleOpenHashMap<String>(2 * metrics.size());
        for (SingleValueMetric metric : metrics) {
            vector.put(metric.getName(), metric.apply(graph));
        }
        return vector;
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
        Set<RefinementNode> newNodes = new HashSet<RefinementNode>();
        // TODO create new expressions
        Expression newExp = null;
        RefinementNode newNode = factory.createNode(newExp);
        // if this node is not already inside the tree
        if (!tree.getNodes().contains(newNode)) {
            // add the node to the tree
            tree.getNodes().add(newNode);
            // connect the new node and its parent
            node.children.add(newNode);
            newNode.setParent(node);
        }
        return newNodes;
    }

}
