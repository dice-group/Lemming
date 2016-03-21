package org.aksw.simba.lemming.metrics.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<List<Integer>> inNodeListA;
    private List<List<Integer>> outNodeListA;
    private List<List<Integer>> inNodeListB;
    private List<List<Integer>> outNodeListB;
    
    private List<List<Double>> nodeSimilarity;
    private List<List<Double>> inNodeSimilarity;
    private List<List<Double>> outNodeSimilarity;
    private Double epsilon;
    private int graphSizeA;
    private int graphSizeB;
    
    public NMSimilarity(ColouredGraph colouredGraphA, ColouredGraph colouredGraphB, Double epsilon) {
        try {
            this.colouredGraphA = colouredGraphA;
            this.colouredGraphB = colouredGraphB;
            this.epsilon = epsilon;
            
            
            this.inNodeListA  = twoDArrayToList(colouredGraphA.getInDegreeNodeArray());
            this.outNodeListA = twoDArrayToList(colouredGraphA.getOutDegreeNodeArray());
            
            this.inNodeListB = twoDArrayToList(colouredGraphB.getInDegreeNodeArray());
            this.outNodeListB = twoDArrayToList(colouredGraphB.getOutDegreeNodeArray());

            this.graphSizeA = colouredGraphA.getGraphSize();
            this.graphSizeB = colouredGraphB.getGraphSize();

             
            this.nodeSimilarity = new ArrayList<List<Double>>();
            this.inNodeSimilarity = new ArrayList<List<Double>>();
            this.outNodeSimilarity = new ArrayList<List<Double>>();

            initializeSimilarityMatrices();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeSimilarityMatrices() {
        
        for (int i = 0; i < graphSizeA; i++) {
            inNodeSimilarity.add(new ArrayList<Double>());
            outNodeSimilarity.add(new ArrayList<Double>());
            nodeSimilarity.add(new ArrayList<Double>());
            
            for (int j = 0; j < graphSizeB; j++) {
                Double maxDegree = Double.valueOf(Math.max(inNodeListA.get(i).size(), inNodeListB.get(j).size()));
                if (maxDegree != 0) {
                    inNodeSimilarity.get(i).add(j,(Math.min(inNodeListA.get(i).size(), inNodeListB.get(j).size())) / (maxDegree)); 
                } else {
                    inNodeSimilarity.get(i).add(j, Double.valueOf(0));
                }

                maxDegree = Double.valueOf(Math.max(outNodeListA.get(i).size(), outNodeListB.get(j).size()));
                if (maxDegree != 0) {
                    outNodeSimilarity.get(i).add(j,((Math.min(outNodeListA.get(i).size(), outNodeListB.get(j).size())) / (maxDegree)));
                } else {
                    outNodeSimilarity.get(i).add(j,Double.valueOf(0));
                }
            }
        }

        for (int i = 0; i < graphSizeA; i++) {
            for (int j = 0; j < graphSizeB; j++) {
                nodeSimilarity.get(i).add(j,(inNodeSimilarity.get(i).get(j) + outNodeSimilarity.get(i).get(j)) / 2);
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
                    double maxDegree = Double.valueOf(Math.max(inNodeListA.get(i).size(), inNodeListB.get(j).size()));
                    int minDegree = Math.min(inNodeListA.get(i).size(), inNodeListB.get(j).size());
                    if (minDegree == inNodeListA.get(i).size()) {
                        similaritySum = enumerationFunction(inNodeListA.get(i), inNodeListB.get(j), 0);
                    } else {
                        similaritySum = enumerationFunction(inNodeListB.get(j), inNodeListA.get(i), 1);
                    }
                    if (maxDegree == 0.0 && similaritySum == 0.0) {
                        inNodeSimilarity.get(i).add(j, 1.0);
                    } else if (maxDegree == 0.0) {
                        inNodeSimilarity.get(i).add(j, 0.0);
                    } else {
                        inNodeSimilarity.get(i).add(j, similaritySum / maxDegree);
                    }

                    //calculate out-degree similarities
                    similaritySum = 0.0;
                    maxDegree = Double.valueOf(Math.max(outNodeListA.get(i).size(), outNodeListB.get(j).size()));
                    minDegree = Math.min(outNodeListA.get(i).size(), outNodeListB.get(j).size());
                    if (minDegree == outNodeListA.get(i).size()) {
                        similaritySum = enumerationFunction(outNodeListA.get(i), outNodeListB.get(j), 0);
                    } else {
                        similaritySum = enumerationFunction(outNodeListB.get(j), outNodeListA.get(i), 1);
                    }
                    if (maxDegree == 0.0 && similaritySum == 0.0) {
                        outNodeSimilarity.get(i).add(j, 1.0);
                    } else if (maxDegree == 0.0) {
                        outNodeSimilarity.get(i).add(j, 0.0);
                    } else {
                        outNodeSimilarity.get(i).add(j, similaritySum / maxDegree);
                    }

                }
            }

            for (int i = 0; i < graphSizeA; i++) {
                for (int j = 0; j < graphSizeB; j++) {
                    double temp = (inNodeSimilarity.get(i).get(j) + outNodeSimilarity.get(i).get(j)) / 2;
                    if (Math.abs((nodeSimilarity.get(i).get(j)) - temp) > maxDifference) {
                        maxDifference = Math.abs(nodeSimilarity.get(i).get(j) - temp);  
                    }
                    nodeSimilarity.get(i).add(j, temp);
                    
                    //I changed place to termination condition
                    if (maxDifference < epsilon) {
                        terminate = true;
                    }
                }
            }
        }
    }

    public double enumerationFunction(List<Integer> neighborListMin, List<Integer> neighborListMax, int graph) {
        double similaritySum = 0.0;
        Map<Integer, Double> valueMap = new HashMap<Integer, Double>();
        if (graph == 0) {
            for (int i = 0; i < neighborListMin.size(); i++) {
                int node = neighborListMin.get(i);
                double max = 0.0;
                int maxIndex = -1;
                for (int j = 0; j < neighborListMax.size(); j++) {
                    int key = neighborListMax.get(j);
                    if (!valueMap.containsKey(key)) {
                        if (max < nodeSimilarity.get(node).get(key)) {
                            max = nodeSimilarity.get(node).get(key);
                            maxIndex = key;
                        }
                    }
                }
                valueMap.put(maxIndex, max);
            }
        } else {
            for (int i = 0; i < neighborListMin.size(); i++) {
                int node = neighborListMin.get(i);
                double max = 0.0;
                int maxIndex = -1;
                for (int j = 0; j < neighborListMax.size(); j++) {
                    int key = neighborListMax.get(j);
                    if (!valueMap.containsKey(key)) {
                        if (max < nodeSimilarity.get(node).get(key)) {
                            max = nodeSimilarity.get(node).get(key);
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
            finalGraphSimilarity = enumerationFunction(colouredGraphA.getNodeList(), colouredGraphB.getNodeList(), 0) / colouredGraphA.getGraphSize();
        } else {
            finalGraphSimilarity = enumerationFunction(colouredGraphB.getNodeList(), colouredGraphA.getNodeList(), 1) / colouredGraphB.getGraphSize();
        }
        finalGraphSimilarity = finalGraphSimilarity*100;
        return finalGraphSimilarity;
    }
    
  
    //move this to util package
    public List<List<Integer>> twoDArrayToList(int[][] twoDArray) {
    
        List<List<Integer>> list  = new ArrayList<List<Integer>>();
        for (int i=0; i<twoDArray.length ; i++){
            list.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < twoDArray.length; i++) {
            for (int j = 0; j < twoDArray[i].length; j++) {
                 list.get(j).add(i);
            }
        }
        return list;
    }


    
}
