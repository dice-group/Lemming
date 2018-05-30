package org.aksw.simba.lemming.grph.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.word2vecrestful.word2vec.Word2VecFactory;
import org.aksw.word2vecrestful.word2vec.Word2VecModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class RDFLiteralGenerator implements IRDFLiteralGenerator{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RDFLiteralGenerator.class);
	
	private Map<BitSet,Set<String>> mBaseData;
	private Word2VecModel mWord2VecModel ;
	
	private Map<BitSet, float[]> mMeanVectors;
	private Map<BitSet, float[]> mStandardDeviationVectors;
	private Random mRand ;
	
	/**
	 * Constructor
	 * @param sampleData the map of data typed edge's colours and their corresponding words
	 */
	public RDFLiteralGenerator(Map<BitSet, Set<String>> sampleData){
		mBaseData = sampleData;
		mWord2VecModel = Word2VecFactory.get();
		mRand = new Random();
		
		mMeanVectors = new HashMap<BitSet, float[]>();
		mStandardDeviationVectors = new HashMap<BitSet, float[]>();
		computeDataVectors();
	}
	
	/**
	 * compute a vector for each of word based on the word2vec model
	 */
	private void computeDataVectors(){
		LOGGER.info("Start - computation of a mean vector and a standard deviation vector");
		Set<BitSet> setOfDTEColours = mBaseData.keySet();
		for(BitSet dteColo: setOfDTEColours){
			float[] meanVec = new float[mWord2VecModel.vectorSize];
			float[] standardDeviationVec = new float[mWord2VecModel.vectorSize];
			
			Set<String> setOfWords = mBaseData.get(dteColo);
			
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
			}
			
			mMeanVectors.put(dteColo, meanVec);
			mStandardDeviationVectors.put(dteColo, standardDeviationVec);
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
	private float[] getRandomVector(BitSet dteColo){
		float [] randomVec = new float[mWord2VecModel.vectorSize];
		
		float[] meanVec = mMeanVectors.get(dteColo);
		float[] stdDevVec = mStandardDeviationVectors.get(dteColo);
		if(meanVec != null && meanVec.length > 0 && stdDevVec != null && stdDevVec.length > 0 ){
			for(int i = 0 ; i < mWord2VecModel.vectorSize ; i++){
				randomVec[i] = (float) mRand.nextGaussian() * stdDevVec[i] + meanVec[i];
			}	
			return randomVec;
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
	public String getWords(BitSet dteColo, int noOfWords) {
		String literal = "";
		int tempNoOfGeneratedWords = 0;
		if(dteColo != null  && noOfWords > 0){
			double currentTime = System.currentTimeMillis();
	       
			
			while(tempNoOfGeneratedWords <= noOfWords){
				float[] wordVec = getRandomVector(dteColo);
				if(wordVec != null ){
					Map<String, float[]> mapNewWords = mWord2VecModel.getClosestEntry(wordVec);
					if(mapNewWords != null && mapNewWords.size() > 0){
						tempNoOfGeneratedWords ++;
						
						// get random a word and put it to the result
						Set<String> setOfWords = mapNewWords.keySet();
						String [] arrOfWords = setOfWords.toArray(new String[0]);
						
						literal += arrOfWords[mRand.nextInt(arrOfWords.length)]; 
					}
				}
			}
		    currentTime = System.currentTimeMillis() - currentTime;
		    System.out.println("Time to get " + noOfWords + " of data typed edge's colour ("+dteColo+") is " + currentTime);
		}
		
		return literal;
	}
}
