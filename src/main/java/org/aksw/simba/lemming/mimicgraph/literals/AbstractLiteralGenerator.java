package org.aksw.simba.lemming.mimicgraph.literals;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.carrotsearch.hppc.BitSet;

public abstract class AbstractLiteralGenerator implements ILiteralGenerator{
	
	// 1st key: the colour of datatyped edge - 2nd key: the colour of tail - value: list of data
	protected Map<BitSet, Map<BitSet, Set<String>>> mBaseData;
	
	protected Random mRand ;
	
	public AbstractLiteralGenerator(Map<BitSet, Map<BitSet, Set<String>>> sampleData){
		mBaseData = sampleData;
		
		mRand = new Random();
	}
	
	@Override
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues){
		return "";
	}	
}
