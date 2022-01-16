package org.aksw.simba.lemming.mimicgraph.literals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.BitSet;

public class RDFLiteralGenertor {

	private LiteralAnalysis mLiteralAnalysis;
	private Map<String , ILiteralGenerator> mMapOfDataTypesAndGenerators;

	public RDFLiteralGenertor(ColouredGraph[] origGrphs){
		// literal collection
		mLiteralAnalysis = new LiteralAnalysis(origGrphs);
		mMapOfDataTypesAndGenerators = new HashMap<>();

		initializeGenerators();
	}

	private void initializeGenerators(){
		if(mLiteralAnalysis != null){

			Map<String, Set<BitSet>> mapOfTypesAndDTEColo = mLiteralAnalysis.getMapOfTypesAndDTEColours();
			if(mapOfTypesAndDTEColo!= null && mapOfTypesAndDTEColo.size()> 0 ){
				Set<String> setOfTypes = mapOfTypesAndDTEColo.keySet();

				for(String dataType : setOfTypes){
					Set<BitSet> setOfDTEColours = mapOfTypesAndDTEColo.get(dataType);
					// get sample data according to the types and dteColours;
					Map<BitSet, Map<BitSet, Set<String>>> mapOfDTEColoAndVColoValues = mLiteralAnalysis.getMapOfDTEColoursAndValues(setOfDTEColours);

					if(mapOfDTEColoAndVColoValues!=null){
					    if(dataType.equals("http://def.seegrid.csiro.au/isotc211/iso19103/2005/basic#ordinates")){
					        ILiteralGenerator ordinatesGenerator = new OrdinatesLiteralGenerator(mapOfDTEColoAndVColoValues);
					        mMapOfDataTypesAndGenerators.put(dataType, ordinatesGenerator);

                        } else if(dataType.contains("#integer") || dataType.contains("#float") || dataType.contains("#long") ||
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
							//ILiteralGenerator datetimeGenerator = new StringLiteralGenerator(mapOfDTEColoAndVColoValues);
							ILiteralGenerator datetimeGenerator = new AtomicLiteralGenerator(mapOfDTEColoAndVColoValues);
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

			//System.out.println("\t\tGet "+numOfValues+" word(s) of type:" + typeOfData );
			ILiteralGenerator literalGenerator = mMapOfDataTypesAndGenerators.get(typeOfData);
			//double currentTime = System.currentTimeMillis();
			literal = literalGenerator.getValue(vColo, dteColo, (int)numOfValues);
		    //currentTime = System.currentTimeMillis() - currentTime;
		    //System.out.println("\t\t\t Time comsumed:" + currentTime);
		}
		return literal;
	}


	public String getLiteralType(BitSet dteColo){
		String typeOfData = "http://www.w3.org/2001/XMLSchema#string";
		if(dteColo !=null){
			typeOfData = mLiteralAnalysis.getDataTypes(dteColo);
		}
		return typeOfData;
	}
}
