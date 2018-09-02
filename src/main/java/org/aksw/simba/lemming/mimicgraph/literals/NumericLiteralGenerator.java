package org.aksw.simba.lemming.mimicgraph.literals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class NumericLiteralGenerator extends AbstractLiteralGenerator implements ILiteralGenerator{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NumericLiteralGenerator.class);
	
	private Map<BitSet, Map<BitSet, Double>> mapOfMinValues;
	
	private Map<BitSet, Map<BitSet, Double>> mapOfMaxValues;
	
	/**
	 * Constructor
	 * @param sampleData
	 */
	public NumericLiteralGenerator(
			Map<BitSet, Map<BitSet, Set<String>>> sampleData) {
		super(sampleData);
		
		mapOfMinValues = new HashMap<BitSet, Map<BitSet, Double>>();
		mapOfMaxValues = new HashMap<BitSet, Map<BitSet, Double>>();
		
		computeDataRange();		
	}
	
	private void computeDataRange(){
		LOGGER.info("Start - computation of range for nummeric values");
		
		//set of datatyped edge colours
		Set<BitSet> setOfDTEColours = mBaseData.keySet();
		
		for(BitSet dteColo: setOfDTEColours){
			Map<BitSet, Set<String>> mapOfTColoAndValues = mBaseData.get(dteColo);
			
			if(mapOfTColoAndValues != null && !mapOfTColoAndValues.isEmpty()){
				
				Map<BitSet, Double> mapOfTColoAndMin = new HashMap<BitSet, Double>();
				Map<BitSet, Double> mapOfTColoAndMax = new HashMap<BitSet, Double>();
				
				//set of tail colours
				Set<BitSet> setOfTColours = mapOfTColoAndValues.keySet();
				for(BitSet tColo : setOfTColours){
					
					//set of values
					Set<String> setOfValues = mapOfTColoAndValues.get(tColo);
					if(setOfValues != null && setOfValues.size() > 0 ){
						Double min = Double.POSITIVE_INFINITY;
						Double max = Double.NEGATIVE_INFINITY;
						for(String val: setOfValues){
							Double value = 0.;
							try{
								value = Double.parseDouble(val);
							}catch(Exception ex){
								value = 0.;
							}
							
							min = Double.min(min, value);
							max = Double.max(max, value);							
						}
						
						mapOfTColoAndMin.put(tColo, min);
						mapOfTColoAndMax.put(tColo, max);
					}
				}
				
				mapOfMinValues.put(dteColo, mapOfTColoAndMin);
				mapOfMaxValues.put(dteColo, mapOfTColoAndMax);
				
			}//end if of checking valid mapOfTColoAndValues
		}//end for of dteColo
		
		
		LOGGER.info("End - computation of range for nummeric values");
	}

	@Override
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues) {
		String literal = "";
		if(tColo!= null && dteColo != null && numberOfValues > 0){
			Map<BitSet, Double> mapOfTColoAndMin = mapOfMinValues.get(dteColo);
			Map<BitSet, Double> mapOfTColoAndMax = mapOfMaxValues.get(dteColo);
			
			if(mapOfTColoAndMin!= null && mapOfTColoAndMin.containsKey(tColo) 
					&& mapOfTColoAndMax != null && mapOfTColoAndMax.containsKey(tColo)){
				Double min = mapOfTColoAndMin.get(tColo);
				Double max = mapOfTColoAndMax.get(tColo);
				
				for(int i = 0 ; i< numberOfValues; i++){
					Double randValue = min + mRand.nextDouble()* (max - min);
					literal += randValue.longValue() + " ";
				}
			}
		}
		
		return literal.trim();
	}
}
