package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;

/**
 * Simulate the distribution as a cumulative distribution array
 * @author user
 *
 * @param <T> generic type of items considered in the distribution
 */
public class OfferedItemByRandomProb<T> implements IOfferedItem <T>{
	private T[] mArrBaseItems;
	private double[] mArrBaseItemProb;
	
	private double[] mSimulatedArr;

	private Random mRandom;
	
	private int mLengthOfArr = 0 ;
	private double mLowerBound = 0 ;
	private double mUpperBound = 0 ;
	
	private double[] mSubArrBaseItemProb;
	private T[] mSubArrBaseItems;
	
	
	public OfferedItemByRandomProb(ObjectDistribution<T> objDist){
		mArrBaseItems = objDist.sampleSpace;
		mArrBaseItemProb = objDist.values;
		mLengthOfArr = mArrBaseItemProb.length;
		mRandom = new Random();
		// build the cumulative distribution array
		buildSimulatedArray();
	}
	
	public OfferedItemByRandomProb(ObjectDistribution<T> objDist, Set<T> setOfFilteredItems){
			
			// find the intersection of 2 set
		if(setOfFilteredItems!= null && setOfFilteredItems.size() > 0){
			Set<T> intersectionSet = new HashSet<T> ();
			Object[] arrSampleSpace = objDist.sampleSpace;
			double[] arrSampleValues = objDist.values;
			int iNoOfBaseItems = arrSampleSpace.length;
			
			for(Object item: arrSampleSpace){
				intersectionSet.add((T)item);
			}
			
			// now the intersectionSet contains only items existing in both set
			intersectionSet.retainAll(setOfFilteredItems);
			
			if(intersectionSet.size() > 0){
				mArrBaseItems = (T[]) (new Object[intersectionSet.size()]);
				mArrBaseItemProb = new double[intersectionSet.size()];
				
				int jIndex = 0;
				
				for(int i = 0 ; i < iNoOfBaseItems ; i++){
					T baseItem = (T)arrSampleSpace[i];
					if(intersectionSet.contains(baseItem)){
						mArrBaseItems[jIndex] = baseItem;
						mArrBaseItemProb[jIndex] = arrSampleValues[i];
						jIndex++;
					}
				}
				
				
			}else{
				mArrBaseItems= (T[]) new Object[0];
				mArrBaseItemProb = new double[0];
			}
		}else{
			mArrBaseItems = objDist.sampleSpace;
			mArrBaseItemProb = objDist.values;
		}
		
		mRandom = new Random();
		mLengthOfArr = mArrBaseItemProb.length;
		// build the cumulative distribution array
		buildSimulatedArray();
	}
	
	/**
	 * Constructor
	 * @param objDist input distribution/probability
	 */
	public OfferedItemByRandomProb(ObjectDistribution<T> objDist, Random inRandom){
		
		mArrBaseItems = objDist.sampleSpace;
		mArrBaseItemProb = objDist.values;
		mLengthOfArr = mArrBaseItemProb.length;
		mRandom = inRandom;
		// build the cumulative distribution array
		buildSimulatedArray();
	}
	
	/**
	 * build a cumulative distribution array based on the input distribution
	 */
	private void buildSimulatedArray(){
		int iNoOfItems = mArrBaseItemProb.length;
		mSimulatedArr = new double[iNoOfItems];
		mSimulatedArr[0] = mArrBaseItemProb[0];
		for(int i = 1 ; i < iNoOfItems; i++){
			mSimulatedArr[i] = mSimulatedArr[i-1]+ mArrBaseItemProb[i]; 
		}
		
		mUpperBound = mSimulatedArr[mSimulatedArr.length - 1];
		mLowerBound = 0;  
		
	}
	
	private double getLowerbound(){
		return 0;
	}
	
	private double getUpperbound(){
		return mUpperBound;
	}
	
