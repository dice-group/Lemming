package org.aksw.simba.lemming.metrics.dist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

public class LiteralProcessor {

	private Map<BitSet, Set<String>> mWordCollectionOfEachDTEColour;
	private ObjectDoubleOpenHashMap<BitSet> mAvrgNoOfWordsPerDTEdgeColour;
	
	/**
	 * Constructor
	 * 
	 * @param origGrphs an array of the original RDF data graphs
	 */
	public LiteralProcessor(ColouredGraph[] origGrphs){
		// initialize member variables
		mWordCollectionOfEachDTEColour = new HashMap<BitSet, Set<String>>();
		mAvrgNoOfWordsPerDTEdgeColour = new ObjectDoubleOpenHashMap<BitSet>();
		
		analize(origGrphs);
	}
	
	/**
	 * get the map of data typed edge's colours to the corresponding set of words
	 * @return a map
	 */
	public Map<BitSet, Set<String>> getWordsOfEachDTEColour(){
		return mWordCollectionOfEachDTEColour;
	}
	
	/**
	 * get a set of words associated to the data typed edge's colour
	 * @param dteColo
	 * @return a set of words
	 */
	public Set<String> getWords(BitSet dteColo){
		Set<String> res = new HashSet<String>();
		if(mWordCollectionOfEachDTEColour.containsKey(dteColo)){
			return mWordCollectionOfEachDTEColour.get(dteColo);
		}
		return res;
	}
	
	/**
	 * get the average number of words that a data typed edge's can hae
	 * @param dteColo the data typed edge's colour
	 * 
	 * @return the average number of words
	 */
	public double getAvrgNoOfWords(BitSet dteColo){
		if(mAvrgNoOfWordsPerDTEdgeColour.containsKey(dteColo)){
			return mAvrgNoOfWordsPerDTEdgeColour.get(dteColo);
		}
		return 0;
	}
	
	/**
	 * analyze and collect literals in the original RDF data graph  
	 * @param origGrphs
	 */
	private void analize(ColouredGraph [] origGrphs){
		
		ObjectIntOpenHashMap<BitSet> mapAppearTimesOfDTEColours = new ObjectIntOpenHashMap<BitSet>();
		
		for(ColouredGraph grph : origGrphs){
			// map of data typed properties to literals
			Map<BitSet, Set<String>> mapDTEdgeColoursToLiterals = grph
					.getMapDTEdgeColoursToLiterals();
			
			Set<BitSet> setOfDTEColours = mapDTEdgeColoursToLiterals.keySet();
			for(BitSet dteColo  : setOfDTEColours){
				Set<String> setOfLiterals = mapDTEdgeColoursToLiterals.get(dteColo);
				if(setOfLiterals != null && setOfLiterals.size() > 0){
					mapAppearTimesOfDTEColours.putOrAdd(dteColo, 1, 1);
					int totalNoOfWords = 0;
					
					for(String literal: setOfLiterals){
						String[] arrWords = literal.split(" ");
						totalNoOfWords += arrWords.length;
						
						Set<String> setWords = mWordCollectionOfEachDTEColour.get(dteColo);
						if(setWords == null){
							setWords = new HashSet<String>();
							mWordCollectionOfEachDTEColour.put(dteColo, setWords);
						}
						
						for(String word: arrWords){
							setWords.add(word);
						}
					}
					// average words of a literal
					double avrgNoOfWordsPerLiteral = totalNoOfWords/setOfLiterals.size();
					mAvrgNoOfWordsPerDTEdgeColour.putOrAdd(dteColo, avrgNoOfWordsPerLiteral, avrgNoOfWordsPerLiteral);
				}
			}
		}
		
		/*
		 *  compute average words of a literal associated with a specific data
		 *  typed property over all graphs
		 */
		Object[] arrDTEColours = mapAppearTimesOfDTEColours.keys;
		for(int i = 0 ; i< arrDTEColours.length ; i++){
			if(mapAppearTimesOfDTEColours.allocated[i]){
				BitSet dteColo = (BitSet)arrDTEColours[i];
				
				int noOfAppearTimes = mapAppearTimesOfDTEColours.get(dteColo);
				
				double avrgWordsPerLiteral = mAvrgNoOfWordsPerDTEdgeColour.get(dteColo);
				if(noOfAppearTimes != 0)
					mAvrgNoOfWordsPerDTEdgeColour.put(dteColo, avrgWordsPerLiteral/noOfAppearTimes);
				else
					mAvrgNoOfWordsPerDTEdgeColour.put(dteColo, 0);
			}
		}
	}
}
