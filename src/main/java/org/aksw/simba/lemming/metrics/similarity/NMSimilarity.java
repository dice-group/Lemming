package org.aksw.simba.lemming.metrics.similarity;

import java.util.ArrayList;
import java.util.Arrays;
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
//This Similarity is not relevant any more!!!
@Deprecated
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
    
    private  Map<Integer,Integer> idCorrespondenceA;
    private  Map<Integer,Integer> idCorrespondenceB;
    
    private double epsilon;
    
    private int graphSizeA;
    private int graphSizeB;
    private NumberOfVerticesMetric size;
    
   
    
    public NMSimilarity(ColouredGraph colouredGraphA, ColouredGraph colouredGraphB, Map<Integer,Integer> idCorrespondenceA, Map<Integer,Integer> idCorrespondenceB, Double epsilon) {
        try {
            this.colouredGraphA = colouredGraphA;
            this.colouredGraphB = colouredGraphB;
            this.idCorrespondenceA = idCorrespondenceA;
            this.idCorrespondenceB = idCorrespondenceB;
            
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
        
        double count = 0.0;
        double conditionedCount = 0.0;
        while (!terminate) {
            maxDifference = 0.0;
            for (int i = 0; i < graphSizeA; i++) {
                for (int j = 0; j < graphSizeB; j++) {
                    double maxDegreeIn = Math.max(inNodeArrayA[i].length, inNodeArrayB[j].length);
                    double maxDegreeOut = Math.max(outNodeArrayA[i].length, outNodeArrayB[j].length);  
                    
                    //checks if colour of vertexA and colour of vertexB intersect so the two vertices are comparable
                    if(colouredGraphA.getVertexColour(i).intersects(colouredGraphB.getVertexColour(j))){ 
                        //calculate in-degree similarities
                        double similaritySum = 0.0;
                        int minDegree = Math.min(inNodeArrayA[i].length, inNodeArrayB[j].length);
                        
                        
                        if (minDegree == inNodeArrayA[i].length) {
                            similaritySum = enumerationFunction(inNodeArrayA[i], inNodeArrayB[j], 0);                    
                        } else {
                            similaritySum = enumerationFunction(inNodeArrayB[j], inNodeArrayA[i], 1);
                        }
                        if (maxDegreeIn == 0.0 && similaritySum == 0.0) {
                            inNodeSimilarity[i][j] = 1.0;
                        } else if (maxDegreeIn == 0.0) {
                            inNodeSimilarity[i][j] = 0.0;
                        } else {
                            inNodeSimilarity[i][j] = similaritySum / maxDegreeIn;
                        }

                        //calculate out-degree similarities
                        similaritySum = 0.0;
                        minDegree = Math.min(outNodeArrayA[i].length, outNodeArrayB[j].length);
                        if (minDegree == outNodeArrayA[i].length) {
                            similaritySum = enumerationFunction(outNodeArrayA[i], outNodeArrayB[j], 0);
                        } else {
                            similaritySum = enumerationFunction(outNodeArrayB[j], outNodeArrayA[i], 1);
                        }
                        if (maxDegreeOut == 0.0 && similaritySum == 0.0) {
                            outNodeSimilarity[i][j] = 1.0;
                        } else if (maxDegreeOut == 0.0) {
                            outNodeSimilarity[i][j] = 0.0;
                        } else {
                            outNodeSimilarity[i][j] = similaritySum / maxDegreeOut;
                        }
                    conditionedCount++;
                    }
                    // {} colour
                    else if (colouredGraphA.getVertexColour(i).equals(colouredGraphB.getVertexColour(j))){
                        inNodeSimilarity[i][j] = 1.0;
                        outNodeSimilarity[i][j] = 1.0;
                    }
                    else{ //if are different colour the vertices are not comparable
                        inNodeSimilarity[i][j] = 0.0;
                        outNodeSimilarity[i][j] = 0.0;
                    }
                    count++;
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
        
        for(double[] row : nodeSimilarity) {
             System.out.println(Arrays.toString(row));
        }
        //System.out.println("conditionedCount " + conditionedCount);
        //System.out.println("count " + count);
        
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


    //1 node from Graph A ->n nodes from Graph B , similarity
    public Map< Map<Integer,ArrayList<Integer>>, Double > getNodeMapping(){

        Map< Map<Integer,ArrayList<Integer>>, Double > nodeMapWithSim = new HashMap< Map<Integer,ArrayList<Integer>>, Double > ();
        Map <Integer, ArrayList<Integer>> nodeMap = new HashMap<Integer, ArrayList<Integer>> ();
        //sample, initial
        if(this.idCorrespondenceA != null && this.idCorrespondenceB != null){
        for ( int i = 0; i < nodeSimilarity.length; i++ ){
                double maxr = 0.0;
                ArrayList<Integer> list = new ArrayList<Integer>();
        
                for ( int j = 0; j < nodeSimilarity[i].length; j++ ){
                    if ( nodeSimilarity [i][j] > maxr ){
                        maxr = nodeSimilarity[i][j];
                        list = new ArrayList<Integer>();
                        list.add(this.idCorrespondenceB.get(j));
                    }
                    else if ( nodeSimilarity [i][j] == maxr && maxr > 0.0 ){
                        list.add(this.idCorrespondenceB.get(j));
                    }
                    
                }
                nodeMap.put(this.idCorrespondenceA.get(i), list);
                nodeMapWithSim.put(nodeMap, maxr);
                nodeMap = new HashMap<Integer, ArrayList<Integer>> ();
            }

            //System.out.println("nodeMapWithSim " +nodeMapWithSim.toString());
        }
    return nodeMapWithSim;
    }

     
    public Double getGraphSimilarity() {
        Double finalGraphSimilarity = 0.0;
        measureSimilarity();
        getNodeMapping();

       //I am dividing with max size of the two graphs instead of min ! 
        if (this.size.apply(this.colouredGraphA) > this.size.apply(this.colouredGraphB)) {
            finalGraphSimilarity = enumerationFunction(this.colouredGraphA.getVertices().toIntArray(), this.colouredGraphB.getVertices().toIntArray(), 0) / this.size.apply(this.colouredGraphA);
        } else {
            finalGraphSimilarity = enumerationFunction(this.colouredGraphB.getVertices().toIntArray(), this.colouredGraphA.getVertices().toIntArray(), 1) / this.size.apply(this.colouredGraphB);
        }
        
        
        finalGraphSimilarity =  finalGraphSimilarity*100 ;
        return finalGraphSimilarity;
    }
      
}
