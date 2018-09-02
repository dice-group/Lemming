package org.aksw.simba.lemming.mimicgraph.literals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;


/**
 * LiteralAnalysis takes responsibility to collect data type of literals,
 * collect potential content of each literal, and perform analysis for some
 * statistic metrics such as literal distribution.
 * 
 * @author nptsy
 */
public class LiteralAnalysis {

	/* 
	 * map of literals associated with the datatype edge colour and tail colours
	 * 1st key: dteColo, 2nd key: tColo and value is a set of literals 
	 */
	
	private Map<BitSet, Map<BitSet, Set<String>>> mValuesOfEachDTEColour;
	
	/*
	 * a map of average length of literals for each dteColo associated with tColo 
	 * The length of a literal is the number of words the literal may have
	 * 
	 * 1st key is the tail colour, 2nd key is the dteColo and value is the average length
	 */
	private Map<BitSet, ObjectDoubleOpenHashMap<BitSet>> mAvrgNoOfWordsPerDTEdgeColour;
	
	/*
	 * map of literal and its potential type.
	 * One problem is: a literal may have more than one type.
	 */
	private Map<BitSet, String> mTypesOfDTEColours;
	
	/**
	 * Constructor
	 * 
	 * @param origGrphs an array of the original RDF data graphs
	 */
	public LiteralAnalysis(ColouredGraph[] origGrphs){
		// initialize member variables
		mValuesOfEachDTEColour = new HashMap<BitSet, Map<BitSet, Set<String>>>();
		mAvrgNoOfWordsPerDTEdgeColour = new HashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
		mTypesOfDTEColours = new HashMap<BitSet, String>();
		
		analyze(origGrphs);
	}
	
	public Map<String, Set<BitSet>> getMapOfTypesAndDTEColours(){
		Map<String, Set<BitSet>> mapOfTypesAndDTEColours = new HashMap<String, Set<BitSet>>();
		if(mTypesOfDTEColours != null && mTypesOfDTEColours.size() > 0){
			Set<BitSet> setOfDTEColours = mTypesOfDTEColours.keySet();
			for(BitSet dteColo : setOfDTEColours){
				String type = mTypesOfDTEColours.get(dteColo);
				
				
				Set<BitSet> setOfTmpDTEColours = mapOfTypesAndDTEColours.get(type);
				if(setOfTmpDTEColours == null){
					setOfTmpDTEColours = new HashSet<BitSet>();
					mapOfTypesAndDTEColours.put(type, setOfTmpDTEColours);
				}
				
				setOfTmpDTEColours.add(dteColo);
			}
		}
		return mapOfTypesAndDTEColours;
	}
	
	public Map<BitSet, Map<BitSet, Set<String>>> getMapOfDTEColoursAndValues(Set<BitSet> setOfDTEColours){
		if(setOfDTEColours!=null && setOfDTEColours.size() > 0){
			
			Map<BitSet, Map<BitSet, Set<String>>> mapSampleData = new HashMap<BitSet, Map<BitSet, Set<String>>>();
			for(BitSet dteColo: setOfDTEColours){
				Map<BitSet, Set<String>> mapOfVColoAndSetValues = mValuesOfEachDTEColour.get(dteColo);
				if(mapOfVColoAndSetValues!=null){
					mapSampleData.put(dteColo, mapOfVColoAndSetValues);
				}
			}
			return mapSampleData;
		}
		return null;
	}
	
