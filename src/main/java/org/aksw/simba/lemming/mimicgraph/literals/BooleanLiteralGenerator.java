package org.aksw.simba.lemming.mimicgraph.literals;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class BooleanLiteralGenerator extends AbstractLiteralGenerator implements ILiteralGenerator{

	private static final Logger LOGGER = LoggerFactory.getLogger(BooleanLiteralGenerator.class);
	
	// 1st key: datatype edge colour, 2nd: tail colour, and value: the probability of getting value "true"
	private Map<BitSet, Map<BitSet, Double>> mapOfProbabilities;
	
	public BooleanLiteralGenerator(
			Map<BitSet, Map<BitSet, Set<String>>> sampleData) {
		super(sampleData);
		mapOfProbabilities = new HashMap<BitSet, Map<BitSet,Double>>();
		computeDistribution();
	}

	private void computeDistribution(){
		LOGGER.info("Start - computation of probability for boolean values");
		
		Set<BitSet> setOfDTEColours = mBaseData.keySet();
		for(BitSet dteColo: setOfDTEColours){
			Map<BitSet, Set<String>> mapOfTColoAndValues = mBaseData.get(dteColo);
			if(mapOfTColoAndValues != null && !mapOfTColoAndValues.isEmpty()){
				Map<BitSet, Double> mapOfTColoProbability = new HashMap<BitSet, Double>();
				
				//set of tail colours
				Set<BitSet> setOfTColours = mapOfTColoAndValues.keySet();
				for(BitSet tColo : setOfTColours){
					//set of values
					Set<String> setOfValues = mapOfTColoAndValues.get(tColo);
					double probability = 0 ;
					if(setOfValues != null && setOfValues.size() > 0 ){
						for(String val : setOfValues){
							try{
								if(Boolean.parseBoolean(val)){
									probability ++;
								}
							}catch (Exception ex){
							}
						}
						
						probability = probability / setOfValues.size();
						mapOfTColoProbability.put(tColo, probability);
					}
				}
				
				mapOfProbabilities.put(dteColo, mapOfTColoProbability);
			}
		}
		LOGGER.info("End - computation of probability for boolean values");
	}
	
	@Override
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues) {
		String literal = "false";
		if(tColo != null && dteColo != null && numberOfValues > 0 ){
			
			Map<BitSet, Double> mapOfTColoAndProbability = mapOfProbabilities.get(dteColo);
			
			if(mapOfTColoAndProbability!= null && mapOfTColoAndProbability.containsKey(tColo)){ 
				double randomNum = mRand.nextDouble();
				if(randomNum >=0 && randomNum < mapOfTColoAndProbability.get(tColo)){
					literal = "true";
				}
			}
		}
		
		return literal;
	}
}