	public double getPotentialProb(double randomX){
		int index = indexOf(randomX, mSimulatedArr);
		if(index != -1){
			return mArrBaseItemProb[index];
		}
		return -1;
	}
	
	public T getPotentialItem(double randomX){
		
		int index = indexOf(randomX, mSimulatedArr);
		if(index != -1){
			return mArrBaseItems[index];
		}
		return null;
	}

	/**
	 * get the index of item based on the input random X
	 * 
	 * @param prob
	 * @return -1 if cannot find appropriate position, otherwise a found index
	 */
	private int indexOf(double randomX, double[] arrProbability){
		
		if(randomX < getLowerbound())
			return -1;
		
		if(arrProbability[0] >= randomX && randomX >= getLowerbound())
			return 0;
		
		if(randomX > getUpperbound())
			return -1;
		
		int lIndex = 0;
		int rIndex = arrProbability.length -1;
		
		while(rIndex > lIndex){
			int middle = (lIndex + rIndex) / 2;
			
			if(randomX > arrProbability[middle] ){
				
				if(arrProbability[middle + 1] >= randomX && (middle + 1) <= arrProbability.length){
					return middle + 1;
				}
				else{
					lIndex = middle + 1;
				}
			}
			
			if(arrProbability[middle] >= randomX){
				if(randomX > arrProbability[middle - 1]  && (middle - 1) >= 0){
					return middle ;
				}
				else{
					rIndex = middle -1;
				}
			}
		}
		return -1;
	}

	private double getPotentialProb() {
		/*
		 * random value is within an interval [mLowerBound, mUpperBound] 
		 */
		double randomX = mLowerBound +  mRandom.nextDouble()* (mUpperBound - mLowerBound);
		
		int potentialIndex = indexOf(randomX, mSimulatedArr);
		if(potentialIndex != -1){
			return mArrBaseItemProb[potentialIndex];
		}
		return -1;
	}

	@Override
	public T getPotentialItem() {
		
		if(mArrBaseItems.length == 1)
			return mArrBaseItems[0];
		else{
			/*
			 * random value is within an interval [mLowerBound, mUpperBound] 
			 */
			double randomX = mLowerBound +  mRandom.nextDouble()* (mUpperBound - mLowerBound);
			int potentialIndex = indexOf(randomX, mSimulatedArr);
			if(potentialIndex != -1){
				return mArrBaseItems[potentialIndex];
			}	
		}
		return null;
	}

	private int getPotentialIndex() {
		double randomX = mLowerBound +  mRandom.nextDouble()* (mUpperBound - mLowerBound);
		int potentialIndex = indexOf(randomX, mSimulatedArr);
		return potentialIndex;
	}
	
	@Override
	public T getPotentialItem(Set<T> setOfFilteredItems) {
		
		if(setOfFilteredItems != null){
			
			// find the intersection of 2 set
			
			Set<T> intersectionSet = new HashSet<T> ();
			for(T item: mArrBaseItems){
				intersectionSet.add(item);
			}
			
			// now the intersectionSet contains only items existing in both set
			intersectionSet.retainAll(setOfFilteredItems);
			
			if(intersectionSet.size() == 1){
				Object[] arrItems = intersectionSet.toArray(new Object[0]);
				return (T) arrItems[0];
				
			}else{
				if(intersectionSet.size() > 1){
					T[] arrFilteredItems = (T[]) (new Object[intersectionSet.size()]);
					double[] arrProbabilities = new double[intersectionSet.size()];
					
					int iNoOfBaseItems = mArrBaseItems.length;
					int jIndex = 0;
					
					for(int i = 0 ; i < iNoOfBaseItems ; i++){
						T baseItem = mArrBaseItems[i];
						if(intersectionSet.contains(baseItem)){
							arrFilteredItems[jIndex] = baseItem;
							if(jIndex == 0){
								arrProbabilities[jIndex] = mArrBaseItemProb[i];
							}else{
								arrProbabilities[jIndex] = arrProbabilities[jIndex -1] + mArrBaseItemProb[i];
							}
							jIndex++;
						}
					}

					double lowerBound = 0 ;
					double upperBound = arrProbabilities[arrProbabilities.length -1];
					
					
					T baseItem = null;
					double randomX = 0;
					int potentialIndex = -1;
					do{
						randomX = lowerBound +  mRandom.nextDouble()* (upperBound - lowerBound);
						potentialIndex = indexOf(randomX, arrProbabilities);					
						if(potentialIndex != -1){
							 baseItem = arrFilteredItems[potentialIndex];
							 if(setOfFilteredItems.contains(baseItem))
								 return baseItem;
						}	
					}while(potentialIndex == -1);
				}
			}
		}else{
			return getPotentialItem();
		}
		
		return null;
	}