	/**
	 * analyze and collect literals in the original RDF data graph  
	 * @param origGrphs
	 */
	private void analyze(ColouredGraph [] origGrphs){
		
		Map<BitSet, ObjectIntOpenHashMap<BitSet>> mapAppearTimesOfDTEColoursOverTColo = new HashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
		
		//------------------------------------------------------
		//collect type of literal first then collect literal
		//------------------------------------------------------
		
		
		//Collect type of literals
		for(ColouredGraph grph: origGrphs){
			// map of datatype edge colours to tail colours with their literals
			Map<BitSet, Map<BitSet, Set<String>>> mapDTEdgeColoursToLiterals = grph.getMapLiterals();
			
			// set of datatype edge colours
			Set<BitSet> setOfDTEColours = mapDTEdgeColoursToLiterals.keySet();
			
			for(BitSet dteColo  : setOfDTEColours){
				// type of literals associated to this datatype edge colour
				String type = grph.getLiteralType(dteColo);
				String orginalTypes = mTypesOfDTEColours.get(dteColo);
				
				// mapping of original type and new type are here 
				if(orginalTypes == null || orginalTypes.isEmpty()){
					mTypesOfDTEColours.put(dteColo, type);
					orginalTypes = type;
				}else{
					if(orginalTypes.contains("XMLSchema#string") && !type.contains("XMLSchema#string")){
						mTypesOfDTEColours.put(dteColo, type);
						orginalTypes = type;
					}
				}
			}
		}
		
		//collect values of literals
		for(ColouredGraph grph : origGrphs){
			// map of datatype edge colours to tail colours with their literals
			Map<BitSet, Map<BitSet, Set<String>>> mapDTEdgeColoursToLiterals = grph.getMapLiterals();
			
			// set of datatype edge colours
			Set<BitSet> setOfDTEColours = mapDTEdgeColoursToLiterals.keySet();
			
			for(BitSet dteColo  : setOfDTEColours){
				
				// map of existing literals associated with tail colours
				Map<BitSet, Set<String>> origMapOfTColoAndLiterals = mValuesOfEachDTEColour.get(dteColo);
				if(origMapOfTColoAndLiterals == null){
					origMapOfTColoAndLiterals = new HashMap<BitSet, Set<String>>();
					mValuesOfEachDTEColour.put(dteColo, origMapOfTColoAndLiterals);
				}

				//get type of the literal
				String orginalTypes = mTypesOfDTEColours.get(dteColo);
				
				// map of literals associated with tail colours
				Map<BitSet, Set<String>> mapOfTColoAndLiterals = mapDTEdgeColoursToLiterals.get(dteColo);
				
				if(mapOfTColoAndLiterals != null && mapOfTColoAndLiterals.size() > 0){
					
					Set<BitSet> setOfTColours = mapOfTColoAndLiterals.keySet();
					for(BitSet tColo : setOfTColours){
						
						Set<String> setOfExistingLiterals = origMapOfTColoAndLiterals.get(tColo);
						if(setOfExistingLiterals == null){
							setOfExistingLiterals = new HashSet<String>();
							origMapOfTColoAndLiterals.put(tColo, setOfExistingLiterals);
						}
						
						Set<String> setOfLiterals= mapOfTColoAndLiterals.get(tColo);
						
						int totalNoOfWords = 0;
						
						for(String literal: setOfLiterals){
							
							if(literal.isEmpty())
								continue;
							
							// remove postfix of literal if any
							literal = normalizeLiterals(literal);
							
							String[] arrWords = literal.split(" ");
							
							for(String word: arrWords){
								
								if(orginalTypes.contains("XMLSchema#string")){
									word = normalizeWords(word);
								}
								
								if(!word.isEmpty()){
									totalNoOfWords ++;
									setOfExistingLiterals.add(word);
								}
							}
						}
						//update the average words of a literal
						double avrgNoOfWordsPerLiteral = totalNoOfWords/setOfLiterals.size();
						ObjectDoubleOpenHashMap<BitSet> mapAvrgLengthOfLiterals = mAvrgNoOfWordsPerDTEdgeColour.get(tColo);
						if(mapAvrgLengthOfLiterals == null){
							mapAvrgLengthOfLiterals = new ObjectDoubleOpenHashMap<BitSet>();
							mAvrgNoOfWordsPerDTEdgeColour.put(tColo, mapAvrgLengthOfLiterals);
						}
						mapAvrgLengthOfLiterals.putOrAdd(dteColo, avrgNoOfWordsPerLiteral, avrgNoOfWordsPerLiteral);
						
						
						ObjectIntOpenHashMap<BitSet> mapAppearTimes = mapAppearTimesOfDTEColoursOverTColo.get(tColo);
						if(mapAppearTimes == null ){
							mapAppearTimes = new ObjectIntOpenHashMap<BitSet>();
							mapAppearTimesOfDTEColoursOverTColo.put(tColo, mapAppearTimes);
						}
						
						mapAppearTimes.putOrAdd(dteColo, 1, 1);
					}
				}
			}
		}
		
		/*
		 *  compute average words of a literal associated with a specific data
		 *  typed property over all graphs
		 */
		
		Set<BitSet> setOfTColours = mapAppearTimesOfDTEColoursOverTColo.keySet();
		for(BitSet tColo: setOfTColours){
			ObjectIntOpenHashMap<BitSet> mapAppearTimeOfEachDTEColo = mapAppearTimesOfDTEColoursOverTColo.get(tColo);
			ObjectDoubleOpenHashMap<BitSet> mapAvrgLengthOfLiterals = mAvrgNoOfWordsPerDTEdgeColour.get(tColo);
			
			if(mapAppearTimeOfEachDTEColo!= null && mapAppearTimeOfEachDTEColo.size() > 0 
					&& mapAvrgLengthOfLiterals!= null && mapAvrgLengthOfLiterals.size() > 0){
				
				Object[] arrDTEColours = mapAppearTimeOfEachDTEColo.keys;
				for(int i = 0 ; i< arrDTEColours.length ; i++){
					if(mapAppearTimeOfEachDTEColo.allocated[i]){
						BitSet dteColo = (BitSet)arrDTEColours[i];
						
						int noOfAppearTimes = mapAppearTimeOfEachDTEColo.get(dteColo);
						
						double avrgWordsPerLiteral = mapAvrgLengthOfLiterals.get(dteColo);
						
						if(noOfAppearTimes != 0)
							mapAvrgLengthOfLiterals.put(dteColo, avrgWordsPerLiteral/noOfAppearTimes);
						else
							mapAvrgLengthOfLiterals.put(dteColo, 0);
					}
				}
			}
		}
	}
	
