package org.aksw.simba.lemming.metrics.similarity;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import grph.algo.distance.PageRank;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.multi.ColouredVerticesMetric;
import org.aksw.simba.lemming.metrics.single.NumberOfVerticesMetric;

/**
 * Finds the best (from PageRank) and most representative (the final set of vertices must include all colours) 
 * vertices of the graph 
 * @author jsaveta
 */
public class Sampling {
    private ObjectObjectOpenHashMap<Integer, BitSet> sampleGenVertices;
    private ObjectObjectOpenHashMap<Integer, BitSet> sampleRealVertices;
    private Random random;
    
    public Sampling(){}
    

    public void compute(ColouredGraph realGraph, ColouredGraph generatedGraph){ 
        random = new Random();
        sampleRealVertices = vertexSample(realGraph);
        sampleGenVertices = vertexSample(generatedGraph);

    }

    /**
     * This method computes the pagerank for each vertex and keeps a sample of 
     * the vertices for each colour
     * @param graph
     * @return sample graph
     */
    public ObjectObjectOpenHashMap<Integer, BitSet> vertexSample(ColouredGraph graph){

    ColouredVerticesMetric colouredVertices = new ColouredVerticesMetric();
    ObjectObjectOpenHashMap<BitSet,IntSet> vertices = colouredVertices.getVerticesForEachColour(graph);
    PageRank pageRank = graph.getGraph().getPageRanking(random);
    ObjectObjectOpenHashMap<Integer, BitSet> sampleVertices = new ObjectObjectOpenHashMap<Integer,BitSet>();
    
        for (ObjectCursor<BitSet> colour : vertices.keys()) {
            if(!colour.value.isEmpty()){  //change this based on the colour we have for rdf:type ...
                Map<Integer, Double> verticesPageRank = new HashMap<Integer, Double>();

                for (int i = 0; i < vertices.get(colour.value).size(); i++) {
                    verticesPageRank.put(vertices.get(colour.value).toArray()[i], pageRank.getRank(vertices.get(colour.value).toArray()[i]));
                }
                //sort vertices based on pagerank and keep only a part of them
                Map<Integer, Double> sortedVerticesPageRank = sortByValues(verticesPageRank,graph,colour.value);   
                    
                for(Map.Entry entry: sortedVerticesPageRank.entrySet()){ 
                    sampleVertices.put((Integer) entry.getKey(), colour.value);
                }
            }
        }
        return sampleVertices;
    }
  
    public <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map,ColouredGraph graph, BitSet colour){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {
            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return o2.getValue().compareTo(o1.getValue()); //descending order
            }
        });

        //keeps only the topK vertices ! 
        int size = numOfSampleVertices(graph, colour);
        if(size > entries.size()) size = entries.size();
        
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();  
        for(Map.Entry<K,V> entry: entries.subList(0, size)){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    
    public int numOfSampleVertices(ColouredGraph graph, BitSet colour){
        NumberOfVerticesMetric metric = new NumberOfVerticesMetric();
        double gSize = metric.apply(graph);
        ObjectObjectOpenHashMap<BitSet,Double> distribution = vertexColourDistribution(graph);
        
        //we choose ceil and not round because we want to keep at least one vertex for each colour
        int keep = (int) Math.ceil(distribution.get(colour) * (gSize * 0.15)); 
        
        return keep;
    }
    
    public ObjectObjectOpenHashMap<BitSet,Double> vertexColourDistribution(ColouredGraph graph){
        ObjectObjectOpenHashMap<BitSet,Double> distribution = new ObjectObjectOpenHashMap<BitSet,Double>();
        NumberOfVerticesMetric metric = new NumberOfVerticesMetric();
        double gSize = metric.apply(graph);
        
        ColouredVerticesMetric colouredVertices = new ColouredVerticesMetric();
        ObjectObjectOpenHashMap<BitSet, IntSet> map = colouredVertices.getVerticesForEachColour(graph);
        
        for (ObjectCursor<BitSet> colour : map.keys()) {
             distribution.put(colour.value, (map.get(colour.value).size()/gSize));
        }
        return distribution;
    }
 
    

    
    public ObjectObjectOpenHashMap<Integer, BitSet> getSampleGeneratedVertices(){
        return this.sampleGenVertices;
    }
    
    public ObjectObjectOpenHashMap<Integer, BitSet> getSampleRealVertices(){
        return this.sampleRealVertices;
    }
   
}

