package org.aksw.simba.lemming.grph.literal;

import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.BitSet;

public class AtomicLiteralGenerator extends AbstractLiteralGenerator implements ILiteralGenerator{

	private long mCounter;
	
	public AtomicLiteralGenerator(
			Map<BitSet, Map<BitSet, Set<String>>> sampleData) {
		super(sampleData);
		mCounter = 1;
	}
	
	private long getAndIncrement(){
		mCounter ++;
		return mCounter-1;
	}
	
	@Override
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues){
		String literal = "";
		if(tColo!=null && dteColo !=null && numberOfValues > 0){
			for(int i = 0 ; i < numberOfValues ; i++){
				literal += "value" + getAndIncrement()+ " ";
			}
		}
		return literal.trim();
	}	
}