	public String getDataTypes(BitSet dteColo){
		return mTypesOfDTEColours.get(dteColo);		
	}
	
	/**
	 * get a set of words associated to the data typed edge's colour
	 * @param dteColo
	 * @return a set of words
	 */
	public Set<String> getSetOfValues(BitSet tColo, BitSet dteColo){
		Set<String> res = new HashSet<String>();
		if(mValuesOfEachDTEColour.containsKey(dteColo)){
			Map<BitSet, Set<String>> mapOfTColoAndLiterals = mValuesOfEachDTEColour.get(dteColo);
			if(mapOfTColoAndLiterals != null )
				return mapOfTColoAndLiterals.get(tColo);
		}
		return res;
	}
	
	/**
	 * get the average number of words that a data typed edge's can hae
	 * @param dteColo the data typed edge's colour
	 * 
	 * @return the average number of words
	 */
	public double getAvrgNoOfWords(BitSet tColo, BitSet dteColo){
		if(mAvrgNoOfWordsPerDTEdgeColour.containsKey(tColo)){
			ObjectDoubleOpenHashMap<BitSet> mapAvrgLengthOfLiterals = mAvrgNoOfWordsPerDTEdgeColour.get(tColo);
			if(mapAvrgLengthOfLiterals!= null)
				return mapAvrgLengthOfLiterals.get(dteColo);
		}
		return 0;
	}
	
	private String normalizeLiterals(String originalLiteral){
		
		if(originalLiteral.contains("^^")){
			int endPos = originalLiteral.indexOf("^^");
			originalLiteral = originalLiteral.substring(0, endPos);		
		}
		
		if(originalLiteral.startsWith("\"") && originalLiteral.contains("\"@")){
			int endPos = originalLiteral.indexOf("\"@");
			originalLiteral = originalLiteral.substring(0, endPos);
			originalLiteral = originalLiteral.substring(1);			
		}
		
		return originalLiteral;
	}
	
	/**
	 * this function is only applied for string type values
	 * @param word
	 * @return
	 */
	private String normalizeWords(String word){
	Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
		
		word = word.trim();
		
		//remove puntuation chars
		word = word.replaceAll("\\p{P}", "");
		
		//remove new line chars
		word = word.replace("\n", "").replace("\r", "");
		
		word = word.replace("\t", "");
		
		//remove special chars in prefix
		//word = word.replaceFirst("[^A-Za-z0-9]", "");	
		
		//remove special chars in postfix
		//String reversedWord = new StringBuffer(word).reverse().toString();
		//reversedWord = reversedWord.replaceFirst("[^A-Za-z0-9]", "");	
		//word = new StringBuffer(reversedWord).reverse().toString();		
		Matcher hasSpecial = special.matcher(word);
		if(word.contains("-"))
		{
			System.err.println("");
		}
		if(hasSpecial.find()){
			if(word.length() == 1){ 
				return "";  
			}		
		}
		return word;
	}
}
