package org.aksw.simba.lemming.grph.literal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class DateTimeLiteralGenerator extends AbstractLiteralGenerator implements ILiteralGenerator{

	private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeLiteralGenerator.class);
	
	private Map<BitSet, Map<BitSet, LocalDateTime>> mapOfStartDate;
	
	private Map<BitSet, Map<BitSet, LocalDateTime>> mapOfEndDate;
	
	public DateTimeLiteralGenerator(
			Map<BitSet, Map<BitSet, Set<String>>> sampleData) {
		super(sampleData);
		
		mapOfStartDate = new HashMap<BitSet, Map<BitSet, LocalDateTime>>();
		mapOfEndDate = new HashMap<BitSet, Map<BitSet, LocalDateTime>>();
		
		computeDateRange();
	}
	
	private void computeDateRange(){
		LOGGER.info("Start - computation of range for datetime values");
		
		//set of datatyped edge colours
		Set<BitSet> setOfDTEColours = mBaseData.keySet();
		for(BitSet dteColo: setOfDTEColours){
			Map<BitSet, Set<String>> mapOfTColoAndValues = mBaseData.get(dteColo);
			if(mapOfTColoAndValues != null && !mapOfTColoAndValues.isEmpty()){
				Map<BitSet, LocalDateTime> mapOfTColoAndStartDate = new HashMap<BitSet, LocalDateTime>();
				Map<BitSet, LocalDateTime> mapOfTColoAndEndDate = new HashMap<BitSet, LocalDateTime>();
				
				//set of tail colours
				Set<BitSet> setOfTColours = mapOfTColoAndValues.keySet();
				for(BitSet tColo : setOfTColours){
					//set of values
					Set<String> setOfValues = mapOfTColoAndValues.get(tColo);
					if(setOfValues != null && setOfValues.size() > 0 ){
						LocalDateTime startDate = LocalDateTime.now();
						LocalDateTime endDate = LocalDateTime.now();
						LocalDateTime date = LocalDateTime.now();
						for(String val : setOfValues){
							try{
								date = LocalDateTime.parse(val);
							}catch(Exception Ex){
							}
							if(date.isBefore(startDate)){
								startDate = date;
							}
							
							if(date.isAfter(endDate)){
								endDate = date;
							}
						}
						
						mapOfTColoAndStartDate.put(tColo, startDate);
						mapOfTColoAndEndDate.put(tColo, endDate);
					}
				}
				
				mapOfStartDate.put(dteColo, mapOfTColoAndStartDate);
				mapOfEndDate.put(dteColo, mapOfTColoAndEndDate);
			}//end if of checking valid mapOfTColoAndValues
		}//end for of dteColo
		
		LOGGER.info("End - computation of range for datetime values");
	}

	@Override
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues) {
		String literal = "";
		if(tColo!= null && dteColo != null && numberOfValues > 0){
			Map<BitSet, LocalDateTime> mapOfTColoAndStartDate = mapOfStartDate.get(dteColo);
			Map<BitSet, LocalDateTime> mapOfTColoAndEndDate = mapOfEndDate.get(dteColo);
			
			if(mapOfTColoAndStartDate!= null && mapOfTColoAndStartDate.containsKey(tColo) 
					&& mapOfTColoAndEndDate != null && mapOfTColoAndEndDate.containsKey(tColo)){
				
				LocalDateTime startDate = mapOfTColoAndStartDate.get(tColo);
				LocalDateTime endDate = mapOfTColoAndEndDate.get(tColo);
				
				
				long startDay = startDate.toLocalDate().toEpochDay();
				long endDay = endDate.toLocalDate().toEpochDay();
				
				
				long randDay = (long)(startDay + mRand.nextDouble() * (endDay - startDay));
				LocalDateTime randValue =
						LocalDateTime.of(LocalDate.ofEpochDay(randDay),  LocalTime.ofSecondOfDay(0));
				
				literal += randValue.toString();
			}
		}
		return literal.trim();
	}
}
