package org.aksw.simba.lemming.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.simba.lemming.metrics.dist.IntDistribution;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.apache.jena.ext.com.google.common.primitives.Doubles;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;


/**
 *
 * @author jsaveta
 */
public class MapUtil {
    
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
	public static <T, E> ObjectDistribution<T> converta(
			ObjectDoubleOpenHashMap<T> sourceData) {

		if (sourceData != null) {
			int iLength = sourceData.allocated.length;
			T[] samples = (T[]) new Object[sourceData.assigned];
			double[] values = new double[sourceData.assigned];
			int ipos = 0;
			for (int i = 0; i < iLength; ++i) {
				if (sourceData.allocated[i]) {
					samples[ipos] = (T) ((Object[]) sourceData.keys)[i];
					values[ipos] = (double) (sourceData.values)[i];
					ipos++;
				}
			}
			return new ObjectDistribution<T>(samples, values);
		}
		return null;
	}
    
    public static Set<BitSet> convert(List<BitSet> lstItemColours){
    	Set<BitSet> res = new HashSet<BitSet>();
    	if(lstItemColours != null){
   			res.addAll(lstItemColours);
    	}
    	return res;
    }
    
    public static List<BitSet> keysToList(ObjectDoubleOpenHashMap<BitSet> mapDoubleKeyValue){
    	if(mapDoubleKeyValue != null){
    		List<BitSet> res = new ArrayList<BitSet>();
    		Object[] keySamples = mapDoubleKeyValue.keys;
    		int iNoOfSamples = keySamples.length;
    		for(int i = 0 ; i< iNoOfSamples ; ++i){
    			if(mapDoubleKeyValue.allocated[i]){
    				BitSet key = (BitSet) keySamples[i];
    				res.add(key);
    			}
    		}
    		return res;
    	}
    	return null; 
    }
    
    public static Set<Integer> convert(IntSet setInt){
    	Set<Integer> setRes = new HashSet<Integer>();
    	if(setInt!=null && setInt.size() > 0){
    		int[] arrInt = setInt.toIntArray();
    		for (int i : arrInt){
    			setRes.add(i);
    		}
    	}
    	return setRes;
    }
    
    public static Set<BitSet> keysToSet(ObjectDoubleOpenHashMap<BitSet> mapDoubleKeyValue){
    	if(mapDoubleKeyValue != null){
    		Set<BitSet> res = new HashSet<BitSet>();
    		Object[] keySamples = mapDoubleKeyValue.keys;
    		int iNoOfSamples = keySamples.length;
    		for(int i = 0 ; i< iNoOfSamples ; ++i){
    			if(mapDoubleKeyValue.allocated[i]){
    				BitSet key = (BitSet) keySamples[i];
    				res.add(key);
    			}
    		}
    		return res;
    	}
    	return null; 
    }
    
    public static ObjectDoubleOpenHashMap<BitSet> convert(ObjectDistribution<BitSet> sourceData){
    	if(sourceData != null){
    		ObjectDoubleOpenHashMap<BitSet> mapDoubleDist = new ObjectDoubleOpenHashMap<BitSet>();
    		
    		BitSet[] sampleSpace = sourceData.sampleSpace;
    		double[] sampleValues = sourceData.values;
    		int iNoOfSamples = sampleSpace.length;
    		for(int i =0 ; i < iNoOfSamples; i++){
    			mapDoubleDist.putOrAdd(sampleSpace[i], sampleValues[i],sampleValues[i]);
    		}
    		return mapDoubleDist;
    	}
    	return null;
    }
    
    public static ObjectDistribution<BitSet> convert(ObjectDoubleOpenHashMap<BitSet> sourceData){
    	
    	if(sourceData != null){
    		int iLength = sourceData.allocated.length;
    		BitSet[] samples = new BitSet[sourceData.assigned];
    		double [] values = new double[sourceData.assigned];
    		int ipos = 0 ;
    		for(int i = 0 ; i< iLength; ++i){
    			if(sourceData.allocated[i]){
    				samples[ipos] = (BitSet)((Object[])sourceData.keys)[i];
    				values[ipos] = (double)(sourceData.values)[i];
    				ipos++;
    			}
    		}
    		
    		return new ObjectDistribution<BitSet>(samples, values);
    	}
    	return null;
    }
    
