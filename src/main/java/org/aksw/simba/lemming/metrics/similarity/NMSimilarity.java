package org.aksw.simba.lemming.metrics.similarity;

import java.util.HashMap;
import java.util.Map;
import org.aksw.simba.lemming.ColouredGraph;

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
    private Double[][] nodeSimilarity;
    private Double[][] inNodeSimilarity;
    private Double[][] outNodeSimilarity;
    private Double epsilon;
    private int graphSizeA;
    private int graphSizeB;

    public NMSimilarity(ColouredGraph colouredGraphA, ColouredGraph colouredGraphB, Double epsilon) {
        try {
            this.colouredGraphA = colouredGraphA;
            this.colouredGraphB = colouredGraphB;
            this.epsilon = epsilon;
            this.inNodeArrayA = colouredGraphA.getInDegreeNodeArray();
            this.outNodeArrayA = colouredGraphA.getOutDegreeNodeArray();
            
//            System.out.println("inNodeArrayA: " + Arrays.deepToString(inNodeArrayA));
//            System.out.println("outNodeArrayA: " + Arrays.deepToString(outNodeArrayA));
            
            this.inNodeArrayB = colouredGraphB.getInDegreeNodeArray();
            this.outNodeArrayB = colouredGraphB.getOutDegreeNodeArray();

            this.graphSizeA = colouredGraphA.getGraphSize();
            this.graphSizeB = colouredGraphB.getGraphSize();

            this.nodeSimilarity = new Double[graphSizeA][graphSizeB];
            this.inNodeSimilarity = new Double[graphSizeA][graphSizeB];
            this.outNodeSimilarity = new Double[graphSizeA][graphSizeB];

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
                    inNodeSimilarity[i][j] = Double.valueOf(0);
                }

                maxDegree = Double.valueOf(Math.max(outNodeArrayA[i].length, outNodeArrayB[j].length));
                if (maxDegree != 0) {
                    outNodeSimilarity[i][j] = ((Math.min(outNodeArrayA[i].length, outNodeArrayB[j].length)) / (maxDegree));
                } else {
                    outNodeSimilarity[i][j] = Double.valueOf(0);
                }
            }
        }

        for (int i = 0; i < graphSizeA; i++) {
            for (int j = 0; j < graphSizeB; j++) {
                //System.out.print(inNodeSimilarity[i][j] + " ");
                nodeSimilarity[i][j] = (inNodeSimilarity[i][j] + outNodeSimilarity[i][j]) / 2;
            }
            //System.out.println();
        }
//        System.out.println();
//        for (int i = 0; i < graphSizeA; i++) {
//            for (int j = 0; j < graphSizeB; j++) {
//                System.out.print(outNodeSimilarity[i][j]+" ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//        for (int i = 0; i < graphSizeA; i++) {
//            for (int j = 0; j < graphSizeB; j++) {
//                System.out.print(nodeSimilarity[i][j]+" ");
//            }
//            System.out.println();
//        }

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
                    double maxDegree = Double.valueOf(Math.max(inNodeArrayA[i].length, inNodeArrayB[j].length));
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
                    maxDegree = Double.valueOf(Math.max(outNodeArrayA[i].length, outNodeArrayB[j].length));
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
//        for (int i = 0; i < graphSizeA; i++) {
//            for (int j = 0; j < graphSizeB; j++) {
//                System.out.print(nodeSimilarity[i][j] + " ");
//            }
//            System.out.println("");
//        }
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

        if (colouredGraphA.getGraphSize() < colouredGraphB.getGraphSize()) {
            finalGraphSimilarity = enumerationFunction(colouredGraphA.getNodeArray(), colouredGraphB.getNodeArray(), 0) / colouredGraphA.getGraphSize();
        } else {
            finalGraphSimilarity = enumerationFunction(colouredGraphB.getNodeArray(), colouredGraphA.getNodeArray(), 1) / colouredGraphB.getGraphSize();
        }
        finalGraphSimilarity = finalGraphSimilarity*100;
        return finalGraphSimilarity;
    }
}
