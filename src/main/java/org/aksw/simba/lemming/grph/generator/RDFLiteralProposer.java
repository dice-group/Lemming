package org.aksw.simba.lemming.grph.generator;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.word2vecrestful.word2vec.Word2VecFactory;
import org.aksw.word2vecrestful.word2vec.Word2VecModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

public class RDFLiteralProposer implements IRDFLiteralProposer{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RDFLiteralProposer.class);
	
	private Map<BitSet,Set<String>> mBaseData;
	private Word2VecModel mWord2VecModel ;
	
	private Map<BitSet, float[]> mMeanVectors;
	private Map<BitSet, float[]> mVariantVectors;
	private Random mRand ;
	
	/**
	 * Constructor
	 * @param sampleData the map of data typed edge's colours and their corresponding words
	 */
	public RDFLiteralProposer(Map<BitSet, Set<String>> sampleData){
		mBaseData = sampleData;
		mWord2VecModel = Word2VecFactory.get();
		mRand = new Random();
		computeDataVectors();
	}
	
	/**
	 * compute a vector for each of word based on the word2vec model
	 */
	private void computeDataVectors(){
		
		Set<BitSet> setOfDTEColours = mBaseData.keySet();
		for(BitSet dteColo: setOfDTEColours){
			float[] meanVec = new float[mWord2VecModel.vectorSize];
			float[] standardDeviationVec = new float[mWord2VecModel.vectorSize];
			
			Set<String> setOfWords = mBaseData.get(dteColo);
			
			if(setOfWords != null && setOfWords.size() > 0 ){
				
				for(String word: setOfWords){
					float[] wordVec = mWord2VecModel.word2vec.get(word);
					
					for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
						meanVec[i] += wordVec[i];
					}
				}
				
				// compute the average of value in each dimension of these words
				for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
					meanVec[i] = meanVec[i]/ setOfWords.size();
				}	
				
				
				// compute the standard deviation of value in each dimension of these words
				for(String word: setOfWords){
					float[] wordVec = mWord2VecModel.word2vec.get(word);
					
					for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
						standardDeviationVec[i] += Math.pow(wordVec[i] - meanVec[i],2);
					}
				}
				
				// compute the average of value in each dimension of these words
				for(int i = 0 ; i< mWord2VecModel.vectorSize ; ++i){
					double tempVal = standardDeviationVec[i]/ setOfWords.size();
					standardDeviationVec[i] = (float)Math.sqrt(tempVal);
				}
			}
			
			mMeanVectors.put(dteColo, meanVec);
			mVariantVectors.put(dteColo, standardDeviationVec);
		}
	}

	/**
	 * compute the average value in each dimension for a vector according to a mean vector
	 * @param wordVec an input vector
	 * @param meanVec the corresponding mean vector
	 * @return an average vector
	 */
	private float[] computeAverageVector(float [] wordVec, float[] meanVec){
		float[] avrVec = new float[mWord2VecModel.vectorSize];
		for (int i = 0 ; i < mWord2VecModel.vectorSize ; ++i){
			avrVec[i] = (wordVec[i] + meanVec[i])/2;
		}
		return avrVec; 
	}
	
	/**
	 * get a string which includes 'noOfWords' words which are closest to the input set of words
	 * associated with the dteColo (of a data typed proerty)
	 * 
	 * @return a string of words
	 */
	@Override
	public String getWords(BitSet dteColo, Set<String> setOfBaseWords,
			int noOfWords) {
		String literal = "";
		int tempNoOfGeneratedWords = 0;
		if(dteColo != null && setOfBaseWords!=null && noOfWords > 0){
			
			if(setOfBaseWords.size() != noOfWords){
				LOGGER.warn("The desired numberd of words is differnt to the number of sample words");
			}
			
			float [] meanVec = mMeanVectors.get(dteColo);
			
			for(String word : setOfBaseWords){
				
				float[] wordVec = mWord2VecModel.word2vec.get(word);
				wordVec = computeAverageVector(wordVec, meanVec);
				Map<String, float[]> mapNewWords = mWord2VecModel.getClosestEntry(wordVec);
				if(mapNewWords != null && mapNewWords.size() > 0){
					tempNoOfGeneratedWords ++;
					
					// get random a word and put it to the result
					Set<String> setOfWords = mapNewWords.keySet();
					String [] arrOfWords = setOfWords.toArray(new String[0]);
					
					literal += arrOfWords[mRand.nextInt(arrOfWords.length)]; 
				}
				
				if(tempNoOfGeneratedWords > noOfWords){
					break;
				}
			}
			
			if(tempNoOfGeneratedWords < noOfWords){
				// get random word in the input list and generate until getting the desired number of words
				String[] arrOfBaseWords = setOfBaseWords.toArray(new String[0]); 
				while(tempNoOfGeneratedWords <= noOfWords){
					String word = arrOfBaseWords[mRand.nextInt(arrOfBaseWords.length)];
					float[] wordVec = mWord2VecModel.word2vec.get(word);
					wordVec = computeAverageVector(wordVec, meanVec);
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
		}
		
		return literal;
	}
}
