package org.aksw.simba.lemming.metrics.similarity;

import java.util.HashMap;
import java.util.Map;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;

/**
 * @author jsaveta
 * Algorithm implemented in Java from @author Sashika on 8/26/2014 and transformed from @author jsaveta
 * to support instances of ColouredGraph.
 * Publication: Neighbor Similarity of Graphs and their Nodes by Neighbor Matching (author: Mladen Nikolic)
 * Compute Neighbor Matching Similarity between pairs of nodes and combine them to compute a final graph similarity.
 */

public class NMSimilarity {
    private ColouredGraph colouredGraphA;
    private ColouredGraph colouredGraphB;
    private int[][] inNodeArrayA;
    private int[][] outNodeArrayA;
    private int[][] inNodeArrayB;
    private int[][] outNodeArrayB;
    
    private double[][] nodeSimilarity;
    private double[][] inNodeSimilarity;
    private double[][] outNodeSimilarity;
    
    private double epsilon;
    
    private int graphSizeA;
    private int graphSizeB;
    private NumberOfVerticesMetric size;
    
    public NMSimilarity(ColouredGraph colouredGraphA, ColouredGraph colouredGraphB, Double epsilon) {
        try {
            this.colouredGraphA = colouredGraphA;
            this.colouredGraphB = colouredGraphB;
            
            this.size = new NumberOfVerticesMetric();
            this.graphSizeA = (int) size.apply(this.colouredGraphA);
            this.graphSizeB = (int) size.apply(this.colouredGraphB);

            this.epsilon = epsilon;
            
            this.inNodeArrayA  = colouredGraphA.getInNeighborhoodsArray();
            this.outNodeArrayA = colouredGraphA.getOutNeighborhoodsArray();
            
            this.inNodeArrayB = colouredGraphB.getInNeighborhoodsArray();
            this.outNodeArrayB = colouredGraphB.getOutNeighborhoodsArray();

                        
            this.nodeSimilarity = new double[graphSizeA][graphSizeB];
            this.inNodeSimilarity = new double[graphSizeA][graphSizeB];
            this.outNodeSimilarity = new double[graphSizeA][graphSizeB];

            
            initializeSimilarityMatrices();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeSimilarityMatrices() {
        for (int i = 0; i < graphSizeA; i++) {
            for (int j = 0; j < graphSizeB; j++) {
                Double maxDegree = Double.valueOf(Math.max(inNodeArrayA[i].length, inNodeArrayB[j].length));
                if (maxDegree != 0) {
                    inNodeSimilarity[i][j] = ((Math.min(inNodeArrayA[i].length, inNodeArrayB[j].length)) / (maxDegree));
                } else {
                    inNodeSimilarity[i][j] = 0d;
                }

                maxDegree = Double.valueOf(Math.max(outNodeArrayA[i].length, outNodeArrayB[j].length));
                if (maxDegree != 0) {
                    outNodeSimilarity[i][j] = ((Math.min(outNodeArrayA[i].length, outNodeArrayB[j].length)) / (maxDegree));
                } else {
                    outNodeSimilarity[i][j] = 0d;
                }
            }
        }

        for (int i = 0; i < graphSizeA; i++) {
            for (int j = 0; j < graphSizeB; j++) {
                nodeSimilarity[i][j] = (inNodeSimilarity[i][j] + outNodeSimilarity[i][j]) / 2;
            }
        }
    }

   public void measureSimilarity() {
        double maxDifference = 0.0;
        boolean terminate = false;

        while (!terminate) {
            maxDifference = 0.0;
            for (int i = 0; i < graphSizeA; i++) {
                for (int j = 0; j < graphSizeB; j++) {
                    //calculate in-degree similarities
                    double similaritySum = 0.0;
                    double maxDegree = Math.max(inNodeArrayA[i].length, inNodeArrayB[j].length);
                    int minDegree = Math.min(inNodeArrayA[i].length, inNodeArrayB[j].length);
                    if (minDegree == inNodeArrayA[i].length) {
                        similaritySum = enumerationFunction(inNodeArrayA[i], inNodeArrayB[j], 0);                    
                    } else {
                        similaritySum = enumerationFunction(inNodeArrayB[j], inNodeArrayA[i], 1);
                    }
                    if (maxDegree == 0.0 && similaritySum == 0.0) {
                        inNodeSimilarity[i][j] = 1.0;
                    } else if (maxDegree == 0.0) {
                        inNodeSimilarity[i][j] = 0.0;
                    } else {
                        inNodeSimilarity[i][j] = similaritySum / maxDegree;
                    }

                    //calculate out-degree similarities
                    similaritySum = 0.0;
                    maxDegree = Math.max(outNodeArrayA[i].length, outNodeArrayB[j].length);
                    minDegree = Math.min(outNodeArrayA[i].length, outNodeArrayB[j].length);
                    if (minDegree == outNodeArrayA[i].length) {
                        similaritySum = enumerationFunction(outNodeArrayA[i], outNodeArrayB[j], 0);
                    } else {
                        similaritySum = enumerationFunction(outNodeArrayB[j], outNodeArrayA[i], 1);
                    }
                    if (maxDegree == 0.0 && similaritySum == 0.0) {
                        outNodeSimilarity[i][j] = 1.0;
                    } else if (maxDegree == 0.0) {
                        outNodeSimilarity[i][j] = 0.0;
                    } else {
                        outNodeSimilarity[i][j] = similaritySum / maxDegree;
                    }

                }
            }

            for (int i = 0; i < graphSizeA; i++) {
                for (int j = 0; j < graphSizeB; j++) {
                    double temp = (inNodeSimilarity[i][j] + outNodeSimilarity[i][j]) / 2;
                    if (Math.abs(nodeSimilarity[i][j] - temp) > maxDifference) {
                        maxDifference = Math.abs(nodeSimilarity[i][j] - temp);                    
                    }
                    nodeSimilarity[i][j] = temp;
                }
            }

            if (maxDifference < epsilon) {
                terminate = true;
            }
        }
    }


     public double enumerationFunction(int[] neighborListMin, int[] neighborListMax, int graph) {
        double similaritySum = 0.0;
        Map<Integer, Double> valueMap = new HashMap<Integer, Double>();
        if (graph == 0) {
            for (int i = 0; i < neighborListMin.length; i++) {
                int node = neighborListMin[i];
                double max = 0.0;
                int maxIndex = -1;
                for (int j = 0; j < neighborListMax.length; j++) {
                    int key = neighborListMax[j];
                    if (!valueMap.containsKey(key)) {
                        if (max < nodeSimilarity[node][key]) {
                            max = nodeSimilarity[node][key];
                            maxIndex = key;
                        }
                    }
                }
                valueMap.put(maxIndex, max);
            }
        } else {
            for (int i = 0; i < neighborListMin.length; i++) {
                int node = neighborListMin[i];
                double max = 0.0;
                int maxIndex = -1;
                for (int j = 0; j < neighborListMax.length; j++) {
                    int key = neighborListMax[j];
                    if (!valueMap.containsKey(key)) {
                        if (max < nodeSimilarity[key][node]) {
                            max = nodeSimilarity[key][node];
                            maxIndex = key;
                        }
                    }
                }
                valueMap.put(maxIndex, max);
            }
        }

        for (double value : valueMap.values()) {
            similaritySum += value;
        }
        return similaritySum;
    }

    public Double getGraphSimilarity() {
        Double finalGraphSimilarity = 0.0;
        measureSimilarity();

        if (this.size.apply(this.colouredGraphA) < this.size.apply(this.colouredGraphB)) {
            finalGraphSimilarity = enumerationFunction(this.colouredGraphA.getVertices().toIntArray(), this.colouredGraphB.getVertices().toIntArray(), 0) / this.size.apply(this.colouredGraphA);
        } else {
            finalGraphSimilarity = enumerationFunction(this.colouredGraphB.getVertices().toIntArray(), this.colouredGraphA.getVertices().toIntArray(), 1) / this.size.apply(this.colouredGraphB);
        }
        finalGraphSimilarity = finalGraphSimilarity*100;
        return finalGraphSimilarity;
    }
      
}
