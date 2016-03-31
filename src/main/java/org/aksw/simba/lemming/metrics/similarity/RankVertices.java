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

/**
 * Finds the best (from PageRank) and most representative (the final set of vertices must include all colours) 
 * vertices of the graph 
 * @author jsaveta
 */
public class RankVertices {
    private ObjectObjectOpenHashMap<Integer, BitSet> sampleGVertices;
    private ObjectObjectOpenHashMap<Integer, BitSet> sampleRVertices;
    private ObjectObjectOpenHashMap<BitSet,IntSet> gVertices;
    private ObjectObjectOpenHashMap<BitSet,IntSet> rVertices;
    private PageRank gPageRank;
    private PageRank rPageRank;
    
    public RankVertices(){}
    

    public void compute(ColouredGraph realGraph, ColouredGraph generatedGraph){ //be careful with the range of the arguments
        
        sampleGVertices = new ObjectObjectOpenHashMap<Integer, BitSet>();
        sampleRVertices = new ObjectObjectOpenHashMap<Integer, BitSet>();
        
        ColouredVerticesMetric colouredVertices = new ColouredVerticesMetric();
        rVertices = colouredVertices.getVerticesForEachColour(realGraph);
        gVertices = colouredVertices.getVerticesForEachColour(generatedGraph);
        
        Random random = new Random();
        gPageRank = generatedGraph.getGraph().getPageRanking(random);
        rPageRank = realGraph.getGraph().getPageRanking(random);
        
        //we suppose that a colour that do not exist in the real graph CANNOT exist in the generated 
        //for each colour of the generated graph keep top-k vertices using pagerank
        for (ObjectCursor<BitSet> colour : gVertices.keys()) {
        
            //I am not keeping the {} colour that is the class type @TODO: check
            if(!colour.value.isEmpty()){ 
                //generated graph
                Map<Integer, Double> verticesGPageRank = new HashMap<Integer, Double>();
                for (int i = 0; i < gVertices.get(colour.value).size(); i++) {
                    verticesGPageRank.put(gVertices.get(colour.value).toArray()[i], gPageRank.getRank(gVertices.get(colour.value).toArray()[i]));
                }
                //sort vertices based on pagerank and keep only a part of them
                Map<Integer, Double> sortedVerticesGPageRank = sortByValues(verticesGPageRank);
                //numOfSampleVertices keeps only a part of the vertices
                for(Map.Entry entry: sortedVerticesGPageRank.entrySet()){ 
                    sampleGVertices.put((Integer) entry.getKey(), colour.value);
                }

                //real graph
                Map<Integer, Double> verticesRPageRank = new HashMap<Integer, Double>();
                //@TODO: REMOVE IF CONDITION LATER, in reality we know that this is true! 
                if(rVertices.containsKey(colour.value)){ 
                    for (int i = 0; i < rVertices.get(colour.value).size(); i++) {    
                       verticesRPageRank.put(rVertices.get(colour.value).toArray()[i], rPageRank.getRank(rVertices.get(colour.value).toArray()[i]));
                    }
                }
                //sort vertices based on pagerank and keep only a part of them
                Map<Integer, Double> sortedVerticesRPageRank = sortByValues(verticesRPageRank);
                //numOfSampleVertices keeps only a part of the vertices
                for(Map.Entry entry: sortedVerticesRPageRank.entrySet()){ 
                    sampleRVertices.put((Integer) entry.getKey(), colour.value);
                }
            }
        }
        //System.out.println(toString());
    }

    public ObjectObjectOpenHashMap<Integer, BitSet> getSampleGeneratedVertices(){
        return this.sampleGVertices;
    }
    
    public ObjectObjectOpenHashMap<Integer, BitSet> getSampleRealVertices(){
        return this.sampleRVertices;
    }
  
    public int numOfSampleVertices(int size){
        //compute here how many vertices you want to keep for the sample graph for each colour
        //@TODO find the "correct" num of vertices
        //maybe a metric based on size of the vertices for each colour and the whole graph?
        int keep = (int) Math.ceil(size/10.0); 
        return keep;
    }
 
    public <K extends Comparable,V extends Comparable> Map<K,V> sortByValues(Map<K,V> map){
        List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {
            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return o2.getValue().compareTo(o1.getValue()); //descending order
            }
        });
      
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();      
        //keeps only the topK vertices ! 
        for(Map.Entry<K,V> entry: entries.subList(0, numOfSampleVertices(entries.size()))){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ObjectCursor<BitSet> colour : gVertices.keys()) {
            builder.append("\nColour: ");
            builder.append(colour.value);
            builder.append("\n******Generated graph******");
            for (int i = 0; i < gVertices.get(colour.value).size(); i++) {
                builder.append("\nVertex: ").append(gVertices.get(colour.value).toArray()[i]);
                builder.append(" PageRank: ").append(gPageRank.getRank(gVertices.get(colour.value).toArray()[i]));
            }
            
            builder.append("\n******Real graph******");
            for (int i = 0; i < rVertices.get(colour.value).size(); i++) {
                builder.append("\nVertex: ").append(rVertices.get(colour.value).toArray()[i]);
                builder.append(" PageRank: ").append(rPageRank.getRank(rVertices.get(colour.value).toArray()[i]));
            }
        }
        return builder.toString();
    }
}

