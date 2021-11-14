package org.aksw.simba.lemming.metrics.testdummy;

import java.util.HashSet;
import java.util.Set;

import org.aksw.simba.lemming.algo.expression.AtomicVariable;
import org.aksw.simba.lemming.algo.expression.Constant;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.algo.expression.Operation;
import org.aksw.simba.lemming.algo.expression.Operator;
import org.aksw.simba.lemming.metrics.single.AvgVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfEdgesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;
import org.aksw.simba.lemming.metrics.single.StdDevVertexDegree;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ExpressionChecker;
import org.junit.Test;

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

import grph.Grph.DIRECTION;

public class CharacteristicExpressionsTest1 {

    ObjectDoubleOpenHashMap<String> mapMetricValues = new ObjectDoubleOpenHashMap<>();

    // Creating a set of Expressions for manually creating expressions.
    Set<Expression> setOfExpressions = new HashSet<>();

    private void initializeInput() {

        // SWDF parameters
        mapMetricValues.put("#vertices", 45420.0);
        mapMetricValues.put("stdDevInDegree", 69.55651745881998);
        mapMetricValues.put("#edgetriangles", 978980.0);
        mapMetricValues.put("maxInDegree", 9365.0);
        mapMetricValues.put("avgDegree", 6.4538529282254515);
        mapMetricValues.put("maxOutDegree", 28179.0);
        mapMetricValues.put("stdDevOutDegree", 179.7017899058663);
        mapMetricValues.put("#nodetriangles", 373086.0);
        mapMetricValues.put("#edges", 293134.0);

        // Expressions for SWDF from paper
        Operation operation1 = new Operation(new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)),
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                                new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.TIMES),
                        new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS),
                Operator.DIV);
        // System.out.println(operation1.toString());
        setOfExpressions.add(operation1);

        Operation operation2 = new Operation(new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)),
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS),
                        new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.MINUS),
                Operator.DIV);
        // System.out.println(operation2.toString());
        setOfExpressions.add(operation2);

        Operation operation3 = new Operation(new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)),
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.DIV),
                        new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS),
                Operator.DIV);
        // System.out.println(operation3.toString());
        setOfExpressions.add(operation3);

        Operation operation4 = new Operation(new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)),
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS),
                        new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.MINUS),
                Operator.DIV);
        // System.out.println(operation4.toString());
        setOfExpressions.add(operation4);

        Operation operation5 = new Operation(new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)),
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                                new AtomicVariable(new StdDevVertexDegree(DIRECTION.out)), Operator.DIV),
                        new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS),
                Operator.DIV);
        // System.out.println(operation5.toString());
        setOfExpressions.add(operation5);
        mapMetricValues.put(operation1.toString(), 0.0926);
        mapMetricValues.put(operation2.toString(), -0.1751);
        mapMetricValues.put(operation3.toString(), 0.9997);
        mapMetricValues.put(operation4.toString(), 0.1294);
        mapMetricValues.put(operation5.toString(), 0.9965);

    }

    private void initializeInputLGD() {

        // SWDF parameters
        mapMetricValues.put("#vertices", 591649.0);
        mapMetricValues.put("stdDevInDegree", 1010.3381907563557);
        mapMetricValues.put("#edgetriangles", 90351.0);
        mapMetricValues.put("maxInDegree", 479601.0);
        mapMetricValues.put("avgDegree", 3.8790363881287724);
        mapMetricValues.put("maxOutDegree", 43.0);
        mapMetricValues.put("stdDevOutDegree", 3.1155261417503834);
        mapMetricValues.put("#nodetriangles", 89797.0);
        mapMetricValues.put("#edges", 2295028.0);

        // Expressions for SWDF from paper
        Operation operation1 = new Operation(new Operation(
                new Operation(new Constant(2.0),
                        new AtomicVariable(new NumberOfVerticesMetric()), Operator.TIMES),
                new AtomicVariable(new NumberOfEdgesMetric()), Operator.MINUS),
                
                new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                        new AtomicVariable(new AvgVertexDegreeMetric()), Operator.TIMES),
                
                Operator.DIV);
        System.out.println(operation1.toString());
        setOfExpressions.add(operation1);

        Operation operation2 = new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                                new AtomicVariable(new AvgVertexDegreeMetric()), Operator.TIMES),
                        new AtomicVariable(new AvgVertexDegreeMetric()), Operator.TIMES),
                
                Operator.DIV);
        System.out.println(operation2.toString());
        setOfExpressions.add(operation2);

        Operation operation3 = new Operation( new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                new AtomicVariable(new NumberOfEdgesMetric()), Operator.MINUS),
                
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.out)), Operator.PLUS),
                        new AtomicVariable(new NumberOfVerticesMetric()), Operator.MINUS),
                
                Operator.DIV);
        System.out.println(operation3.toString());
        setOfExpressions.add(operation3);

        Operation operation4 = new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                
                new Operation(
                        new AtomicVariable(new NumberOfEdgesMetric()),
                        new Operation(new AtomicVariable(new AvgVertexDegreeMetric()),
                                new Constant(1.0), Operator.MINUS), Operator.TIMES),
                Operator.DIV);
        System.out.println(operation4.toString());
        setOfExpressions.add(operation4);

        Operation operation5 = new Operation( new Operation(new AtomicVariable(new NumberOfVerticesMetric()),
                new AtomicVariable(new NumberOfEdgesMetric()), Operator.MINUS),
                
                new Operation(
                        new Operation(new AtomicVariable(new NumberOfEdgesMetric()),
                                new AtomicVariable(new MaxVertexDegreeMetric(DIRECTION.in)), Operator.PLUS),
                        new AtomicVariable(new NumberOfVerticesMetric()), Operator.MINUS),
                
                Operator.DIV);
        System.out.println(operation5.toString());
        setOfExpressions.add(operation5);
        mapMetricValues.put(operation1.toString(), -0.1250);
        mapMetricValues.put(operation2.toString(), 0.0160);
        mapMetricValues.put(operation3.toString(), -0.9978);
        mapMetricValues.put(operation4.toString(), 0.0851);
        mapMetricValues.put(operation5.toString(), -0.7857);

    }
    
    @Test
    public void checkMetricsFromExpressionsSWDF() {

        initializeInput();

        // Initialize Expression Checker
        ExpressionChecker expressionChecker = new ExpressionChecker(setOfExpressions, mapMetricValues);
        expressionChecker.storeExpressions1(mapMetricValues);

        expressionChecker.checkExpressions();

        System.out.println("Metric Details : ");
        System.out.println("Metric to Increase : " + expressionChecker.getMetricToIncrease()
                + ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceIncreaseMetric());

        System.out.println("Metric to Decrease : " + expressionChecker.getMetricToDecrease()
                + ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceDecreaseMetric());
        
        System.out.println();
        System.out.println();

    }
    
    @Test
    public void checkMetricsFromExpressionsLGD() {

        initializeInputLGD();

        // Initialize Expression Checker
        ExpressionChecker expressionChecker = new ExpressionChecker(setOfExpressions, mapMetricValues);
        expressionChecker.storeExpressions1(mapMetricValues);

        expressionChecker.checkExpressions();

        System.out.println("Metric Details : ");
        System.out.println("Metric to Increase : " + expressionChecker.getMetricToIncrease()
                + ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceIncreaseMetric());

        System.out.println("Metric to Decrease : " + expressionChecker.getMetricToDecrease()
                + ", Difference of Expression with mean : " + expressionChecker.getMaxDifferenceDecreaseMetric());
        
        System.out.println();
        System.out.println();

    }

}
