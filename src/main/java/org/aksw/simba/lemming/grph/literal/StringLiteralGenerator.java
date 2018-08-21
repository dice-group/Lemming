package org.aksw.simba.lemming.grph.literal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.util.GlobalDataCollecter;
import org.aksw.simba.lemming.util.WordCentroidsCollectorIO;
import org.aksw.word2vecrestful.word2vec.Word2VecFactory;
import org.aksw.word2vecrestful.word2vec.Word2VecModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class StringLiteralGenerator extends AbstractLiteralGenerator implements ILiteralGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringLiteralGenerator.class);
	
	private Word2VecModel mWord2VecModel ;
	
	
	//1st key: the colour of datatyped edge - 2nd key: the colour of tail - value: mean vector
	private Map<BitSet, Map<BitSet, float[]>> mMeanVectors;
	
	//1st key: the colour of datatyped edge - 2nd key: the colour of tail - value: standard deviation vector
	private Map<BitSet, Map<BitSet, float[]>> mStandardDeviationVectors;
	
	public StringLiteralGenerator(Map<BitSet, Map<BitSet, Set<String>>> sampleData){
		
		super(sampleData);
		
		mMeanVectors = new HashMap<BitSet, Map<BitSet, float[]>>();
		mStandardDeviationVectors = new HashMap<BitSet, Map<BitSet, float[]>>();
		
		//word2vec model
		mWord2VecModel = Word2VecFactory.get();
		
		//compute data vectors
		computeDataVectors();
		
		//output means and standardeviation to files
		outputDataToFile();
	}
	
	private void outputDataToFile(){
		
		String dataFile = GlobalDataCollecter.getInstance().getDatasetName()+".json";
		
		File f = new File(dataFile);
		
		if(f!= null && f.exists()){
			LOGGER.info("A file of centroids and standard deviations of words is already generated!");
			return;
		}
		
		if(mMeanVectors!=null && mStandardDeviationVectors!= null){
			Set<BitSet> dteColours = mMeanVectors.keySet();
			
			for(BitSet dteColo : dteColours){
				Map<BitSet, float[]> mapTColoAndMeans = mMeanVectors.get(dteColo);
				Map<BitSet, float[]> mapTColoAndStandardDeviations = mStandardDeviationVectors.get(dteColo);
				
				Set<BitSet> tColours = mapTColoAndMeans.keySet();
				
				for(BitSet tColo : tColours){
					float[] means = mapTColoAndMeans.get(tColo);
					float[] sd = mapTColoAndStandardDeviations.get(tColo);
					
					String key = GlobalDataCollecter.getInstance().getKey(tColo, dteColo);
					WordCentroidsCollectorIO.getInstance().addWordData(key, means, sd);
				}
			}
		}
		
		WordCentroidsCollectorIO.getInstance().writeFiles(dataFile);
	}
	
	private void computeDataVectors(){
		LOGGER.info("Start - computation of a mean vector and a standard deviation vector");
		
		//set of datatyped edge colours
		Set<BitSet> setOfDTEColours = mBaseData.keySet();
		
		for(BitSet dteColo: setOfDTEColours){
			Map<BitSet, Set<String>> mapOfTColoAndValues = mBaseData.get(dteColo);
			if(mapOfTColoAndValues != null && !mapOfTColoAndValues.isEmpty()){
				
				Map<BitSet, float[]> mapOfTColoAndMean = new HashMap<BitSet, float[]>();
				Map<BitSet, float[]> mapOfTColoAndDeviation = new HashMap<BitSet, float[]>();
				
				//set of tail colours
				Set<BitSet> setOfTColours = mapOfTColoAndValues.keySet();
				for(BitSet tColo : setOfTColours){
					
					float[] meanVec = new float[mWord2VecModel.vectorSize];
					float[] standardDeviationVec = new float[mWord2VecModel.vectorSize];
					
					//set of values
					Set<String> setOfWords = mapOfTColoAndValues.get(tColo);
					
					if(setOfWords != null && setOfWords.size() > 0 ){
						
						for(String word: setOfWords){
							float[] wordVec = mWord2VecModel.word2vec.get(word);
							
							if (wordVec != null){
								for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
									meanVec[i] += wordVec[i];
								}
							}
						}
						
						// compute the average of value in each dimension of these words
						for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
							meanVec[i] = meanVec[i]/ setOfWords.size();
						}	
						
						
						// compute the standard deviation of value in each dimension of these words
						for(String word: setOfWords){
							float[] wordVec = mWord2VecModel.word2vec.get(word);
							if(wordVec != null){
								for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
									standardDeviationVec[i] += Math.pow(wordVec[i] - meanVec[i],2);
								}
							}
						}
						
						// compute the average of value in each dimension of these words
						for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
							double tempVal = standardDeviationVec[i]/ setOfWords.size();
							standardDeviationVec[i] = (float)Math.sqrt(tempVal);
						}
						
						mapOfTColoAndMean.put(tColo, meanVec);
						mapOfTColoAndDeviation.put(tColo, standardDeviationVec);						
					}
				}
				mMeanVectors.put(dteColo, mapOfTColoAndMean);
				mStandardDeviationVectors.put(dteColo, mapOfTColoAndDeviation);
			}
		}
		LOGGER.info("End - computation of a mean vector and a standard deviation vector");
	}

	
	/** 
	 * Generate a random vector based on a mean vector and a standard deviation vector.
	 * The mean and standard deivation vectors are got accordingly to the data typed edge's colour
	 * 
	 * @param dteColo the data typed edge's colour 
	 * 
	 * @return a random vector
	 */
	private float[] getRandomVector(BitSet tColo, BitSet dteColo){
		float [] randomVec = new float[mWord2VecModel.vectorSize];
		
		Map<BitSet, float[]> mapOfTailAndMean = mMeanVectors.get(dteColo);
		Map<BitSet, float[]> mapOfTailAndDeviation = mStandardDeviationVectors.get(dteColo);
		
		if(mapOfTailAndMean != null &&  mapOfTailAndMean.containsKey(tColo) 
				&& mapOfTailAndDeviation != null && mapOfTailAndDeviation.containsKey(tColo)){
			float[] meanVec = mapOfTailAndMean.get(tColo);
			float[] stdDevVec  = mapOfTailAndDeviation.get(tColo);
			
			if(meanVec != null && meanVec.length > 0 && stdDevVec != null && stdDevVec.length > 0 ){
				for(int i = 0 ; i < mWord2VecModel.vectorSize ; i++){
					randomVec[i] = (float) mRand.nextGaussian() * stdDevVec[i] + meanVec[i];
				}	
				return randomVec;
			}
		}
		return null;
	}
	
	
	/**
	 * get a string which includes 'noOfWords' words which are closest to the input set of words
	 * associated with the dteColo (of a data typed proerty)
	 * 
	 * @return a string of words
	 */
	@Override
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfWords) {
		String literal = "";
		
		//remove this when done
		if(true)
			return "hello";
		
		int noOfGeneratedWords = 0;
		if(tColo!= null && dteColo != null  && numberOfWords > 0){
			double currentTime = System.currentTimeMillis();
			
			while(noOfGeneratedWords <= numberOfWords){
				float[] wordVec = getRandomVector(tColo, dteColo);
				if(wordVec != null ){
					Map<String, float[]> mapNewWords = mWord2VecModel.getClosestEntry(wordVec);
					if(mapNewWords != null && mapNewWords.size() > 0){
						noOfGeneratedWords ++;
						
						// get random a word and put it to the result
						Set<String> setOfWords = mapNewWords.keySet();
						String [] arrOfWords = setOfWords.toArray(new String[0]);
						
						//literal += arrOfWords[mRand.nextInt(arrOfWords.length)] +" ";
						literal += arrOfWords[0] +" ";
					}
				}
			}
		    currentTime = System.currentTimeMillis() - currentTime;
		    //System.out.println("Time to get " + numberOfWords + " of data typed edge's colour ("+dteColo+") is " + currentTime);
		}
		return literal.trim();
	}
}
