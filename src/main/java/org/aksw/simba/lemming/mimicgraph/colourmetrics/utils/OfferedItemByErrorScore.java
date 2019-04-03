package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.util.Constants;

public class OfferedItemByErrorScore<T> implements IOfferedItem<T> {

	private T[] mArrBaseItems;
	private int[] mArrBaseItemIndices;
	private double[] mArrBaseItemProb;
		
	private double[] mSimulatedArr;
	
	private long mRuntimeNoOfItems = 0;
	private int mLengthOfArr = 0;
	
	public OfferedItemByErrorScore(ObjectDistribution<T> objDist){
		mLengthOfArr = objDist.sampleSpace.length;
		mArrBaseItems = (T[])new Object[mLengthOfArr];
		mArrBaseItemProb = new double[mLengthOfArr];
		copyData(objDist);
		buildSimulatedArray();
	}
	
	public OfferedItemByErrorScore(ObjectDistribution<T> objDist, Random rand) {
		mLengthOfArr = objDist.sampleSpace.length;
		mArrBaseItems = (T[])new Object[mLengthOfArr];
		mArrBaseItemProb = new double[mLengthOfArr];
		copyData(objDist);
		buildSimulatedArray();
	}
	
	private void copyData(ObjectDistribution<T> objDist){
		for(int i = 0 ; i < mLengthOfArr; i++){
			mArrBaseItems[i] = objDist.sampleSpace[i];
			mArrBaseItemProb[i] = objDist.values[i];
		}
	}
	
	/**
	 * build a frequency array of each base item
	 */
	private void buildSimulatedArray(){
		mSimulatedArr = new double[mLengthOfArr];
		mArrBaseItemIndices = new int[mLengthOfArr];
		
		for(int i = 0 ; i < mLengthOfArr; i++){
			mSimulatedArr[i] = 0;
			mArrBaseItemIndices[i] = i;
		}
		
		//sort the array of the probability of base items
		sortBaseItemProb();
	}
	
	private void sortBaseItemProb(){
		
		int i = 0 ; 
		while( i <mLengthOfArr){
			int j = i;
			while(j > 0 && mArrBaseItemProb[j] > mArrBaseItemProb[j-1]){
				
				//swap samples
				T tempSample = mArrBaseItems[j-1];
				mArrBaseItems[j-1] = mArrBaseItems[j];
				mArrBaseItems[j] = tempSample;
				
				//swap samples' values
				double tempSampleVal = mArrBaseItemProb[j-1];
				mArrBaseItemProb[j-1] = mArrBaseItemProb[j];
				mArrBaseItemProb[j] = tempSampleVal;
				
				// swap samples's indices
				int tempSampleIndex = mArrBaseItemIndices[j-1];
				mArrBaseItemIndices[j-1] = mArrBaseItemIndices[j];
				mArrBaseItemIndices[j] = tempSampleIndex;
				
				j--;
			}
			i++;
		}
	}
	
	private double getPotentialProb() {
		int potentialIndex = -1;
		if(mRuntimeNoOfItems == 0 ){
			potentialIndex = 0;
		}else{
			double minError = Double.MAX_VALUE;
			
			for(int i = 0 ; i < mLengthOfArr ; i++){
				double runtimeSampleVal = mSimulatedArr[i] / mRuntimeNoOfItems;
				double errorScore = runtimeSampleVal - mArrBaseItemProb[i];
				if(minError > errorScore){
					minError = errorScore;
					potentialIndex = i;
				}
			}
			
			if(minError == 0 ){
				potentialIndex = 0;
			}
		}
		
		mRuntimeNoOfItems ++;
		mSimulatedArr[potentialIndex]++;
		return mArrBaseItemProb[potentialIndex];
	}

	@Override
	public T getPotentialItem() {

		int potentialIndex = -1;
		if(mRuntimeNoOfItems == 0){
			potentialIndex = 0;
		}else{
			double minError = Double.MAX_VALUE;
			
			for(int i = 0 ; i < mLengthOfArr ; i++){
				double runtimeSampleVal = mSimulatedArr[i] / mRuntimeNoOfItems;
				double errorScore = runtimeSampleVal - mArrBaseItemProb[i];
				if(minError > errorScore ){
					minError = errorScore;
					potentialIndex = i;
				}
			}
			
			if(minError == 0 ){
				potentialIndex = 0;
			}
		}
		
		
		mRuntimeNoOfItems ++;
		mSimulatedArr[potentialIndex]++;
		
		return mArrBaseItems[potentialIndex];
	}

	private int getPotentialIndex() {
		int potentialIndex = -1;
		
		if(mRuntimeNoOfItems == 0){
			potentialIndex = 0;
		}else{
			double minError = Double.MAX_VALUE;
			
			for(int i = 0 ; i < mLengthOfArr ; i++){
				double runtimeSampleVal = mSimulatedArr[i] / mRuntimeNoOfItems;
				double errorScore = runtimeSampleVal - mArrBaseItemProb[i];
				if(minError > errorScore ){
					minError = errorScore;
					potentialIndex = i;
				}
			}
			
			if(minError == 0 ){
				potentialIndex = 0;
			}
		}
			
		mRuntimeNoOfItems ++;
		mSimulatedArr[potentialIndex]++;
	
		return mArrBaseItemIndices[potentialIndex];
	}

	@Override
	public T getPotentialItem(Set<T> setOfRestrictedItems) {
		
		if(setOfRestrictedItems.size() == 1){
			for(T item: setOfRestrictedItems){
				for(T baseItem: mArrBaseItems){
					if(mArrBaseItems.equals(item))
						return item;
				}
			}
		}
		/*
		 * random value is within an interval [mLowerBound, mUpperBound] 
		 */
		for(int i = 0 ; i < Constants.MAX_EXPLORING_TIME ;i++){
			T baseItem = getPotentialItem() ;
			 if(setOfRestrictedItems.contains(baseItem))
				 return baseItem;
		}
		return null;
	}

	@Override
	public T getPotentialItem(Set<T> setOfRestrictedItems,
			boolean reusedProbability) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem#getSeed()
	 */
	@Override
	public long getSeed() {
		return 0;
	}
	
	
}