    public static ObjectDistribution<BitSet> convert( ObjectObjectOpenHashMap<BitSet, IntSet> mapVertWithIDs){
    	if(mapVertWithIDs != null){
    		int iLength = mapVertWithIDs.allocated.length;
    		BitSet[] samples = new BitSet[mapVertWithIDs.assigned];
    		double [] values = new double[mapVertWithIDs.assigned];
    		int ipos = 0 ;
    		for(int i = 0 ; i< iLength; ++i){
    			if(mapVertWithIDs.allocated[i]){
    				BitSet vertColo = (BitSet)((Object[])mapVertWithIDs.keys)[i]; 
    				samples[ipos] = vertColo;
    				IntSet setIDs = mapVertWithIDs.get(vertColo);
    				values[ipos] = (double)(setIDs.size());
    				ipos++;
    			}
    		}
    		return new ObjectDistribution<BitSet>(samples, values);
    	}
    	return null;
    }
    
    public static void convert(ObjectDistribution<BitSet> sourceData, List<BitSet> outLstDataSamples, List<Double> outLstDataValues){
    	if(sourceData!= null){
    		outLstDataSamples = Arrays.asList(sourceData.getSampleSpace());
    		outLstDataValues = Doubles.asList(sourceData.getValues());
    	}else{
    		if(outLstDataSamples == null){
        		outLstDataSamples = new ArrayList<BitSet>();
        	}
        	
        	if(outLstDataValues == null){
        		outLstDataValues = new ArrayList<Double>();
        	}	
    	}
    }
    
    public static void convert(ObjectIntOpenHashMap<BitSet> sourceData, List<BitSet> outLstDataSamples, List<Double> outLstDataValues){
    	if( outLstDataSamples ==null )
    		outLstDataSamples = new ArrayList<BitSet>();
    	
		if( outLstDataValues == null)
			outLstDataValues = new ArrayList<Double>();

		int iLengthOfVertColoDist = sourceData.allocated.length;
		for (int i = 0; i < iLengthOfVertColoDist; ++i) {
			if (sourceData.allocated[i]) {
				outLstDataSamples
						.add((BitSet) ((Object[]) sourceData.keys)[i]);
				outLstDataValues
						.add((double) sourceData.values[i]);
			}
		}
    }
    
    public static ObjectDistribution<BitSet> convert(ObjectIntOpenHashMap<BitSet> sourceData){
    	if(sourceData != null){
    		int iLength = sourceData.allocated.length;
    		BitSet[] samples = new BitSet[sourceData.assigned];
    		double [] values = new double[sourceData.assigned];
    		int ipos = 0 ;
    		for(int i = 0 ; i< iLength; ++i){
    			if(sourceData.allocated[i]){
    				samples[ipos] = (BitSet)((Object[])sourceData.keys)[i];
    				values[ipos] = (double)(sourceData.values)[i];
    				ipos++;
    			}
    		}
    		
    		return new ObjectDistribution<BitSet>(samples, values);
    	}
    	return null;
    }
    
    public static void printMapContent(Map<BitSet,ObjectDistribution<BitSet>> inMap, String nameOfMap){
    	Set<BitSet> keySets = inMap.keySet();
    	for(BitSet key : keySets){
    		ObjectDistribution<BitSet> eObj = inMap.get(key);
    		BitSet[] sampleSpaces = eObj.getSampleSpace();
    		double[] sampleValues = eObj.getValues();
    		
    		System.out.println( nameOfMap + " colour: " + key);
    		System.out.println("\t(sample, value)");
    		System.out.print("\t");
    		int iSize = sampleSpaces.length;
    		for(int i = 0 ; i< iSize ; i++){
    			System.out.print("("+sampleSpaces[i] +", "+sampleValues[i]+ "); ");
    		}
    		System.out.println();
    	}
    }
    
    public static void printMapContent(Map<BitSet, IntDistribution>inMap, String nameOfMap, int sizeOfDistribution){
    	Set<BitSet> keySets = inMap.keySet();
    	for(BitSet key: keySets){
    		IntDistribution intDist = inMap.get(key);
    		int[] sampleSpaces = intDist.getSampleSpace();
    		double[] sampleValues = intDist.getValues();
    		
    		System.out.println( nameOfMap + " colour: " + key);
    		System.out.println("\t(sample, value)");
    		System.out.print("\t");
    		int iSize = sampleSpaces.length;
    		for(int i = 0 ; i< iSize ; i++){
    			System.out.print("("+sampleSpaces[i] +", "+ sampleValues[i]/(sizeOfDistribution *1.0)+ "); ");
    		}
    		System.out.println();
    	}
    }
    
}
