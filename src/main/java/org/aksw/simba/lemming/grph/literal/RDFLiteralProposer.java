package org.aksw.simba.lemming.grph.literal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class RDFLiteralProposer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RDFLiteralProposer.class);
	
	private Map<BitSet, String> mMapOfDataTypes;
	
	private LiteralAnalysis mLiteralAnalysis;
	
	private Map<String , ILiteralGenerator> mMapOfDataTypesAndGenerators;
	
	public RDFLiteralProposer(ColouredGraph[] origGrphs){
		// literal collection
		mLiteralAnalysis = new LiteralAnalysis(origGrphs);
		mMapOfDataTypesAndGenerators = new HashMap<String, ILiteralGenerator>();
		
		initializeGenerators();
	}
	
	private void initializeGenerators(){
		if(mLiteralAnalysis != null){
			
			
//			(int) mLiteralAnalysis.getAvrgNoOfWords(dteColo),
//			mLiteralAnalysis.getDataTypePropertyURI(dteColo)
			
			Map<String, Set<BitSet>> mapOfTypesAndDTEColo = mLiteralAnalysis.getMapOfTypesAndDTEColours();
			if(mapOfTypesAndDTEColo!= null && mapOfTypesAndDTEColo.size()> 0 ){
				Set<String> setOfTypes = mapOfTypesAndDTEColo.keySet();
				for(String dataType : setOfTypes){
					
					Set<BitSet> setOfDTEColours = mapOfTypesAndDTEColo.get(dataType);
					// get sampledata according to the types and dteColours;
					Map<BitSet, Map<BitSet, Set<String>>> mapOfDTEColoAndVColoValues = mLiteralAnalysis.getMapOfDTEColoursAndValues(setOfDTEColours);
					
					if(mapOfDTEColoAndVColoValues!=null){
						if(dataType.contains("#integer") || dataType.contains("#float") || dataType.contains("#long") ||
								dataType.contains("#double") || dataType.contains("#short") || dataType.contains("#char")){
							//create a numeric generator
							ILiteralGenerator nummericGenerator = new NumericLiteralGenerator(mapOfDTEColoAndVColoValues);
							mMapOfDataTypesAndGenerators.put(dataType, nummericGenerator);
							
						}else if(dataType.contains("#boolean") || dataType.contains("#bool")){
							//create a boolean generator
							ILiteralGenerator booleanGenerator = new BooleanLiteralGenerator(mapOfDTEColoAndVColoValues);
							mMapOfDataTypesAndGenerators.put(dataType, booleanGenerator);
						}else if(dataType.contains("#datetime") || dataType.contains("#date") || dataType.contains("dateTime")){
							ILiteralGenerator datetimeGenerator = new DateTimeLiteralGenerator(mapOfDTEColoAndVColoValues);
							mMapOfDataTypesAndGenerators.put(dataType, datetimeGenerator);
						}else{
							//by default create a string generator
							ILiteralGenerator datetimeGenerator = new StringLiteralGenerator(mapOfDTEColoAndVColoValues);
							//ILiteralGenerator datetimeGenerator = new UntypedLiteralGenerator(mapOfDTEColoAndVColoValues);
							mMapOfDataTypesAndGenerators.put(dataType, datetimeGenerator);
						}
					}//end if of checking valid sample data
				}// end for of dataType
			}// end if of checking valid map types and edge's colours
		}
	}
	
	/**
	 * get a string which includes 'noOfWords' words which are closest to the input set of words
	 * associated with the dteColo (of a data typed proerty)
	 * 
	 * @return a string of words
	 */
	public String getValue(BitSet vColo, BitSet dteColo) {
	String literal = "";
		if(vColo != null && dteColo !=null){
			double numOfValues = mLiteralAnalysis.getAvrgNoOfWords(vColo, dteColo);
			String typeOfData = mLiteralAnalysis.getDataTypes(dteColo);

			ILiteralGenerator literalGenerator = mMapOfDataTypesAndGenerators.get(typeOfData);
			double currentTime = System.currentTimeMillis();
			literal = literalGenerator.getValue(vColo, dteColo, (int)numOfValues);
		    currentTime = System.currentTimeMillis() - currentTime;
		}
		return literal;
	}
}
