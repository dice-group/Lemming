package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.Set;

public interface IOfferedItem <T>{
	public T getPotentialItem();
	public T getPotentialItem(Set<T> setOfRestrictedItems );
	public T getPotentialItem(Set<T> setOfRestrictedItems , boolean reusedProbability);
	public long getSeed();
	
	//public double getPotentialProb();
	//public int getPotentialIndex();
}
