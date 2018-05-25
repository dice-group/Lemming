package org.aksw.simba.lemming.metrics.dist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class LiteralProcessor {

	private Map<BitSet, Set<String>> mWordCollectionOfEachDTEColour;
	private ObjectDoubleOpenHashMap<BitSet> mAvrgNoOfWordsPerDTEdgeColour;
	
	
	public LiteralProcessor(ColouredGraph[] origGrphs){
		// initialize member variables
		mWordCollectionOfEachDTEColour = new HashMap<BitSet, Set<String>>();
		mAvrgNoOfWordsPerDTEdgeColour = new ObjectDoubleOpenHashMap<BitSet>();
		
		analize(origGrphs);
	}
	
	public Set<String> getWords(BitSet dteColo){
		Set<String> res = new HashSet<String>();
		if(mWordCollectionOfEachDTEColour.containsKey(dteColo)){
			return mWordCollectionOfEachDTEColour.get(dteColo);
		}
		return res;
	}
	
	public double getAvrgNoOfWords(BitSet dteColo){
		if(mAvrgNoOfWordsPerDTEdgeColour.containsKey(dteColo)){
			return mAvrgNoOfWordsPerDTEdgeColour.get(dteColo);
		}
		return 0;
	}
	
	private void analize(ColouredGraph [] origGrphs){
		
		ObjectIntOpenHashMap<BitSet> mapAppearTimesOfDTEColours = new ObjectIntOpenHashMap<BitSet>();
		
		for(ColouredGraph grph : origGrphs){
			// map of data typed properties to literals
			ObjectObjectOpenHashMap<BitSet, Set<String>> mapDTEdgeColoursToLiterals = grph
					.getMapDTEdgeColoursToLiterals();
			
			Object[] arrDTEColours = mapDTEdgeColoursToLiterals.keys;
			for(int i = 0 ; i< arrDTEColours.length ; i++){
				if(mapDTEdgeColoursToLiterals.allocated[i]){
					BitSet dteColo  = (BitSet) arrDTEColours[i];
					Set<String> setLiterals = mapDTEdgeColoursToLiterals.get(dteColo);
					
					if(setLiterals != null && setLiterals.size() > 0){
						mapAppearTimesOfDTEColours.putOrAdd(dteColo, 1, 1);
						int totalNoOfWords = 0;
						
						for(String literal: setLiterals){
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
						double avrgNoOfWordsPerLiteral = totalNoOfWords/setLiterals.size();
						mAvrgNoOfWordsPerDTEdgeColour.putOrAdd(dteColo, avrgNoOfWordsPerLiteral, avrgNoOfWordsPerLiteral);
					}
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