	@Override
	public T getPotentialItem(Set<T> setOfFilteredItems, boolean reusedProbability) {
		
		if(reusedProbability){
			if(mSubArrBaseItems != null && mSubArrBaseItemProb!= null){
				
				double lowerBound = 0 ;
				double upperBound = mSubArrBaseItemProb[mSubArrBaseItemProb.length -1];
				
				T baseItem = null;
				double randomX = 0;
				int potentialIndex = -1;
				do{
					randomX = lowerBound +  mRandom.nextDouble()* (upperBound - lowerBound);
					potentialIndex = indexOf(randomX, mSubArrBaseItemProb);					
					if(potentialIndex != -1){
						 baseItem = mSubArrBaseItems[potentialIndex];
						 if(setOfFilteredItems.contains(baseItem))
							 return baseItem;
					}	
				}while(potentialIndex == -1);
			}
		}

		/**
		 * ccompute everything again for the setOfFilteredItems
		 */
		
		
		if(setOfFilteredItems != null){
			
			// find the intersection of 2 set
			
			Set<T> intersectionSet = new HashSet<T> ();
			for(T item: mArrBaseItems){
				intersectionSet.add(item);
			}
			
			// now the intersectionSet contains only items existing in both set
			intersectionSet.retainAll(setOfFilteredItems);
			
			if(intersectionSet.size() == 1){
				Object[] arrItems = intersectionSet.toArray(new Object[0]);
				return (T) arrItems[0];
				
			}else{
				if(intersectionSet.size() > 1){
					mSubArrBaseItems = (T[]) (new Object[intersectionSet.size()]);
					mSubArrBaseItemProb = new double[intersectionSet.size()];
					
					int iNoOfBaseItems = mArrBaseItems.length;
					int jIndex = 0;
					
					for(int i = 0 ; i < iNoOfBaseItems ; i++){
						T baseItem = mArrBaseItems[i];
						if(intersectionSet.contains(baseItem)){
							mSubArrBaseItems[jIndex] = baseItem;
							if(jIndex == 0){
								mSubArrBaseItemProb[jIndex] = mArrBaseItemProb[i];
							}else{
								mSubArrBaseItemProb[jIndex] = mSubArrBaseItemProb[jIndex -1] + mArrBaseItemProb[i];
							}
							jIndex++;
						}
					}

					double lowerBound = 0 ;
					double upperBound = mSubArrBaseItemProb[mSubArrBaseItemProb.length -1];
					
					
					T baseItem = null;
					double randomX = 0;
					int potentialIndex = -1;
					do{
						randomX = lowerBound +  mRandom.nextDouble()* (upperBound - lowerBound);
						potentialIndex = indexOf(randomX, mSubArrBaseItemProb);					
						if(potentialIndex != -1){
							 baseItem = mArrBaseItems[potentialIndex];
							 if(setOfFilteredItems.contains(baseItem))
								 return baseItem;
						}	
					}while(potentialIndex == -1);
				}
			}
		}else{
			return getPotentialItem();
		}
		
		return null;
	}
}
